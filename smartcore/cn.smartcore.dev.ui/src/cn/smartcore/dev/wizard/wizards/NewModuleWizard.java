package cn.smartcore.dev.wizard.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

// New Smart-Core Module wizard. Its role is to specify and create necessary files in the Smart-Core Module Project.
public class NewModuleWizard extends Wizard implements INewWizard {

	// Use the WizardNewProjectCreationPage, which is provided by the Eclipse
	// framework.
	private WizardNewProjectCreationPage wizardPage;

	private IConfigurationElement config;

	private IWorkbench workbench;

	private IStructuredSelection selection;

	private IProject project;

	public NewModuleWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	// Add pages to the wizard.
	public void addPages() {
		wizardPage = new WizardNewProjectCreationPage("New Smart-Core Module Project");
		wizardPage.setDescription("Create a new Smart-Core Module Project.");
		wizardPage.setTitle("New Smart-Core Module Project");
		addPage(wizardPage);
		// testPage = new MyPage();
		// addPage(testPage);
	}

	// This method is called when 'Finish' button is pressed in the wizard.
	public boolean performFinish() {
		if (project != null) {
			return true;
		}

		final IProject projectHandle = wizardPage.getProjectHandle();
		URI projectURI = (!wizardPage.useDefaults()) ? wizardPage.getLocationURI() : null;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription desc = workspace.newProjectDescription(projectHandle.getName());
		desc.setLocationURI(projectURI);

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				createProject(desc, projectHandle, monitor, "aaa");
			}
		};

		/*
		 * This isn't as robust as the code in the BasicNewProjectResourceWizard
		 * class. Consider beefing this up to improve error handling. Note: The first
		 * parameter should be false!!!
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

		BasicNewProjectResourceWizard.updatePerspective(config);
		BasicNewProjectResourceWizard.selectAndReveal(project, workbench.getActiveWorkbenchWindow());

		// System.out.println(testPage.getText1());
		return true;
	}

	// Create the project in the workspace.
	void createProject(IProjectDescription description, IProject proj, IProgressMonitor monitor, String info)
			throws CoreException, OperationCanceledException {
		try {
			monitor.beginTask("", 2000);
			proj.create(description, new SubProgressMonitor(monitor, 100));

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			proj.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 100));

			// now we have the project and we can do more things with it before
			// updating the perspective.
			IContainer container = (IContainer) proj;

			// Add the src folder.
			final IFolder srcFolder = container.getFolder(new Path("src"));
			srcFolder.create(true, true, monitor);

			// Add the xxx.c file
			// InputStream resourceStream = new ByteArrayInputStream(
			// ("#include <stdio.h>\nint main(){\n\tprintln(\"" + info +
			// "\");\n\treturn 0;\n}").getBytes());
			// addFileToProject(container, new Path(srcFolder.getName() +
			// Path.SEPARATOR + info + ".c"), resourceStream,
			// monitor);
			// resourceStream.close();

			// Add the images folder.
			// IFolder imageFolder = container.getFolder(new Path("images"));
			// imageFolder.create(true, true, monitor);
		} catch (Exception ioe) {
			IStatus status = new Status(IStatus.ERROR, "NewFileWizard", IStatus.OK, ioe.getLocalizedMessage(), null);
			throw new CoreException(status);
		} finally {
			monitor.done();
		}
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

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		this.workbench = workbench;
	}

	/**
	 * Sets the initialization data for the wizard.
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		this.config = config;
	}
}