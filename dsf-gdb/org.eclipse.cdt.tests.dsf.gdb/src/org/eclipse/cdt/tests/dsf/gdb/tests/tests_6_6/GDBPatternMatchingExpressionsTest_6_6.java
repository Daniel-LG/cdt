/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_6;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.GDBPatternMatchingExpressionsTest;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class GDBPatternMatchingExpressionsTest_6_6 extends GDBPatternMatchingExpressionsTest {
    @Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_6_6);
	}
}
