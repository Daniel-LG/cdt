/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author dsteffle
 */
public class FileBasePluginTestCase extends TestCase {
    static NullProgressMonitor		monitor;
    static IWorkspace 				workspace;
    static IProject 				project;
    static FileManager 				fileManager;
    static int						numProjects;
    static Class					className;
	static ICProject cPrj;

    public FileBasePluginTestCase() {
	}

	public FileBasePluginTestCase(String name) {
		super(name);
	}

	private void initialize(Class aClassName) {
        if (CCorePlugin.getDefault() != null && CCorePlugin.getDefault().getCoreModel() != null) {
			//(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
			monitor = new NullProgressMonitor();

			workspace = ResourcesPlugin.getWorkspace();

	        try {
	        	cPrj = CProjectHelper.createCCProject("ParserTestProject", "bin", IPDOMManager.ID_NO_INDEXER); //$NON-NLS-1$ //$NON-NLS-2$
	            project = cPrj.getProject();

	            // ugly
	            if (className == null || !className.equals(aClassName)) {
	            	className = aClassName;
	            	numProjects++;
	            }
	        } catch (CoreException e) {
	            // Ignore
	        }
			if (project == null)
				throw new NullPointerException("Unable to create project"); //$NON-NLS-1$

			//Create file manager
			fileManager = new FileManager();
        }
    }

    public FileBasePluginTestCase(String name, Class className) {
    	super(name);
    	initialize(className);
    }

    public void cleanupProject() throws Exception {
    	numProjects--;

    	try {
    		if (numProjects == 0) {
    			project.delete(true, false, monitor);
    			project = null;
    		}
	    } catch (Throwable e) {
	        // Ignore
	    }
    }

    @Override
	protected void tearDown() throws Exception {
        if (project == null || !project.exists())
            return;

        IResource [] members = project.members();
        for (int i = 0; i < members.length; i++) {
            if (members[i].getName().equals(".project") || members[i].getName().equals(".cproject")) //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            if (members[i].getName().equals(".settings"))
            	continue;
            try {
                members[i].delete(false, monitor);
            } catch (Throwable e) {
                // Ignore
            }
        }
	}

    protected IFolder importFolder(String folderName) throws Exception {
    	IFolder folder = project.getProject().getFolder(folderName);

		// Create file input stream
		if (!folder.exists())
			folder.create(false, false, monitor);

		return folder;
    }

    public IFile importFile(String fileName, String contents) throws Exception {
		// Obtain file handle
		IFile file = project.getProject().getFile(fileName);

		InputStream stream = new ByteArrayInputStream(contents.getBytes());
		// Create file input stream
		if (file.exists()) {
		    file.setContents(stream, false, false, monitor);
		} else {
			file.create(stream, false, monitor);
		}

		fileManager.addFile(file);

		return file;
	}
}
