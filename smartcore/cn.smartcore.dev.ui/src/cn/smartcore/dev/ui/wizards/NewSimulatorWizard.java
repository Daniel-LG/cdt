package cn.smartcore.dev.ui.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.wizards.CProjectWizard;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

import cn.smartcore.dev.ui.SmartSimuDevPlugin;
import cn.smartcore.dev.ui.messages.Messages;
import cn.smartcore.dev.ui.natures.ProjectNature;

/**
 * @author juwy
 * 
 *         We set simulator project as a C project so that we can make use of
 *         the internal builders in CDT, @see CommonBuilder#build. In the
 *         future, if you know how to implement your own builder in CDT, you may
 *         no longer need to extend the CProjectWizard
 */
public class NewSimulatorWizard extends CProjectWizard {

	private static final int SMARTSIMU_SIMULATOR = 2;

	public NewSimulatorWizard() {
		super(SMARTSIMU_SIMULATOR);
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
			Util.addNature(description, ProjectNature.SIMULATOR_PROJECT_ID);
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

		addNecessaryFiles(newProject);

		monitor.done();

		// Add the conf file
		// InputStream resourceStream = new
		// ByteArrayInputStream(("").getBytes());
		// Util.addFileToProject(newProject, new Path("simu.conf"),
		// resourceStream, monitor);
		// try {
		// resourceStream.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		return proj;
	}

	// copy the "main", "main_debug_info" and "memory_space/memory_space.so" in
	// the templates to the simulator project. We can not achieve it in the
	// template.xml possibly because in the process of "AddFiles", specified
	// strings will be replaced. If we do the replace for these files, their
	// content will be changed and further corrupted.
	private void addNecessaryFiles(IProject simulatorProject) {
		URL templateURL = SmartSimuDevPlugin.getBundleContext().getBundle().getResource("templates");
		String templatePath = null;
		try {
			templatePath = FileLocator.toFileURL(templateURL).getPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String mainPath = templatePath + File.separator + "main";
		String mainDebugPath = templatePath + File.separator + "main_debug_info";
		String memorySpacePath = templatePath + File.separator + "memory_space" + File.separator + "memory_space.so";

		// create the resources folder and the memory_space folder
		IFolder resources = simulatorProject.getFolder(new Path("resources"));
		if (!resources.exists()) {
			try {
				resources.create(true, true, null);
			} catch (CoreException e) {
				System.out.println("create resources folder error");
				System.out.println(e.getMessage());
			}
		}
		IFolder memorySpaceFolder = resources.getFolder(new Path("memory_space"));
		try {
			memorySpaceFolder.create(true, true, null);
		} catch (CoreException e) {
			System.out.println("create memory_space folder error");
			System.out.println(e.getMessage());
		}

		String projectResourcesPath = resources.getLocation().toOSString();
		try {
			copy(mainPath, projectResourcesPath + File.separator + "main");
		} catch (IOException e) {
			System.out.println("copy main file error");
			System.out.println(e.getMessage());
		}
		try {
			copy(mainDebugPath, projectResourcesPath + File.separator + "main_debug_info");
		} catch (IOException e) {
			System.out.println("copy main debug file error");
			System.out.println(e.getMessage());
		}
		try {
			copy(memorySpacePath,
					projectResourcesPath + File.separator + "memory_space" + File.separator + "memory_space.so");
		} catch (IOException e) {
			System.out.println("copy memory space file error");
			System.out.println(e.getMessage());
		}

	}

	private void copy(String oldPath, String newPath) throws IOException {
		InputStream is = null;
		FileOutputStream fos = null;

		try {
			is = new FileInputStream(oldPath);
			fos = new FileOutputStream(newPath);
			byte[] buffer = new byte[1024];
			int byteread = 0;
			while ((byteread = is.read(buffer)) != -1) {
				fos.write(buffer, 0, byteread);
			}
		} catch (IOException e) {
			System.out.println("error copy"); //$NON-NLS-1$
			e.printStackTrace();
		} finally {
			is.close();
			fos.close();
		}
	}

}
