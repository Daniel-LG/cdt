/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.macho.parser;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.core.runtime.IPath;


/**
 * @deprecated. Deprecated as of CDT 6.1. Use 64 bit version {@link MachOBinaryExecutable64}.
 * This class is planned for removal in next major release.
 */
@Deprecated
public class MachOBinaryExecutable extends MachOBinaryObject implements IBinaryExecutable {

	public MachOBinaryExecutable(IBinaryParser parser, IPath path) {
		super(parser, path, IBinaryFile.EXECUTABLE);
	}

}
