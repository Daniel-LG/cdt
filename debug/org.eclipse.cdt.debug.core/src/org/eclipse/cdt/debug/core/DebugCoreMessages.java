/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

public class DebugCoreMessages {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.core.DebugCoreMessages";//$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

	private DebugCoreMessages() {
	}

	static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getString(key), new String[]{arg});
	}

	static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), args);
	}

	public static String getString( String key ) {
		try {
			return RESOURCE_BUNDLE.getString( key );
		}
		catch( MissingResourceException e ) {
			return '!' + key + '!';
		}
	}
}
