/**********************************************************************
 * Created on Mar 30, 2003
 *
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jcamelon
 *
 */
public class TemplateDeclaration extends Declaration implements IScope, IAccessable, ITemplateParameterListOwner {

	private final boolean exported;
	private AccessSpecifier visibility = null; 
	 
	private List declarations = new ArrayList(); 
	private TemplateParameterList templateParms = null;                

	public TemplateDeclaration( IScope ownerScope, boolean exported )
	{
		super( ownerScope );
		this.exported = exported;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IScope#addDeclaration(org.eclipse.cdt.internal.core.dom.Declaration)
	 */
	public void addDeclaration(Declaration declaration) {
		declarations.add( declaration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IScope#getDeclarations()
	 */
	public List getDeclarations() {
		return Collections.unmodifiableList( declarations );
	}

	/**
	 * @return boolean
	 */
	public boolean isExported() {
		return exported;
	}

	/**
	 * @return
	 */
	public TemplateParameterList getTemplateParms() {
		return templateParms;
	}

	/**
	 * @param list
	 */
	public void setTemplateParms(TemplateParameterList list) {
		templateParms = list;
	}


	/**
	 * @param token
	 */
	public void setFirstOffset(int startingOffset) {
		setStartingOffset( startingOffset );
	}

	/**
	 * @param token
	 */
	public void setLastOffset(int lastOffset) {
		setTotalLength( lastOffset - getStartingOffset() ); 
	}

	/**
	 * @return
	 */
	public int getVisibility() {
		if( visibility == null ) return AccessSpecifier.v_unknown;
		return visibility.getAccess();
	}

	/**
	 * @param specifier
	 */
	public void setVisibility(int visibility) {
		if( this.visibility == null ) this.visibility = new AccessSpecifier(visibility);
		else this.visibility.setAccess(visibility);
	}



}
