/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.StartupFileParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.UserError;

/**
 * The <code>listeners</code> element is a container for specifying parameters
 * of slisteners.
 */
public class ListenersProcessor extends StartupElementProcessor
{
    public static final String label = "listeners";

    public ListenersProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public void process(Element element, StartupFileParser parser)
    {
        // Does it have an href attribute?
        if (element.hasAttribute(HREF))
        {
            String href = element.getAttribute(HREF);
            try
            {
                parser.processResponse(new URI(href));
            } 
            catch (ProcessorException e)
            {
                throw new UserError(e.getMessage());
            }
			catch (URISyntaxException e)
			{
                throw new UserError(e.getMessage());
			}
            return;
        }
        // (otherwise...)
        parser.evaluate(element.getChildNodes());
    } 
}