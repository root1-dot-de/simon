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
package de.root1.simon.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTester {
	
	private static final Logger logger = LoggerFactory.getLogger(LogTester.class);

	
	public static void main(String[] args) {
		
		Enumeration<String> loggerNames = LogManager.getLogManager().getLoggerNames();
		while (loggerNames.hasMoreElements()) {
			String string = (String) loggerNames.nextElement();
			System.out.println("Logger: '"+string+"'");
		}
		
		doLogTest();
		
//		// ------------------------------
//		
		InputStream is;
		File f = new File("config/simon_logging.properties");
		try {
			is = new FileInputStream(f);
			LogManager.getLogManager().readConfiguration(is);
			
			System.out.println("*******************");
			System.out.println("** Config Loaded **");
			System.out.println("*******************");
			System.out.flush();
			
			doLogTest();
			
			
		} catch (FileNotFoundException e) {
			
			System.err.println("File not found: "+f.getAbsolutePath()+".\n" +
					"If you don't want to debug SIMON, leave 'Utils.DEBUG' with false-value.\n" +
					"Otherwise you have to provide a Java Logging API conform properties-file like mentioned.");
			
		} catch (SecurityException e) {
			
			System.err.println("Security exception occured while trying to load "+f.getAbsolutePath()+"\n" +
					"Logging with SIMON not possible!.");
			
		} catch (IOException e) {
			
			System.err.println("Cannot load "+f.getAbsolutePath()+" ...\n" +
					"Please make sure that Java has access to that file.");
			
		}
		
	}


	private static void doLogTest() {
		if (logger.isDebugEnabled()) 	System.out.println("logger can log DEBUG");
		if (logger.isInfoEnabled()) 	System.out.println("logger can log INFO");
		if (logger.isWarnEnabled()) 	System.out.println("logger can log WARN");
		if (logger.isErrorEnabled()) 	System.out.println("logger can log ERROR");
		if (logger.isTraceEnabled()) 	System.out.println("logger can log TRACE");
		
		logger.debug(	"logger can log DEBUG");
		logger.info(	"logger can log INFO");
		logger.warn(	"logger can log WARN");
		logger.error(	"logger can log ERROR");
		logger.trace(	"logger can log TRACE");
		
		System.out.println("*******************");
		System.out.flush();

	}

}
