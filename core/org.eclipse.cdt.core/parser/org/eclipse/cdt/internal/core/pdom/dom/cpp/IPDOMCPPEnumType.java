/*******************************************************************************
 * Copyright (c) 2010, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;

/**
 * Interface that allows to implement a class-scope.
 */
public interface IPDOMCPPEnumType extends ICPPEnumeration, IPDOMBinding, IIndexType {
	/**
	 * Returns the scope name, for use in {@link IScope#getScopeName()}
	 */
	IIndexName getScopeName();
	
	@Override
	IEnumerator[] getEnumerators();

	/**
	 * Called by the scope to access the enumerators.
	 * Returns the enumerators in the order of declaration.
	 */
	void loadEnumerators(List<IPDOMCPPEnumerator> enumerators);
}
