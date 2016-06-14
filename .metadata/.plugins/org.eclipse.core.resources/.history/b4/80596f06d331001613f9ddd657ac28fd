package cn.smartcore.dev.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.IWizardWithMemory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import cn.smartcore.dev.ui.SmartCoreDevPlugin;
import cn.smartcore.dev.ui.messages.Messages;

public class ModuleProjectMainWizardPage extends WizardNewProjectCreationPage {

	private String moduleConfigPath;

	// Widgets
	private Tree tree;

	/**
	 * Creates a new project creation wizard page.
	 *
	 * @param pageName
	 *            the name of this page
	 */
	public ModuleProjectMainWizardPage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		createDynamicGroup((Composite) getControl());
		updateData(tree);

		setPageComplete(validatePage());
		setErrorMessage(null);
		setMessage(null);
	}

	private void createDynamicGroup(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.setLayout(new GridLayout(1, true));

		Label l1 = new Label(c, SWT.NONE);
		l1.setText(Messages.MainWizardPage_0);
		l1.setFont(parent.getFont());
		l1.setLayoutData(new GridData(GridData.BEGINNING));

		tree = new Tree(c, SWT.SINGLE | SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] tis = tree.getSelection();
				if (tis == null || tis.length == 0)
					return;
				// switchTo((CWizardHandler) tis[0].getData(), (EntryDescriptor)
				// tis[0].getData(DESC));
				setPageComplete(validatePage());
			}
		});
		tree.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				for (int i = 0; i < tree.getItemCount(); i++) {
					if (tree.getItem(i).getText().equals(e.result))
						return;
				}
				e.result = Messages.MainWizardPage_0;
			}
		});
	}

	public URI getProjectLocation() {
		return useDefaults() ? null : getLocationURI();
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *         <code>false</code> if at least one is invalid
	 */
	@Override
	protected boolean validatePage() {
		setMessage(null);
		if (!super.validatePage())
			return false;

		if (getProjectName().indexOf('#') >= 0) {
			setErrorMessage(Messages.ErrorMsg_1);
			return false;
		}

		boolean bad = true; // should we treat existing project as error

		IProject handle = getProjectHandle();
		if (handle.exists()) {
			if (getWizard() instanceof IWizardWithMemory) {
				IWizardWithMemory w = (IWizardWithMemory) getWizard();
				if (w.getLastProjectName() != null && w.getLastProjectName().equals(getProjectName()))
					bad = false;
			}
			if (bad) {
				setErrorMessage(Messages.ErrorMsg_2);
				return false;
			}
		}

		if (bad) { // Skip this check if project already created
			try {
				IFileStore fs;
				URI p = getProjectLocation();
				if (p == null) {
					fs = EFS.getStore(ResourcesPlugin.getWorkspace().getRoot().getLocationURI());
					fs = fs.getChild(getProjectName());
				} else
					fs = EFS.getStore(p);
				IFileInfo f = fs.fetchInfo();
				if (f.exists()) {
					if (f.isDirectory()) {
						if (f.getAttribute(EFS.ATTRIBUTE_READ_ONLY)) {
							setErrorMessage(Messages.ErrorMsg_3);
							return false;
						} else
							setMessage(Messages.ErrorMsg_4, IMessageProvider.WARNING);
					} else {
						setErrorMessage(Messages.ErrorMsg_5);
						return false;
					}
				}
			} catch (CoreException e) {
				CUIPlugin.log(e.getStatus());
			}
		}

		if (!useDefaults()) {
			IStatus locationStatus = ResourcesPlugin.getWorkspace().validateProjectLocationURI(handle,
					getLocationURI());
			if (!locationStatus.isOK()) {
				setErrorMessage(locationStatus.getMessage());
				return false;
			}
		}

		if (tree.getItemCount() == 0) {
			setErrorMessage(Messages.ErrorMsg_6);
			return false;
		}

		TreeItem[] selectedItems = tree.getSelection();
		if (selectedItems.length != 1) {
			setErrorMessage(Messages.ErrorMsg_7);
			return false;
		}

		TreeItem item = selectedItems[0];
		if (((File) item.getData("file")).isDirectory()) {
			setErrorMessage(null);
			return false;
		}

		this.moduleConfigPath = ((File) item.getData("file")).getAbsolutePath();
		setErrorMessage(null);
		return true;
	}

	public static CWizardHandler updateData(Tree tree) {
		tree.removeAll();

		URL simulatorURL = SmartCoreDevPlugin.getBundleContext().getBundle().getResource("modules");
		String modulesPath = null;
		try {
			modulesPath = FileLocator.toFileURL(simulatorURL).getPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File moduleFiles = new File(modulesPath);

		addItemsToTree(tree, moduleFiles);

		return null;
	}

	protected static void addItemsToTree(Tree tree, File root) {
		File[] files = root.listFiles();

		for (File file : files) {
			if (file.isDirectory()) {
				TreeItem treeItem = new TreeItem(tree, SWT.NONE);
				treeItem.setText(file.getName());
				treeItem.setData("file", file);
				treeItem.setImage(calcImage(file));
				addItemsToTree(treeItem, file);
			}
		}

		for (File file : files) {
			if (file.isFile()) {
				TreeItem treeItem = new TreeItem(tree, SWT.NONE);
				treeItem.setText(file.getName());
				treeItem.setData("file", file);
				treeItem.setImage(calcImage(file));
			}
		}
	}

	private static void addItemsToTree(TreeItem tree, File root) {
		File[] files = root.listFiles();

		for (File file : files) {
			if (file.isDirectory()) {
				TreeItem treeItem = new TreeItem(tree, SWT.NONE);
				treeItem.setText(file.getName());
				treeItem.setData("file", file);
				treeItem.setImage(calcImage(file));
				addItemsToTree(treeItem, file);
			}
		}

		for (File file : files) {
			if (file.isFile()) {
				TreeItem treeItem = new TreeItem(tree, SWT.NONE);
				treeItem.setText(file.getName());
				treeItem.setData("file", file);
				treeItem.setImage(calcImage(file));
			}
		}
	}

	private static Image calcImage(File file) {
		if (file.isDirectory())
			return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_SEARCHFOLDER);
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_VARIABLE);
	}

	public String getModuleConfPath() {
		return moduleConfigPath;
	}

}
