/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     ERicsson             - Updated
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * 
 *      -environment-directory [-r] PATHDIR
 *
 *   Add directory PATHDIR to beginning of search path for source files.
 *   -r will first reset the path to its default
 * 
 */
public class MIEnvironmentDirectory extends MICommand<MIInfo> {
	
	public MIEnvironmentDirectory(IDMContext ctx, String[] paths, boolean reset) {
		super(ctx, "-environment-directory", paths); //$NON-NLS-1$

		if (reset) {
			setOptions(new String[] {"-r"}); //$NON-NLS-1$
		}
	}
}