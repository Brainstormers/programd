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

import org.aitools.programd.Core;
import org.aitools.programd.parser.GenericParser;
import org.aitools.programd.parser.TemplateParser;

/**
 * An <code>AIMLProcessor</code> is responsible for processing a particular
 * AIML element.
 * 
 * @version 4.2
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author Noel Bush
 */
abstract public class AIMLProcessor extends Processor
{
    public AIMLProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public String process(Element element, GenericParser parser) throws ProcessorException
    {
        try
        {
            return process(element, (TemplateParser)parser);
        } 
        catch (ClassCastException e)
        {
            throw new ProcessorException("Tried to pass a non-TemplateParser to an AIMLProcessor.");
        } 
    } 

    abstract public String process(Element element, TemplateParser parser) throws AIMLProcessorException;
}