/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.pty;


import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.utils.pty.PTY.MasterFD;
import org.eclipse.core.runtime.Platform;

public class PTYOutputStream extends OutputStream {

	private static final byte EOT = '\4';

	MasterFD master;

	/**
	 * From a Unix valid file descriptor set a Reader.
	 * @param fd file descriptor.
	 */
	public PTYOutputStream(MasterFD fd) {
		master = fd;
	}

	/**
	 * @see OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (
			(off < 0)
				|| (off > b.length)
				|| (len < 0)
				|| ((off + len) > b.length)
				|| ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		byte[] tmpBuf = new byte[len];
		System.arraycopy(b, off, tmpBuf, 0, len);
		write0(master.getFD(), tmpBuf, len);
	}
	/**
	 * Implementation of read for the InputStream.
	 *
	 * @exception IOException on error.
	 */
	@Override
	public void write(int b) throws IOException {
		byte[] buf = new byte[1];
		buf[0] = (byte) b;
		write(buf, 0, 1);
	}

	/**
	 * Close the Reader
	 * @exception IOException on error.
	 */
	@Override
	public void close() throws IOException {
		if (master.getFD() == -1)
			return;
		// For non-windows platforms, send EOT instead of closing
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			int status = close0(master.getFD());
			if (status == -1)
				throw new IOException("close error"); //$NON-NLS-1$
			master.setFD(-1);
		} else {
			write(EOT);
		}
	}

	@Override
	protected void finalize() throws IOException {
		close();
	}

	private native int write0(int fd, byte[] b, int len) throws IOException;
	private native int close0(int fd) throws IOException;

	static {
		System.loadLibrary("pty"); //$NON-NLS-1$
	}

}
