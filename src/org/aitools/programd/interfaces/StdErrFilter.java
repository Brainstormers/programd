/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This filter allows only {@link java.util.logging.Level#WARNING Level.WARNING}
 * and {@link java.util.logging.Level#SEVERE Level.SEVERE} records.
 * 
 * @author Noel Bush
 * @since 4.2
 */
public class StdErrFilter implements StdFilter
{
    /** For comparison, the value of {@link java.util.logging.Level#WARNING Level.WARNING}. */
    private static int warningLevel = Level.WARNING.intValue();
    
    /**
     * @see java.util.logging.Filter#isLoggable(java.util.logging.LogRecord)
     * @return <code>true</code> if the record level is WARNING or greater, false otherwise
     */
    public boolean isLoggable(LogRecord record)
    {
        return (record.getLevel().intValue() >= warningLevel);
    }
}
