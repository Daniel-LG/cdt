/*******************************************************************************
 * Copyright (c) 2001 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.dom;

/**
 * @author jcamelon
 *
 */
public class BitField {


	public BitField( Declarator owner )
	{
		ownerDeclarator= owner;
	}
	private final Declarator ownerDeclarator; 
	
	/**
	 * @return
	 */
	public Declarator getOwnerDeclarator() {
		return ownerDeclarator;
	}

}
