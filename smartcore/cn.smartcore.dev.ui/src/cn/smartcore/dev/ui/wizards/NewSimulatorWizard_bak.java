package cn.smartcore.dev.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
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

import cn.smartcore.dev.ui.natures.ProjectNature;

public class NewSimulatorWizard_bak extends Wizard implements INewWizard {

	// Use the WizardNewProjectCreationPage, which is provided by the Eclipse
	// framework.
	private WizardNewProjectCreationPage wizardPage;

	private IConfigurationElement config;

	private IWorkbench workbench;

	private IStructuredSelection selection;

	private IProject project;

	public NewSimulatorWizard_bak() {
		super();
		setNeedsProgressMonitor(true);
	}

	// Add pages to the wizard.
	@Override
	public void addPages() {
		wizardPage = new WizardNewProjectCreationPage("New Smart-Core Module Project");
		wizardPage.setDescription("Create a new Smart-Core Module Project.");
		wizardPage.setTitle("New Smart-Core Module Project");
		addPage(wizardPage);
		// testPage = new MyPage();
		// addPage(testPage);
	}

	// This method is called when 'Finish' button is pressed in the wizard.
	@Override
	public boolean performFinish() {
		if (project != null) {
			return true;
		}

		final IProject projectHandle = wizardPage.getProjectHandle();
		URI projectURI = (!wizardPage.useDefaults()) ? wizardPage.getLocationURI() : null;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription desc = workspace.newProjectDescription(projectHandle.getName());
		desc.setLocationURI(projectURI);
		try {
			addNature(desc);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		try {
			addBuilder(desc, "cn.smartcore.dev.ui.SimulatorBuilder");
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

		BasicNewProjectResourceWizard.updatePerspective(config);
		BasicNewProjectResourceWizard.selectAndReveal(project, workbench.getActiveWorkbenchWindow());

		// System.out.println(testPage.getText1());
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

			// now we have the project and we can do more things with it before
			// updating the perspective.
			IContainer container = (IContainer) proj;

			// Add the conf file
			InputStream resourceStream = new ByteArrayInputStream(("test").getBytes());
			addFileToProject(container, new Path("simu.conf"), resourceStream, monitor);
			resourceStream.close();

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

	private void addNature(IProjectDescription description) throws CoreException {
		String[] prevNatures = description.getNatureIds();
		for (int i = 0; i < prevNatures.length; i++) {
			if (prevNatures[i].equals(ProjectNature.SIMULATOR_PROJECT_ID)) {
				return;
			}
		}
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = ProjectNature.SIMULATOR_PROJECT_ID;
		description.setNatureIds(newNatures);
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

	private void addBuilder(IProject project, String id) throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		for (int i = 0; i < commands.length; ++i)
			if (commands[i].getBuilderName().equals(id))
				return;
		// add builder to project
		ICommand command = desc.newCommand();
		command.setBuilderName(id);
		ICommand[] nc = new ICommand[commands.length + 1];
		// Add it before other builders.
		System.arraycopy(commands, 0, nc, 1, commands.length);
		nc[0] = command;
		desc.setBuildSpec(nc);
		project.setDescription(desc, null);
	}

	// The ID should be the concatenation of the plug-in ID and the builder ID.
	// In other words, if the plug-ins ID is org.eclipse.escript.builder, and
	// the ID of the builder is Builder, the builder ID reference in the
	// .project file should be org.eclipse.escript.builder.Builder.
	private void addBuilder(IProjectDescription description, String id) throws CoreException {
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i)
			if (commands[i].getBuilderName().equals(id))
				return;
		// add builder to project
		ICommand command = description.newCommand();
		command.setBuilderName(id);
		ICommand[] nc = new ICommand[commands.length + 1];
		// Add it before other builders.
		System.arraycopy(commands, 0, nc, 1, commands.length);
		nc[0] = command;
		description.setBuildSpec(nc);
	}

}
