/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.errorparsers.xlc.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.core.IMarkerGenerator;


public class TestError_1 extends TestCase {
	String err_msg;
	/**
	 * This function tests parseLine function of the
	 * XlcErrorParser class. Error message generated by
	 * xlc compiler with medium severity (E) is given as 
	 * input for testing.  
	 */
	public void testparseLine()
	{
		XlcErrorParserTester aix = new XlcErrorParserTester();
		aix.parseLine(err_msg);
		assertEquals("temp8.c", aix.getFileName(0));
		assertEquals(9, aix.getLineNumber(0));
		assertEquals(IMarkerGenerator.SEVERITY_ERROR_RESOURCE, aix.getSeverity(0));
		assertEquals("Missing argument(s).",aix.getMessage(0));
	}
	public TestError_1( String name)
	{
		super(name);
		err_msg = "\"temp8.c\", line 9.17: 1506-098 (E) "
					+ "Missing argument(s).";
	}
}