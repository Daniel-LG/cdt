/*
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.core.runtime.CoreException;

/**
 * QGadgets are C++ classes that have been annotated with Qt marker macros.  This class is
 * used to introduce the QGadget to the Qt linkage.  The only feature of Q_GADGET is the
 * ability to host Q_ENUMs.
 */
public class QGadgetName extends AbstractQClassName {

	public QGadgetName(ICPPASTCompositeTypeSpecifier spec) {
		super(spec);
	}

	@Override
	protected QtPDOMBinding createPDOMBinding(QtPDOMLinkage linkage, IASTName name) throws CoreException {
		return new QtPDOMQGadget(linkage, this, name);
	}

	@Override
	protected IASTName copy(CopyStyle style, ICPPASTCompositeTypeSpecifier spec) {
		return new QGadgetName(spec);
	}
}
