/*******************************************************************************
 * Copyright (c) 2002, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * QNX Software Systems
 *******************************************************************************/

package org.eclipse.cdt.core.model;


/**
 * A C model region describes a hierarchical set of elements.
 * Regions are often used to describe a set of elements to be considered
 * when performing operations; for example, the set of elements to be
 * considered during a search. A region may include elements from different
 * projects.
 * <p>
 * When an element is included in a region, all of its children
 * are considered to be included. Children of an included element 
 * <b>cannot</b> be selectively excluded.
 * </p>
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IRegion {
	/**
	 * Adds the given element and all of its descendents to this region.
	 * If the specified element is already included, or one of its
	 * ancestors is already included, this has no effect. If the element
	 * being added is an ancestor of an element already contained in this
	 * region, the ancestor subsumes the descendent.
	 * 
	 * @param element the given element
	 */
	void add(ICElement element);
	/**
	 * Returns whether the given element is contained in this region.
	 * 
	 * @param element the given element
	 * @return true if the given element is contained in this region, false otherwise
	 */
	boolean contains(ICElement element);
	/**
	 * Returns the top level elements in this region.
	 * All descendents of these elements are also included in this region.
	 * 
	 * @return the top level elements in this region
	 */
	ICElement[] getElements();
	/**
	 * Removes the specified element from the region and returns
	 * <code>true</code> if successful, <code>false</code> if the remove
	 * fails. If an ancestor of the given element is included, the
	 * remove fails (in other words, it is not possible to selectively
	 * exclude descendants of included ancestors).
	 * 
	 * @param element the given element
	 * @return <code>true</code> if successful, <code>false</code> if the remove fails
	 */
	boolean remove(ICElement element);
}
