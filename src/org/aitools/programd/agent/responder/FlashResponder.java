/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.agent.responder;

import java.io.File;
import java.util.HashMap;

import org.aitools.programd.util.Globals;
import org.aitools.programd.util.SuffixFilenameFilter;

/**
 * Implements a {@link Responder}for a Flash client.
 * 
 * @author Chris Fahey
 */
public class FlashResponder extends AbstractMarkupResponder
{
    /** A filename filter for finding html templates. */
    private static final SuffixFilenameFilter flashFilenameFilter = new SuffixFilenameFilter(new String[]
        { ".flash", ".data" });

    /** Map of template names to filenames. */
    private static HashMap templates;

    /** Location of html templates. */
    private static final String templatesDirectoryName = Globals.getProperty(
            "programd.responder.flash.template.directory", "templates" + File.separator + "flash");

    /** Path to the default chat template. */
    private static String chatTemplatePath = templatesDirectoryName + File.separator
            + Globals.getProperty("programd.responder.flash.template.chat-default", "chat.flash");

    /**
     * Scans and registers templates.
     */
    static
    {
        templates = registerTemplates(templatesDirectoryName, flashFilenameFilter);
    }

    public FlashResponder(String botidToRespond, String templateName)
    {
        super(botidToRespond);
        if (templateName.equals(EMPTY_STRING))
        {
            parseTemplate(chatTemplatePath);
        }
        else
        {
            // Otherwise, try to find the named template.
            String templateFileName = (String) templates.get(templateName);
            if (templateFileName != null)
            {
                parseTemplate(templateFileName);
            }
            else
            {
                parseTemplate(chatTemplatePath);
            }
        }
    }
}