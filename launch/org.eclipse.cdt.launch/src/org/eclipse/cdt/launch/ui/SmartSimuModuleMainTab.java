package org.eclipse.cdt.launch.ui;

import java.util.ArrayList;

import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import cn.smartcore.dev.ui.SmartSimuLabelProvider;
import cn.smartcore.dev.ui.natures.ProjectNature;

// added by jwy, to replace the default tab in "run" module, 
// add a filter to browse project button to make sure that only module projects are visible
public class SmartSimuModuleMainTab extends CMainTab2 {

	public SmartSimuModuleMainTab() {
		super();
	}

	public SmartSimuModuleMainTab(int flags) {
		super(flags);
	}

	@Override
	protected void handleProjectButtonSelected() {
		String currentProjectName = fProjText.getText();
		IProject project = chooseModuleProject();
		if (project == null) {
			return;
		}
		String projectName = project.getName();
		fProjText.setText(projectName);
		if (currentProjectName.length() == 0) {
			// New project selected for the first time, set the program name
			// default too.
			// IBinary[] bins = getBinaryFiles(project);
			// if (bins != null && bins.length == 1) {
			// fProgText.setText(bins[0].getResource().getProjectRelativePath().toOSString());
			// }
		}
	}

	private IProject chooseModuleProject() {
		IProject[] projects = getModuleProjects();
		ILabelProvider labelProvider = new SmartSimuLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(LaunchMessages.CMainTab_Project_Selection);
		dialog.setMessage(LaunchMessages.CMainTab_Choose_project_to_constrain_search_for_program);
		dialog.setElements(projects);
		IProject moduleProject = getModuleProject();
		if (moduleProject != null) {
			dialog.setInitialSelections(new Object[] { moduleProject });
		}
		if (dialog.open() == Window.OK) {
			return (IProject) dialog.getFirstResult();
		}
		return null;
	}

	private IProject getModuleProject() {
		String projectName = fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}

		IProject[] projects = getModuleProjects();
		for (IProject project : projects) {
			if (project.getName().equals(projectName)) {
				return project;
			}
		}

		return null;
	}

	private IProject[] getModuleProjects() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		ArrayList<IProject> list = new ArrayList<IProject>();

		for (IProject project : projects) {
			try {
				if (project.hasNature(ProjectNature.MODULE_PROJECT_ID)) {
					list.add(project);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return list.toArray(new IProject[list.size()]);
	}
}
