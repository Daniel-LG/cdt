package cn.smartcore.dev.ui.wizards;

import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import cn.smartcore.dev.ui.natures.ProjectNature;

public class Util {

	static void addNature(IProject project, String nature) throws CoreException {
		if (!project.hasNature(nature)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = nature;
			description.setNatureIds(newNatures);

			IProgressMonitor monitor = null;
			project.setDescription(description, monitor);
		}
	}

	static void addNature(IProjectDescription description, String nature) throws CoreException {
		String[] prevNatures = description.getNatureIds();
		for (int i = 0; i < prevNatures.length; i++) {
			if (prevNatures[i].equals(nature)) {
				return;
			}
		}
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = nature;
		description.setNatureIds(newNatures);
	}
	
	// Adds a new file to the project.
	static void addFileToProject(IContainer container, Path path, InputStream contentStream, IProgressMonitor monitor)
			throws CoreException {
		final IFile file = container.getFile(path);
		if (file.exists()) {
			file.setContents(contentStream, true, true, monitor);
		} else {
			file.create(contentStream, true, monitor);
		}
	}
	
}
