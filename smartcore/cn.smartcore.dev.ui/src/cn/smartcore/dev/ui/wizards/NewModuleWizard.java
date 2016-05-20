package cn.smartcore.dev.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.ui.wizards.CProjectWizard;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

import cn.smartcore.dev.ui.natures.ProjectNature;

// New Smart-Core Module wizard. Its role is to specify and create necessary files in the Smart-Core Module Project.
public class NewModuleWizard extends CProjectWizard {

	@Override
	public IProject createIProject(String name, URI location, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.CDTCommonProjectWizard_creatingProject, 100);

		if (newProject != null)
			return newProject;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		final IProject newProjectHandle = root.getProject(name);

		if (!newProjectHandle.exists()) {
			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			// 只能在这里进行修改，resourceChange是在project创建过程中触发的，如果之后再添加nature，
			// resourceChange的处理过程中不含有该nature，详见ModuleContent.resourceChange
			addNature(description);
			if (location != null)
				description.setLocationURI(location);
			newProject = CCorePlugin.getDefault().createCDTProject(description, newProjectHandle,
					new SubProgressMonitor(monitor, 25));
		} else {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};
			workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, new SubProgressMonitor(monitor, 25));
			newProject = newProjectHandle;
		}

		// Open the project if we have to
		if (!newProject.isOpen()) {
			newProject.open(new SubProgressMonitor(monitor, 25));
		}

		continueCreationMonitor = new SubProgressMonitor(monitor, 25);
		IProject proj = continueCreation(newProject);

		monitor.done();

		// Add the conf file
		InputStream resourceStream = new ByteArrayInputStream(("").getBytes());
		addFileToProject(newProject, new Path("module.conf"), resourceStream, monitor);
		try {
			resourceStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return proj;
	}

	private void addNature(IProject project) throws CoreException {
		if (!project.hasNature(ProjectNature.MODULE_PROJECT_ID)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = ProjectNature.MODULE_PROJECT_ID;
			description.setNatureIds(newNatures);

			IProgressMonitor monitor = null;
			project.setDescription(description, monitor);
		}
	}

	private void addNature(IProjectDescription description) throws CoreException {
		String[] prevNatures = description.getNatureIds();
		for (int i = 0; i < prevNatures.length; i++) {
			if (prevNatures[i].equals(ProjectNature.MODULE_PROJECT_ID)) {
				return;
			}
		}
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = ProjectNature.MODULE_PROJECT_ID;
		description.setNatureIds(newNatures);
	}

	// Adds a new file to the project.
	private void addFileToProject(IContainer container, Path path, InputStream contentStream, IProgressMonitor monitor)
			throws CoreException {
		final IFile file = container.getFile(path);
		if (file.exists()) {
			file.setContents(contentStream, true, true, monitor);
		} else {
			file.create(contentStream, true, monitor);
		}
	}
}