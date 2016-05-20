package cn.smartcore.dev.ui.views;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import cn.smartcore.dev.ui.natures.ProjectNature;

public class ModuleContentProvider implements ITreeContentProvider, IResourceChangeListener, IPreferenceChangeListener {

	/**
	 * Presentation of the content, i.e. for MakeView tree or for
	 * BuildTargetDialog table
	 */
	protected boolean bFlatten;

	protected StructuredViewer viewer;

	public ModuleContentProvider() {
		this(false);
	}

	/**
	 * Constructor.
	 *
	 * @param flat
	 *            - {@code true} for "flat" representation for a table
	 *            {@code false} to represent as a tree.
	 */
	public ModuleContentProvider(boolean flat) {
		bFlatten = flat;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (bFlatten) {
			List<Object> list = new ArrayList<Object>();
			Object[] children = getChildren(inputElement);
			for (int i = 0; i < children.length; i++) {
				list.add(children[i]);
				list.addAll(Arrays.asList(getElements(children[i])));
			}
			return list.toArray();
		}
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IWorkspaceRoot) {
			try {
				IProject[] projects = ((IWorkspaceRoot) parentElement).getProjects();
				ArrayList<Object> children = new ArrayList<Object>();
				for (IProject project : projects) {
					if (project.hasNature(ProjectNature.MODULE_PROJECT_ID)) {
						children.add(project);
					}
				}
				return children.toArray();
			} catch (CoreException e) {
				System.out.println(e.getMessage());
			}
		} else if (parentElement instanceof IProject) {
			IProject project = (IProject) parentElement;
			ArrayList<String> simictAttribute = new ArrayList<String>();
			ArrayList<String> simictAttributeInterface = new ArrayList<String>();
			ArrayList<String> simictInterface = new ArrayList<String>();
			IFile file = project.getFile("module.conf");

			if (file.exists()) {
				InputStream is = null;
				try {
					is = file.getContents();
				} catch (CoreException e) {
					e.printStackTrace();
				}
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);

				String str;
				try {
					while ((str = br.readLine()) != null) {
						if (str.indexOf("simict_attribute_interface") != -1) {
							simictAttributeInterface.add(str);
						} else if (str.indexOf("simict_attribute") != -1) {
							simictAttribute.add(str);
						} else {
							simictInterface.add(str);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Object[] children = new Object[simictAttribute.size() + simictAttributeInterface.size()
					+ simictInterface.size()];

			int index = 0;
			for (String str : simictAttribute) {
				children[index++] = str;
			}
			for (String str : simictAttributeInterface) {
				children[index++] = str;
			}
			for (String str : simictInterface) {
				children[index++] = str;
			}

			return children;
		}

		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		/*
		 * if (element instanceof IMakeTarget) { // this is ambiguous as make
		 * target can sit in 2 places, in its container // or source folder
		 * represented by TargetSourceContainer return
		 * ((IMakeTarget)obj).getContainer(); } else
		 */
		if (element instanceof IFile) {
			return ((IFile) element).getParent();
		} else if (element instanceof IContainer) {
			return ((IContainer) element).getParent();
		}
		/*
		 * else if (element instanceof TargetSourceContainer) { IContainer
		 * container = ((TargetSourceContainer)obj).getContainer(); //
		 * TargetSourceContainer sits at project root return
		 * container.getProject(); }
		 */

		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta == null) {
			return;
		}

//		Set<IProject> affectedProjects = new HashSet<IProject>();
//		boolean flag = collectAffectedProjects(delta, affectedProjects);

//		if (flag) {
			refreshView();
//			return;
//		}

		// If the view is being filtered or source roots shown,
		// adding/removing resources can structurally affect the tree
		// starting with the project
//		for (IProject project : affectedProjects) {
//			refreshProjectTree(project);
//		}

	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(CCorePreferenceConstants.SHOW_SOURCE_ROOTS_AT_TOP_LEVEL_OF_PROJECT)) {
			refreshView();
		}
	}

	private boolean collectAffectedProjects(IResourceDelta delta, Set<IProject> affectedProjects) {
		if (affectedProjects.contains(delta.getResource().getProject())) {
			return false;
		}

		for (IResourceDelta d : delta.getAffectedChildren(IResourceDelta.ADDED | IResourceDelta.REMOVED)) {
			// handle folders only, additions/removal of projects are dealt with
			// in #targetChanged(MakeTargetEvent)
			IResource rc = d.getResource();
			// if (rc.getType() == IResource.FOLDER || rc.getType() ==
			// IResource.PROJECT
			// || rc.getType() == IResource.ROOT) {
			IProject project = rc.getProject();
			// only think about module project
			try {
				if (!project.exists()) {
					return true;
				}
				if (project.hasNature(ProjectNature.MODULE_PROJECT_ID)) {
					if (rc.getType() == IResource.PROJECT) {
						return true;
					} else {
						affectedProjects.add(project);
						return false;
					}
				}
			} catch (CoreException e) {
				System.out.println(e.getMessage());
			}
			// }
		}

		for (IResourceDelta d : delta.getAffectedChildren(IResourceDelta.CHANGED)) {
			collectAffectedProjects(d, affectedProjects);
		}

		return false;
	}

	/**
	 * Refresh the project tree or the project subtree (in case of drill-down
	 * adapter) in the view.
	 */
	private void refreshProjectTree(final IProject project) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed())
					return;

				int itemCount = 0;
				if (viewer instanceof TreeViewer) {
					itemCount = ((TreeViewer) viewer).getTree().getItemCount();
				} else if (viewer instanceof TableViewer) {
					itemCount = ((TableViewer) viewer).getTable().getItemCount();
				}
				if (itemCount <= 0) {
					return;
				}

				Object firstItem = null;
				if (viewer instanceof TreeViewer) {
					firstItem = ((TreeViewer) viewer).getTree().getItem(0).getData();
				} else if (viewer instanceof TableViewer) {
					firstItem = ((TableViewer) viewer).getTable().getItem(0).getData();
				}

				IContainer parentContainer = null;

				boolean isDrilledDown = !(firstItem instanceof IProject);
				if (!isDrilledDown) {
					// view shows projects
					viewer.refresh(project);
				} else {
					// drill-down adapter in the game
					if (firstItem instanceof IResource) {
						parentContainer = ((IResource) firstItem).getParent();
					}
					/*
					 * else if (firstItem instanceof TargetSourceContainer) {
					 * parentContainer = ((TargetSourceContainer)
					 * firstItem).getContainer().getParent(); } else if
					 * (firstItem instanceof IMakeTarget) { parentContainer =
					 * ((IMakeTarget) firstItem).getContainer(); }
					 */

					if (parentContainer != null && project.equals(parentContainer.getProject())) {
						viewer.refresh();
					}
				}
			}
		});
	}

	/**
	 * Refresh the whole view.
	 */
	private void refreshView() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed())
					return;
				viewer.refresh();
			}
		});
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (StructuredViewer) viewer;
		IWorkspace oldWorkspace = null;
		IWorkspace newWorkspace = null;
		if (oldInput instanceof IWorkspace) {
			oldWorkspace = (IWorkspace) oldInput;
		} else if (oldInput instanceof IContainer) {
			oldWorkspace = ((IContainer) oldInput).getWorkspace();
		}

		if (newInput instanceof IWorkspace) {
			newWorkspace = (IWorkspace) newInput;
		} else if (newInput instanceof IContainer) {
			newWorkspace = ((IContainer) newInput).getWorkspace();
		}

		if (oldWorkspace != newWorkspace) {
			ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
			if (oldWorkspace != null) {
				InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).removePreferenceChangeListener(this);
				// mngr.removeCProjectDescriptionListener(this);
				oldWorkspace.removeResourceChangeListener(this);
			}
			if (newWorkspace != null) {
				newWorkspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
				// mngr.addCProjectDescriptionListener(this,
				// CProjectDescriptionEvent.APPLIED);
				InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).addPreferenceChangeListener(this);
			}
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
