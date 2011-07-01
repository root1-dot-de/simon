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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * This class formats the loggin-output for the console
 *
 * @version 20060518 0920
 */
public class ConsoleLogFormatter extends Formatter {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm::ss.SSS");
    private static final String CRLF = "\r\n";

    /* (non-Javadoc)
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    public String format(LogRecord record) {

        StringBuffer output = new StringBuffer();

        output.append(simpleDateFormat.format(new Date(record.getMillis())));
//        output.append(" [");
//        output.append(record.getLevel().getName());

        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        java.util.Formatter formatter = new java.util.Formatter(sb, Locale.getDefault());
        formatter.format(" %-7s ", record.getLevel().getName());
        output.append(sb);

//        if (record.getLevel().getName().length() < 7) {
//            output.append("\t");
//        }

//        output.append("] ");
        output.append("tid=");
        output.append(record.getThreadID());
        output.append(" ");
        output.append(record.getSourceClassName());
        output.append(".");
        output.append(record.getSourceMethodName());
        output.append(": ");
        output.append(record.getMessage());
        output.append(CRLF);

        return output.toString();
    }
}