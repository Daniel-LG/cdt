/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;

/**
 * @author Emanuel Graf
 */
public class ExtractFunctionRefactoringRunner extends RefactoringRunner  {

	public ExtractFunctionRefactoringRunner(ICElement element, ISelection selection,
			IShellProvider shellProvider, ICProject cProject) {
		super(element, selection, shellProvider, cProject);
	}

	@Override
	public void run() {
		ExtractFunctionRefactoring refactoring =
				new ExtractFunctionRefactoring(element, selection, project);
		ExtractFunctionWizard wizard = new ExtractFunctionWizard(refactoring);
		run(wizard, refactoring, RefactoringSaveHelper.SAVE_REFACTORING);
	}
}
