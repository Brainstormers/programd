/*    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.aitools.programd.processor.loadtime;

import org.aitools.programd.parser.StartupFileParser;
import org.aitools.programd.parser.XMLNode;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.UserError;

/**
 *  The <code>listeners</code> element is a container
 *  for specifying parameters of slisteners.
 */
public class ListenersProcessor extends StartupElementProcessor
{
    public static final String label = "listeners";

    public String process(int level, XMLNode tag, StartupFileParser parser)
        throws InvalidStartupElementException
    {
        // Does it have an href attribute?
        String href = getHref(tag);

        if (href.length() > 0)
        {
            try
            {
                return parser.processResponse(
                    FileManager.getFileContents(href));
            }
            catch (ProcessorException e)
            {
                throw new UserError(e.getMessage());
            }
        }
        // (otherwise...)
        return parser.evaluate(level++, tag.XMLChild);
    }
}
