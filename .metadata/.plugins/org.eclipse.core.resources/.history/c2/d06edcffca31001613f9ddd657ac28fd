package org.eclipse.cdt.launch.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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

import cn.smartcore.dev.ui.SmartSimuLabelProvider;
import cn.smartcore.dev.ui.natures.ProjectNature;

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
		ILabelProvider labelProvider = new SmartSimuLabelProvider();
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
				// TODO by jwy: Ū������ԭ�������������������������ܴ������øı��¼���Ϊ��?
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
		ILabelProvider labelProvider = new SmartSimuLabelProvider();
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

		// add the default simulator
		list.add(0, new IProject() {
			
			@Override
			public boolean isConflicting(ISchedulingRule rule) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean contains(ISchedulingRule rule) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void touch(IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setSessionProperty(QualifiedName key, Object value) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setReadOnly(boolean readOnly) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setPersistentProperty(QualifiedName key, String value) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public long setLocalTimeStamp(long value) throws CoreException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setHidden(boolean isHidden) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setDerived(boolean isDerived, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setDerived(boolean isDerived) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void revertModificationStamp(long value) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor)
					throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isVirtual() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isTeamPrivateMember(int options) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isTeamPrivateMember() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isSynchronized(int depth) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isReadOnly() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isPhantom() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isLocal(int depth) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isLinked(int options) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isLinked() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isHidden(int options) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isHidden() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isDerived(int options) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isDerived() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isAccessible() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public IWorkspace getWorkspace() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getType() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Object getSessionProperty(QualifiedName key) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Map<QualifiedName, Object> getSessionProperties() throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public ResourceAttributes getResourceAttributes() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public URI getRawLocationURI() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IPath getRawLocation() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IPath getProjectRelativePath() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IProject getProject() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getPersistentProperty(QualifiedName key) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Map<QualifiedName, String> getPersistentProperties() throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IPathVariableManager getPathVariableManager() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IContainer getParent() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getName() {
				return "Default Simulator";
			}
			
			@Override
			public long getModificationStamp() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public IMarker getMarker(long id) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public URI getLocationURI() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IPath getLocation() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public long getLocalTimeStamp() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public IPath getFullPath() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getFileExtension() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth) throws CoreException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IMarker findMarker(long id) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean exists() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public IResourceProxy createProxy() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IMarker createMarker(String type) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void clearHistory(IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void accept(IResourceProxyVisitor visitor, int depth, int memberFlags) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void accept(IResourceVisitor visitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setDefaultCharset(String charset, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setDefaultCharset(String charset) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public IResource[] members(int memberFlags) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IResource[] members(boolean includePhantoms) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IResource[] members() throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IFolder getFolder(IPath path) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IResourceFilterDescription[] getFilters() throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IFile getFile(IPath path) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getDefaultCharset(boolean checkImplicit) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getDefaultCharset() throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IResource findMember(IPath path, boolean includePhantoms) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IResource findMember(String path, boolean includePhantoms) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IResource findMember(IPath path) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IResource findMember(String path) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean exists(IPath path) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public IResourceFilterDescription createFilter(int type, FileInfoMatcherDescription matcherDescription,
					int updateFlags, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void setDescription(IProjectDescription description, int updateFlags, IProgressMonitor monitor)
					throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setDescription(IProjectDescription description, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void saveSnapshot(int options, URI snapshotLocation, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void open(int updateFlags, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void open(IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void move(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void loadSnapshot(int options, URI snapshotLocation, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isOpen() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isNatureEnabled(String natureId) throws CoreException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean hasNature(String natureId) throws CoreException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean hasBuildConfig(String configName) throws CoreException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public IPath getWorkingLocation(String id) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IProject[] getReferencingProjects() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IProject[] getReferencedProjects() throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IBuildConfiguration[] getReferencedBuildConfigs(String configName, boolean includeMissing)
					throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IPath getPluginWorkingLocation(IPluginDescriptor plugin) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IProjectNature getNature(String natureId) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IFolder getFolder(String name) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IFile getFile(String name) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IProjectDescription getDescription() throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IContentTypeMatcher getContentTypeMatcher() throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IBuildConfiguration[] getBuildConfigs() throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IBuildConfiguration getBuildConfig(String configName) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IBuildConfiguration getActiveBuildConfig() throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void create(IProjectDescription description, int updateFlags, IProgressMonitor monitor)
					throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void create(IProjectDescription description, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void create(IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void close(IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void build(int kind, String builderName, Map<String, String> args, IProgressMonitor monitor)
					throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void build(IBuildConfiguration config, int kind, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void build(int kind, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub
				
			}
		});
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
