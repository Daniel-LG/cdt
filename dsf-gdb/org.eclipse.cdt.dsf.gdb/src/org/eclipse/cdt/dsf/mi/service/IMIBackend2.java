/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.io.InputStream;

/**
 * This interface extends the IMIBackend service to obtain the error
 * stream of the backend process. (Bug 350837)
 * @since 4.1
 */

public interface IMIBackend2 extends IMIBackend {

    /**
     * Returns the backend error stream.
     */
    public InputStream getMIErrorStream();
}
