package cn.smartcore.dev.ui.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

// get idea from MakeLabelProvider
public class ModuleLabelProvider extends LabelProvider implements ITableLabelProvider {

	private WorkbenchLabelProvider fLableProvider = new WorkbenchLabelProvider();

	@Override
	public Image getImage(Object element) {
		if (element instanceof IProject) {
			return ImageDescriptor.createFromFile(ModuleLabelProvider.class, "/icons/view/module.gif").createImage();
		} else if (element instanceof String) {
			if (((String) element).contains("attribute_INTERFACE")) {
				return ImageDescriptor.createFromFile(ModuleLabelProvider.class, "/icons/view/attribute_interface.gif")
						.createImage();
			} else if (((String) element).contains("attribute")) {
				return ImageDescriptor.createFromFile(ModuleLabelProvider.class, "/icons/view/attribute.gif")
						.createImage();
			} else {
				return ImageDescriptor.createFromFile(ModuleLabelProvider.class, "/icons/view/interface.gif")
						.createImage();
			}
		} else {
			return fLableProvider.getImage(element);
		}
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IProject) {
			return fLableProvider.getText(element);
		} else if (element instanceof String) {
			return (String) element;
		}
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return columnIndex == 0 ? getImage(element) : null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		// what is the difference between 0 and 1?
		switch (columnIndex) {
		case 0:
			return getText(element);
		case 1:
			return getText(element);
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		super.dispose();
		fLableProvider.dispose();
	}

}
