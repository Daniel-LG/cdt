/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.ui.text.ISemanticToken;

/**
 * Semantic highlighting.
 * Cloned from JDT.
 *
 * @since 4.0
 */
public abstract class SemanticHighlighting {
	/**
	 * @return the preference key, will be augmented by a prefix and a suffix for each preference
	 */
	public abstract String getPreferenceKey();
	
	/**
	 * @return <code>true</code> if the highlighting is enabled by default
	 */
	public abstract boolean isEnabledByDefault();

	/**
	 * Indicates that the highlighting needs to visit implicit names
	 * (e.g. overloaded operators)
	 */
	public boolean requiresImplicitNames() {
		return false;
	}

	/**
	 * Returns <code>true</code> iff the semantic highlighting consumes the semantic token.
	 * <p>
	 * NOTE: Implementors are not allowed to keep a reference on the token or on any object
	 * retrieved from the token.
	 * </p>
	 *
	 * @param token the semantic token for a {@link org.eclipse.cdt.core.dom.ast.IASTName}
	 * @return <code>true</code> iff the semantic highlighting consumes the semantic token
	 */
	public abstract boolean consumes(ISemanticToken token);
}
