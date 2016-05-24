package cn.smartcore.dev.ui.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import cn.smartcore.dev.ui.ProjectDecorator;
import cn.smartcore.dev.ui.natures.ProjectNature;

public class SimulatorLabelProvider extends LabelProvider {

	private WorkbenchLabelProvider fLableProvider = new WorkbenchLabelProvider();

	@Override
	public Image getImage(Object element) {
		if (element instanceof IProject) {
			if (((IProject) element).getName() == "Default Simulator") {
				return ImageDescriptor.createFromFile(ProjectDecorator.class, "/icons/view/simu.gif").createImage();
			}
			try {
				if (((IProject) element).hasNature(ProjectNature.SIMULATOR_PROJECT_ID)) {
					return ImageDescriptor.createFromFile(ProjectDecorator.class, "/icons/view/simu.gif").createImage();
				} else if (((IProject) element).hasNature(ProjectNature.MODULE_PROJECT_ID)) {
					return ImageDescriptor.createFromFile(ProjectDecorator.class, "/icons/view/module.gif")
							.createImage();
				} else {
					return ImageDescriptor.createFromFile(ProjectDecorator.class, "/icons/view/app.gif").createImage();
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IProject) {
			if (((IProject) element).getName() == "Default Simulator") {
				return "Default Simulator";
			}
			return fLableProvider.getText(element);
		}

		return null;
	}

}
