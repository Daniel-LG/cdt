package cn.smartcore.dev.ui.wizards;

import java.net.URI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.templateengine.SharedDefaults;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.ui.wizards.CProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import cn.smartcore.dev.ui.natures.ProjectNature;

// App project is exactly a C Project, just 
public class NewAPPWizard extends CProjectWizard {

	static final String SHARED_DEFAULTS_PREFIX_KEY = "prefix";

	static final String SHARED_DEFAULTS_PREFIX_VALUE = "sparc-rtems4.11-";

	private static final int SMARTSIMU_APP = 0;

	public NewAPPWizard() {
		super(SMARTSIMU_APP);
		/** 
		 * see @org.eclipse.cdt.internal.build.crossgcc.SetCrossCommandWizardPage
		 * Set the default "prefix"
		 */
		SharedDefaults.getInstance().getSharedDefaultsMap().put(SHARED_DEFAULTS_PREFIX_KEY,
				SHARED_DEFAULTS_PREFIX_VALUE);
	}

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
			// ֻ������������޸ģ�resourceChange����project���������д����ģ����֮�������nature��
			// resourceChange�Ĵ�������в����и�nature�����ModuleContent.resourceChange
			Util.addNature(description, ProjectNature.APP_PROJECT_ID);
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

		return proj;
	}

}
