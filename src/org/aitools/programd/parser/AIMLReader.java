/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.parser;

import java.io.BufferedReader;
import java.lang.reflect.Field;

import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.NotAnAIMLPatternException;
import org.aitools.programd.util.PatternArbiter;
import org.aitools.programd.util.logging.Log;

/**
 * <p>
 * <code>AIMLReader</code> reads an AIML file, searching for <a
 * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-topic">
 * <code>topic</code> </a>, <a
 * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-category">
 * <code>category</code> </a>, <a
 * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-pattern">
 * <code>pattern</code> </a>, <a
 * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-pattern-side-that">pattern-side
 * <code>that</code> </a> and <a
 * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-template">
 * <code>template</code> </a> elements. If the elements occur in the correct
 * sequence/structure, their contents are delivered to the
 * {@link AIMLReaderListener#newCategory newCategory}method of whatever
 * {@link AIMLReaderListener}is assigned to this <code>AIMLReader</code>.
 * </p>
 * <p>
 * This version is a refactoring of Kris Drent's original parser (including
 * changes made by Thomas Ringate and Pedro Colla).
 * </p>
 * <p>
 * Kris Drent originally wrote, &quot;Yes, I know.... It isn't very elegant, it
 * isn't an extensible XML parser. It's a brute force AIML parser. That's what
 * it was designed to be.&quot;
 * </p>
 * 
 * @see AIMLReaderListener
 * @author Kris Drent
 * @author Thomas Ringate, Pedro Colla
 * @author Noel Bush
 * @version 4.1.3
 */
public class AIMLReader extends GenericReader
{
    /*
     * Constants used in parsing.
     */

    /** '<code>&lt;property</code>' */
    private static final String PROPERTY_OPEN = "<property";

    /** '<code>&lt;template&gt;</code>' */
    private static final String TEMPLATE_START = "<template>";

    /** '<code>&lt;/template&gt;</code>' */
    private static final String TEMPLATE_END = "</template>";

    /** '<code>&lt;pattern&gt;</code>' */
    private static final String PATTERN_START = "<pattern>";

    /** '<code>&lt;/pattern&gt;</code>' */
    private static final String PATTERN_END = "</pattern>";

    /** '<code>&lt;category</code>' */
    private static final String CATEGORY_START = "<category>";

    /** '<code>&lt;/category&gt;</code>' */
    private static final String CATEGORY_END = "</category>";

    /** '<code>&lt;that&gt;</code>' */
    private static final String THAT_START = "<that>";

    /** '<code>&lt;/that&gt;</code>' */
    private static final String THAT_END = "</that>";

    /** '<code>&lt;topic name=&quot;</code>' */
    private static final String TOPIC_START = "<topic name=\"";

    /** '<code>&lt;/topic&gt;</code>' */
    private static final String TOPIC_END = "</topic>";

    /** '<code>&lt;aiml&gt;</code>' */
    private static final String AIML_START = "<aiml>";

    /** '<code>&lt;aiml version=&quot;1.0&quot;&gt; */
    private static final String AIML_VERSION_START = "<aiml version=\"1.0\">";

    /** '<code>&lt;/aiml&gt;</code>' */
    private static final String AIML_END = "</aiml>";

    /** '<code>&lt;programd-startup&gt;</code>' */
    private static final String STARTUP_START = "<programd-startup>";

    /** '<code>&lt;/programd-startup&gt;</code>' */
    private static final String STARTUP_END = "</programd-startup>";

    /** The string &quot;localhost&quot;. */
    private static final String LOCALHOST = "localhost";

    /** The string &quot;name&quot;. */
    private static final String NAME = "name";

    /** The string &quot;pattern&quot;. */
    private static final String PATTERN = "pattern";

    /** The string &quot;that&quot;. */
    private static final String THAT = "that";

    /** The string &quot;topic&quot;. */
    private static final String TOPIC = "topic";

    /** The string &quot;template&quot;. */
    private static final String TEMPLATE = "template";

    /**
     * Any of these tags, if found at an unexpected place in a category, will
     * raise an alert that the current category is aborted.
     */
    private static final String[] UNEXPECTED_OUTSIDE_TEMPLATE =
        { TEMPLATE_END, PATTERN_END, CATEGORY_START, TEMPLATE_START, PATTERN_START, THAT_END, THAT_START, TOPIC_END,
                TOPIC_START };

    /**
     * Any of these tags, if found at an unexpected place (context not
     * specified), will raise an alert that the tag is unexpected and cause
     * parsing of the file to abort. {@link #AIML_END}is handled specially if
     * found when {@link #categoryCount}=<code>0</code>.
     * 
     * @see #alertUnexpected()
     */
    private static final String[] UNEXPECTED_GENERAL =
        { AIML_END, AIML_START, AIML_VERSION_START };

    /**
     * For convenience, a constant indicating both startup and error log
     * destinations.
     */
    private static final String[] STARTUP_AND_ERROR = new String[]
        { Log.STARTUP, Log.ERROR };

    /*
     * Parser states.
     */

    /** Parser state: not within any element. */
    private static final int S_NONE = 1;

    /** Parser state: entered an &lt;aiml&gt; element. */
    private static final int S_IN_AIML = 2;

    /** Parser state: entered a &lt;topic&gt; element. */
    private static final int S_IN_TOPIC = 3;

    /** Parser state: entered a &lt;category&gt; element. */
    private static final int S_IN_CATEGORY = 4;

    /** Parser state: entered a &lt;pattern&gt; element. */
    private static final int S_IN_PATTERN = 5;

    /** Parser state: exited a &lt;pattern&gt; element. */
    private static final int S_OUT_PATTERN = 6;

    /** Parser state: entered a &lt;that&gt; element. */
    private static final int S_IN_THAT = 7;

    /** Parser state: exited a &lt;that&gt; element. */
    private static final int S_OUT_THAT = 8;

    /** Parser state: entered a &lt;template&gt; element. */
    private static final int S_IN_TEMPLATE = 9;

    /** Parser state: exited a &lt;template&gt; element. */
    private static final int S_OUT_TEMPLATE = 10;

    /** Parser state: exited a &lt;category&gt; element. */
    private static final int S_OUT_CATEGORY = 11;

    /** Parser state: exited a &lt;topic&gt; element. */
    private static final int S_OUT_TOPIC = 12;

    /** Parser state: exited an &lt;aiml&gt; element. */
    private static final int S_OUT_AIML = 13;

    /** Parser state: entered a &lt;startup&gt; element. */
    private static final int S_IN_STARTUP = 14;

    /** Parser state: exited a &lt;startup&gt; element. */
    private static final int S_OUT_STARTUP = 15;

    /*
     * Parser actions.
     */

    /** Parser action: deliver a category. */
    private static final int DELIVER_CATEGORY = 0;

    /** Parser action: set done to true. */
    private static final int SET_DONE = 1;

    /** Parser action: abort unexpectedly. */
    private static final int ABORT = 2;

    /** Parser action: unset topic. */
    private static final int UNSET_TOPIC = 3;

    /** Parser action: process a startup element. */
    private static final int PROCESS_STARTUP = 4;

    /*
     * Instance variables.
     */

    /** Whether to warn about non-AIML elements directly beneath &lt;aiml&gt;. */
    private boolean warnNonAIML;

    /** A count of categories collected. */
    private int categoryCount = 0;

    /** The most recently collected &lt;pattern&gt;&lt;/pattern&gt; contents. */
    protected String pattern = ASTERISK;

    /** A reference to {@link #pattern}. */
    protected Field patternField;

    /** The most recently collected &lt;that&gt;&lt;/that&gt; contents. */
    protected String that = ASTERISK;

    /** A reference to {@link #that}. */
    protected Field thatField;

    /** The most recently collected &lt;topic&gt;&lt;/topic&gt; contents. */
    protected String topic = ASTERISK;

    /** A reference to {@link #topic}. */
    protected Field topicField;

    /** The most recently collected &lt;template&gt;&lt;/template&gt; contents. */
    protected String template = ASTERISK;

    /** A reference to {@link #template}. */
    protected Field templateField;

    /**
     * Constructs a new <code>AIMLReader</code>, given a
     * {@link java.io.BufferedReader BufferedReader}handle to some input stream (
     * <code>buffReader</code>), a filename to use in printing error messages (
     * <code>fileName</code>), and an {@link AIMLReaderListener}that will
     * handle creation of new categories as they are discovered.
     * 
     * @see AIMLReaderListener
     * @param fileNameToRead
     *            name of the AIML file to be read
     * @param buffReaderForFile
     *            a BufferedReader already open to the file (could be remote)
     * @param readerListener
     *            will handle new categories
     * @param warnNonAIMLSetting
     *            whether to warn about non-AIML elements directly beneath
     *            &lt;aiml&gt;
     */
    public AIMLReader(String fileNameToRead, BufferedReader buffReaderForFile, AIMLReaderListener readerListener,
            boolean warnNonAIMLSetting)
    {
        super(fileNameToRead, buffReaderForFile, readerListener);
        super.readerInstance = this;
        this.warnNonAIML = warnNonAIMLSetting;
        this.state = AIMLReader.S_NONE;
    }

    protected void initialize()
    {
        try
        {
            this.patternField = this.getClass().getDeclaredField(PATTERN);
            this.thatField = this.getClass().getDeclaredField(THAT);
            this.topicField = this.getClass().getDeclaredField(TOPIC);
            this.templateField = this.getClass().getDeclaredField(TEMPLATE);
        }
        catch (NoSuchFieldException e)
        {
            throw new DeveloperError("The developer has specified a field that does not exist in AIMLReader.");
        }
        catch (SecurityException e)
        {
            throw new DeveloperError("Security manager prevents AIMLReader from functioning.");
        }
    }

    protected void tryStates() throws TransitionMade
    {
        switch (this.state)
        {
            case S_NONE:
                transition(AIML_VERSION_START, AIMLReader.S_IN_AIML);
                transition(AIML_START, AIMLReader.S_IN_AIML);
                transition(STARTUP_START, AIMLReader.S_IN_STARTUP);
                break;

            case S_IN_AIML:
                transition(CATEGORY_START, AIMLReader.S_IN_CATEGORY);
                transition(TOPIC_START, AIMLReader.S_IN_TOPIC, this.topicField, NAME);
                break;

            case S_IN_TOPIC:
                transition(CATEGORY_START, AIMLReader.S_IN_CATEGORY);
                break;

            case S_IN_CATEGORY:
                transition(PATTERN_START, AIMLReader.S_IN_PATTERN);

            case S_IN_PATTERN:
                transition(PATTERN_END, AIMLReader.S_OUT_PATTERN, this.patternField);
                break;

            case S_OUT_PATTERN:
                transition(TEMPLATE_START, AIMLReader.S_IN_TEMPLATE);
                transition(THAT_START, AIMLReader.S_IN_THAT);
                break;

            case S_IN_TEMPLATE:
                transition(TEMPLATE_END, AIMLReader.S_OUT_TEMPLATE, this.templateField);
                break;

            case S_IN_THAT:
                transition(THAT_END, AIMLReader.S_OUT_THAT, this.thatField);
                break;

            case S_OUT_THAT:
                transition(TEMPLATE_START, AIMLReader.S_IN_TEMPLATE);
                break;

            case S_OUT_TEMPLATE:
                transition(CATEGORY_END, AIMLReader.S_OUT_CATEGORY, AIMLReader.DELIVER_CATEGORY);
                break;

            case S_OUT_CATEGORY:
                transition(CATEGORY_START, AIMLReader.S_IN_CATEGORY);
                transition(TOPIC_END, AIMLReader.S_OUT_TOPIC, AIMLReader.UNSET_TOPIC);
                transition(TOPIC_START, AIMLReader.S_IN_TOPIC, this.topicField, NAME);
                transition(AIML_END, AIMLReader.S_NONE, AIMLReader.SET_DONE);
                break;

            case S_OUT_TOPIC:
                transition(CATEGORY_START, AIMLReader.S_IN_CATEGORY);
                transition(TOPIC_START, AIMLReader.S_IN_TOPIC, this.topicField, NAME);
                transition(AIML_END, AIMLReader.S_OUT_AIML, AIMLReader.SET_DONE);
                break;

            case S_IN_STARTUP:
                transition(STARTUP_END, AIMLReader.S_OUT_STARTUP, AIMLReader.PROCESS_STARTUP);
                break;

            default:
                break;
        }
        alertUnexpected();
    }

    /**
     * <p>
     * If {@link #bufferString}contains <code>tag</code> at {@link #tagStart},
     * sets {@link #state}to <code>toState</code>, and performs the action
     * indicated by <code>action</code>.
     * </p>
     * <p>
     * If <code>action</code> is {@link #DELIVER_CATEGORY}, checks whether
     * required {@link #pattern}and {@link #template}have values; if so, calls
     * the {@link AIMLReaderListener#newCategory newCategory}method of
     * {@link #listener}; if not, alerts the user that one of the required
     * components is missing and aborts the category.
     * </p>
     * <p>
     * If <code>action</code> is {@link #SET_DONE}, sets {@link #done}to
     * <code>true</code>, so that parsing of this file is halted (no message
     * given).
     * </p>
     * <p>
     * If <code>action</code> is {@link #UNSET_TOPIC}, deletes the contents
     * of {@link #topic}.
     * </p>
     * 
     * @param tag
     *            the tag to look for in {@link #buffer}
     * @param toState
     *            the parser {@link #state}to assign if successful
     * @param action
     *            one of {{{@link #DELIVER_CATEGORY},{@link #SET_DONE},
     *            {@link #UNSET_TOPIC}}}.
     * @throws TransitionMade
     *             if the transition is successfully made
     */
    private void transition(String tag, int toState, int action) throws TransitionMade
    {
        if (succeed(tag, toState))
        {
            switch (action)
            {
                case DELIVER_CATEGORY:
                    // Check for required components in category.
                    if (this.pattern.length() == 0)
                    {
                        abortCategory("Pattern missing from category.");
                    }
                    else if (this.template.length() == 0)
                    {
                        abortCategory("Template missing from category.");
                    }
                    else
                    {
                        // Check that the pattern, that and topic are valid
                        // patterns.
                        try
                        {
                            PatternArbiter.checkAIMLPattern(this.pattern, false);
                            PatternArbiter.checkAIMLPattern(this.that, false);
                            PatternArbiter.checkAIMLPattern(this.topic, false);

                            // Deliver pattern, that and template to
                            // AIMLReaderListener.
                            ((AIMLReaderListener) super.listener).newCategory(this.pattern, this.that, this.topic,
                                    this.template);
                        }
                        catch (NotAnAIMLPatternException e)
                        {
                            abortCategory(e.getMessage());
                        }

                        // Reset pattern, that and template to defaults (note
                        // that topic is not reset).
                        this.pattern = this.template = EMPTY_STRING;
                        this.that = ASTERISK;
                        this.searchStart = 0;

                        // Index this event.
                        this.categoryCount++;

                        // Recreate the buffer (otherwise it gets huge).
                        this.buffer = new StringBuffer(Math.max(bufferStartCapacity, this.buffer.length()));
                        this.buffer.append(this.bufferString);
                    }
                    break;

                case SET_DONE:
                    this.done = true;
                    break;

                case UNSET_TOPIC:
                    this.topic = ASTERISK;
                    break;

                case PROCESS_STARTUP:
                    try
                    {
                        new StartupFileParser().processResponse(this.bufferString.substring(0, this.tagStart));
                    }
                    catch (ProcessorException e)
                    {
                        // Do nothing.
                    }
                    break;
            }
            throw (super.TRANSITION_MADE);
        }
    }

    /**
     * <p>
     * As long as state is not {@link #S_IN_TEMPLATE}, cycles through the tags
     * in {@link #UNEXPECTED_OUTSIDE_TEMPLATE}and {@link #UNEXPECTED_GENERAL}
     * and checks whether they occur at {@link #tagStart}in {@link #buffer}.
     * If so, the user is alerted that the tags are unexpected. The first of
     * {@link #UNEXPECTED_OUTSIDE_TEMPLATE}found (if any) alerts the user that
     * the current category will be aborted; the first of
     * {@link #UNEXPECTED_GENERAL}found (if any) alerts the user that the rest
     * of the file will be aborted.
     * </p>
     * <p>
     * Special attention is given to an unexpected &lt;/aiml&gt; -- if no
     * categories have been collected, an appropriate error message is
     * delivered.
     * </p>
     * <p>
     * If {@link #warnNonAIML}is set to <code>true</code>, this method will
     * also alert the user about non-AIML tags directly beneath the <aiml>
     * element that are not namespace-qualified. Obviously this is not real XML
     * validation; it's mostly meant to warn about stray &quot;meta&quot; tags
     * that aren't part of AIML.
     * </p>
     */
    private void alertUnexpected()
    {
        if (this.state != AIMLReader.S_IN_TEMPLATE)
        {
            for (int index = UNEXPECTED_OUTSIDE_TEMPLATE.length; --index >= 0;)
            {
                String unexpectedTag = UNEXPECTED_OUTSIDE_TEMPLATE[index];
                int unexpectedLength = unexpectedTag.length();
                if (this.bufferString.regionMatches(this.tagStart, unexpectedTag, 0, unexpectedLength))
                {
                    Log.userinfo(new String[]
                        { "Unexpected " + unexpectedTag + "; aborting category.",
                                "  (Line " + this.lineNumber + ", \"" + this.fileName + "\")" }, Log.ERROR);
                    return;
                }
            }
            for (int index = UNEXPECTED_GENERAL.length; --index >= 0;)
            {
                String unexpectedTag = UNEXPECTED_GENERAL[index];
                int unexpectedLength = unexpectedTag.length();
                if (this.bufferString.regionMatches(this.tagStart, unexpectedTag, 0, unexpectedLength))
                {
                    if ((unexpectedTag == AIML_END) && (this.categoryCount == 0))
                    {
                        Log.userinfo("aiml element does not contain any AIML content in \"" + this.fileName + "\".",
                                Log.ERROR);
                    }
                    else
                    {
                        Log.userinfo(new String[]
                            { "Unexpected " + unexpectedTag + "; rest of file ignored.",
                                    "  (Line " + this.lineNumber + ", \"" + this.fileName + "\")" }, Log.ERROR);
                    }
                    this.done = true;
                    return;
                }
            }
            if (this.warnNonAIML && this.state == AIMLReader.S_IN_AIML)
            {
                int nextSpace = this.bufferString.indexOf(SPACE, this.tagStart);
                if (nextSpace > -1)
                {
                    String unexpectedTag = this.bufferString.substring(this.tagStart + 1, nextSpace);
                    if (unexpectedTag.indexOf(COLON) == -1 && !unexpectedTag.equals(COMMENT_MARK))
                    {
                        Log.userinfo(new String[]
                            { "There is no \"" + unexpectedTag + "\" element in AIML.",
                                    "  (Line " + this.lineNumber + ", \"" + this.fileName + "\")" }, Log.ERROR);
                    }
                }
            }
        }
    }

    /**
     * Prints a standard set of error info when a category read is aborted for
     * some reason.
     * 
     * @param reason
     *            the reason the category was aborted
     */
    private void abortCategory(String reason)
    {
        Log.userinfo(new String[]
            { "Aborting category:", reason,
                    "  (Category ends line " + this.lineNumber + ", \"" + this.fileName + "\")." }, STARTUP_AND_ERROR);
    }
}