/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.util.Substituter;

/**
 * <p>
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-gender">gender</a></code>
 * element.
 * </p>
 * 
 * @version 4.2
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author Noel Bush
 */
public class GenderProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "gender";

    /** The map of substitutions to be performed on an input. */
    private static HashMap<String, String> substitutionMap = new HashMap<String, String>();

    /**
     * Creates a new GenderProcessor using the given Core.
     * @param coreToUse the Core object to use
     */
    public GenderProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    /**
     * @see org.aitools.programd.processor.AIMLProcessor#process(org.w3c.dom.Element, org.aitools.programd.parser.TemplateParser)
     */
    public String process(Element element, TemplateParser parser) throws AIMLProcessorException
    {
        if (element.getChildNodes().getLength() > 0)
        {
            // This looks ugly, but completely avoids a temporary variable.
            try
            {
                return parser.processResponse(applySubstitutions(parser.evaluate(element.getChildNodes()), parser
                        .getBotID()));
            } 
            catch (ProcessorException e)
            {
                throw (AIMLProcessorException) e;
            } 
        }
        // otherwise...
        return parser.shortcutTag(element, label, StarProcessor.label, Node.ELEMENT_NODE);
    } 

    /**
     * Applies substitutions as defined in the {@link #substitutionMap} .
     * Comparisons are case-insensitive.
     * 
     * @param input
     *            the input on which to perform substitutions
     * @param botid the botid whose substitutions should be applied
     * @return the input with substitutions performed
     */
    public String applySubstitutions(String input, String botid)
    {
        return Substituter.applySubstitutions(this.core.getBots().getBot(botid).getGenderSubstitutionsMap(), input);
    } 

    /**
     * Adds a substitution to the substitutions map. The <code>find</code>
     * parameter is stored in uppercase, to do case-insensitive comparisons. The
     * <code>replace</code> parameter is stored as is.
     * 
     * @param find
     *            the string to find in the input
     * @param replace
     *            the string with which to replace the found string
     */
    public static void addSubstitution(String find, String replace)
    {
        if (find != null && replace != null)
        {
            substitutionMap.put(find.toUpperCase(), replace);
        } 
    } 
}