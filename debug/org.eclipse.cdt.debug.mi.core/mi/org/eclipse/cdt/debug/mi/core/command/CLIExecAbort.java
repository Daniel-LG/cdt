/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -exec-finish
 *
 *  Asynchronous command.  Resumes the execution of the inferior program
 *  until the current function is exited.  Displays the results returned by
 *  the function.
 * 
 */
public class CLIExecAbort extends CLICommand 
{
	public CLIExecAbort() {
		super("kill"); //$NON-NLS-1$
	}
}
