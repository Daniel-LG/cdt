/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Freescale Semiconductor - refactoring
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.Platform;

/**
 * Some general utilities used by the DSF Disassembly view and its backends
 */
public class DisassemblyUtils {

	/**
	 * Trace option. The view started out and continues to be in DSF but
	 * backends can be non-DSF.
	 */
	public final static boolean DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.cdt.dsf.ui/debug/disassembly"));  //$NON-NLS-1$//$NON-NLS-2$
	
	public static String getAddressText(BigInteger address) {
		if (address == null) {
			return "<null>"; //$NON-NLS-1$
		}
		if (address.compareTo(BigInteger.ZERO) < 0) {
			return address.toString();
		}
		String hex = address.toString(16);
		return "0x" + "0000000000000000".substring(hex.length() + (address.bitLength() <= 32 ? 8 : 0)) + hex; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void internalError(Throwable e) {
		if (DEBUG) {
			System.err.println("Disassembly: Internal error"); //$NON-NLS-1$
			CDebugUIPlugin.log(e);
		}
	}
	public static BigInteger decodeAddress(String string) {
		if (string.startsWith("0x")) { //$NON-NLS-1$
			return new BigInteger(string.substring(2), 16);
		}
		return new BigInteger(string);
	}
}
