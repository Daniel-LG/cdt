package org.eclipse.cdt.launch.ui;

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import cn.smartcore.dev.ui.natures.ProjectNature;
import cn.smartcore.dev.ui.views.SimulatorLabelProvider;

/**
 * A launch configuration tab that displays and edits project and main type name
 * launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * 
 * @since 7.3
 */

// added by jwy, to replace the default main tab in "run", add a filter to browse
// project button to make sure that only application projects are visible; also,
// we add a textbox and a button to let user select the simulator in which the
// application runs
public class SmartSimuApplicationMainTab extends CMainTab2 {

	public SmartSimuApplicationMainTab() {
		super();
	}

	public SmartSimuApplicationMainTab(int flags) {
		super(flags);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		LaunchUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(),
				ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);
		createProjectGroup(comp, 1);
		createSimulatorGroup(comp, 1);
		createExeFileGroup(comp, 1);

		if (fIncludeBuildSettings) {
			createBuildOptionGroup(comp, 1);
		}
		createVerticalSpacer(comp, 1);
		if (fSpecifyCoreFile) {
			createCoreFileGroup(comp, 1);
		}

		LaunchUIPlugin.setDialogShell(parent.getShell());
	}

	@Override
	protected void handleProjectButtonSelected() {
		String currentProjectName = fProjText.getText();
		IProject project = chooseAPPProject();
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

	private IProject chooseAPPProject() {
		IProject[] projects = getAPPProjects();
		ILabelProvider labelProvider = new SimulatorLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(LaunchMessages.CMainTab_Project_Selection);
		dialog.setMessage(LaunchMessages.CMainTab_Choose_project_to_constrain_search_for_program);
		dialog.setElements(projects);
		IProject appProject = getAPPProject();
		if (appProject != null) {
			dialog.setInitialSelections(new Object[] { appProject });
		}
		if (dialog.open() == Window.OK) {
			return (IProject) dialog.getFirstResult();
		}
		return null;
	}

	private IProject getAPPProject() {
		String projectName = fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}

		IProject[] projects = getAPPProjects();
		for (IProject project : projects) {
			if (project.getName().equals(projectName)) {
				return project;
			}
		}

		return null;
	}

	private IProject[] getAPPProjects() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		ArrayList<IProject> list = new ArrayList<IProject>();

		for (IProject project : projects) {
			try {
				if (project.hasNature(ProjectNature.APP_PROJECT_ID)) {
					list.add(project);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return list.toArray(new IProject[list.size()]);
	}

	// added by jwy
	protected void createSimulatorGroup(Composite parent, int colSpan) {
		Composite projComp = new Composite(parent, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);
		fSimuLabel = new Label(projComp, SWT.NONE);
		fSimuLabel.setText(LaunchMessages.CMainTab_SimulatorColon);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fSimuLabel.setLayoutData(gd);
		fSimuText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fSimuText.setLayoutData(gd);
		fSimuText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				// if project changes, invalidate program name cache
				// fPreviouslyCheckedProgram = null;
				// TODO by jwy: 弄清其中原理，必须有下面这两个方法才能触发配置改变事件，为何?
				// updateBuildConfigCombo(EMPTY_STRING);
				updateLaunchConfigurationDialog();
			}
		});
		fSimuButton = createPushButton(projComp, LaunchMessages.Launch_common_Browse_1, null);
		fSimuButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleSimuButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});
	}

	// get idea from handleProjectButtonSelected()
	protected void handleSimuButtonSelected() {
		// String currentProjectName = fSimuText.getText();
		IProject project = chooseSimuProject();
		if (project == null) {
			return;
		}
		String projectName = project.getName();
		fSimuText.setText(projectName);
		// if (currentProjectName.length() == 0) {
		// New project selected for the first time, set the program name
		// default too.
		// IBinary[] bins = getBinaryFiles(project);
		// if (bins != null && bins.length == 1) {
		// fProgText.setText(bins[0].getResource().getProjectRelativePath().toOSString());
		// }
		// }
	}

	// get idea from chooseCProject()
	protected IProject chooseSimuProject() {
		IProject[] projects = getSimuProjects();
		ILabelProvider labelProvider = new SimulatorLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(LaunchMessages.CMainTab_Simulator_Selection);
		dialog.setMessage(LaunchMessages.CMainTab_Choose_simulator_to_constrain_search_for_program);
		dialog.setElements(projects);
		IProject simuProject = getSimuProject();
		if (simuProject != null) {
			dialog.setInitialSelections(new Object[] { simuProject });
		}
		if (dialog.open() == Window.OK) {
			return (IProject) dialog.getFirstResult();
		}
		return null;
	}

	protected IProject getSimuProject() {
		String projectName = fSimuText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}

		IProject[] projects = getSimuProjects();
		for (IProject project : projects) {
			if (project.getName().equals(projectName)) {
				return project;
			}
		}

		return null;
	}

	protected IProject[] getSimuProjects() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		ArrayList<IProject> list = new ArrayList<IProject>();

		for (IProject project : projects) {
			try {
				if (project.hasNature(ProjectNature.SIMULATOR_PROJECT_ID)) {
					list.add(project);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return list.toArray(new IProject[list.size()]);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		updateSimulatorFromConfig(config);
	}

	// added by jwy
	protected void updateSimulatorFromConfig(ILaunchConfiguration config) {
		String simulatorName = EMPTY_STRING;
		try {
			simulatorName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_SIMULATOR_NAME, EMPTY_STRING);
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce);
		}
		if (!fSimuText.getText().equals(simulatorName))
			fSimuText.setText(simulatorName);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		super.performApply(config);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_SIMULATOR_NAME, fSimuText.getText());
	}
}
