package cn.smartcore.dev.ui.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import cn.smartcore.dev.ui.SmartSimuDevPlugin;
import cn.smartcore.dev.ui.messages.Messages;
import cn.smartcore.dev.ui.natures.ProjectNature;

public class NewModuleWizard extends BasicNewResourceWizard {

	private ModuleProjectMainWizardPage fMainPage;

	private IConfigurationElement config;

	private IWorkbench workbench;

	private IStructuredSelection selection;

	private IProject project;

	public NewModuleWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		fMainPage = new ModuleProjectMainWizardPage("New SmartSimu Module Project");
		fMainPage.setDescription("Create a new SmartSimu Module Project.");
		fMainPage.setTitle("New SmartSimu Module Project");
		addPage(fMainPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		this.workbench = workbench;
	}

	@Override
	public boolean performFinish() {
		if (project != null) {
			return true;
		}

		final IProject projectHandle = fMainPage.getProjectHandle();
		URI projectURI = (!fMainPage.useDefaults()) ? fMainPage.getLocationURI() : null;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription desc = workspace.newProjectDescription(projectHandle.getName());
		desc.setLocationURI(projectURI);
		try {
			Util.addNature(desc, ProjectNature.MODULE_PROJECT_ID);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				createProject(desc, projectHandle, monitor);
			}
		};

		/*
		 * This isn't as robust as the code in the BasicNewProjectResourceWizard
		 * class. Consider beefing this up to improve error handling. Note: The
		 * first parameter should be false!!!
		 */
		try {
			getContainer().run(false, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}

		project = projectHandle;

		if (project == null) {
			return false;
		}

		String moduleConfPath = fMainPage.getModuleConfPath();
		try {
			project.setPersistentProperty(new QualifiedName(Messages.Qualifier, Messages.Property_1), moduleConfPath);
			project.setPersistentProperty(new QualifiedName(Messages.Qualifier, Messages.Property_2),
					moduleConfPath.replace(".conf", ".so"));
			project.setPersistentProperty(new QualifiedName(Messages.Qualifier, Messages.Property_3), moduleConfPath
					.substring(moduleConfPath.lastIndexOf(File.separator) + 1, moduleConfPath.lastIndexOf('.')));
		} catch (CoreException e) {
			e.printStackTrace();
		}

		BasicNewProjectResourceWizard.updatePerspective(config);
		BasicNewProjectResourceWizard.selectAndReveal(project, workbench.getActiveWorkbenchWindow());

		return true;
	}

	// Create the project in the workspace.
	void createProject(IProjectDescription description, IProject proj, IProgressMonitor monitor)
			throws CoreException, OperationCanceledException {
		try {
			monitor.beginTask("", 2000);
			proj.create(description, new SubProgressMonitor(monitor, 100));
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			proj.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 100));
		} catch (Exception ioe) {
			IStatus status = new Status(IStatus.ERROR, "NewFileWizard", IStatus.OK, ioe.getLocalizedMessage(), null);
			throw new CoreException(status);
		} finally {
			monitor.done();
		}
	}

}
