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