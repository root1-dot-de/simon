/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of SIMON.
 *
 *   SIMON is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SIMON is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SIMON.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.simon.utils;

import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


/**
 * This class formats the loggin-output for the console
 * 
 * @version 20060518 0920
 */

public class ConsoleLogFormatter extends Formatter
{

	private DecimalFormat df2 = new DecimalFormat( "00" );
	private DecimalFormat df3 = new DecimalFormat( "000" );
	private DecimalFormat df4 = new DecimalFormat( "0000" );

	private static final String CRLF = "\r\n";
	private GregorianCalendar calendar = new GregorianCalendar(); 

	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	public String format(LogRecord record)
	{
		StringBuilder output = new StringBuilder();

		calendar.setTimeInMillis(record.getMillis());
		
// Versuch das Datum deutsch darzustellen. Ist aber eher unnötig.	
				
		output.append(df4.format(calendar.get(GregorianCalendar.YEAR)));
		output.append("-");
		output.append(df2.format((calendar.get(GregorianCalendar.MONTH)+ 1)));
		output.append("-");
		output.append(df2.format(calendar.get(GregorianCalendar.DAY_OF_MONTH)));
		output.append(" ");
		output.append(df2.format(calendar.get(GregorianCalendar.DAY_OF_MONTH)));
		output.append(":");
		output.append(df2.format(calendar.get(GregorianCalendar.MINUTE)));
		output.append(":");
		output.append(df2.format(calendar.get(GregorianCalendar.SECOND)));
		output.append(",");
		output.append(df3.format(calendar.get(GregorianCalendar.MILLISECOND)));
		output.append(" [");
		output.append(record.getLevel().getName());
		output.append("\t] ");
		output.append(record.getLoggerName());
		output.append(".");
		output.append(record.getSourceMethodName());
		output.append(" -> ");
		output.append(record.getMessage());
		output.append(CRLF);

		return output.toString();
	}
}