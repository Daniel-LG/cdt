/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia   - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;

/**
 * Interface for codan marker resolution
 * 
 * @since 2.0
 */
public interface ICodanMarkerResolution extends IMarkerResolution {
	/**
	 * Override is extra checks is required to determine applicability of marker
	 * resolution
	 * 
	 * @param marker
	 * @return
	 */
	public boolean isApplicable(IMarker marker);
}
