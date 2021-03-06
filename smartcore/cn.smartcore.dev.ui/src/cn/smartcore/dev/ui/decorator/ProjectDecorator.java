package cn.smartcore.dev.ui.decorator;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import cn.smartcore.dev.ui.messages.Messages;
import cn.smartcore.dev.ui.natures.ProjectNature;

public class ProjectDecorator implements ILabelDecorator {

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	// @Override
	public void decorate(Object element, IDecoration decoration) {
		try {
			if (((IProject) element).hasNature(ProjectNature.MODULE_PROJECT_ID)) {
				decoration.addOverlay(ImageDescriptor.createFromFile(ProjectDecorator.class, "/icons/sample.gif"),
						IDecoration.TOP_RIGHT);
				decoration.addSuffix(" [Smart Core Module]");
			} else if (((IProject) element).hasNature(ProjectNature.SIMULATOR_PROJECT_ID)) {
				decoration.addOverlay(ImageDescriptor.createFromFile(ProjectDecorator.class, "/icons/night.png"),
						IDecoration.TOP_RIGHT);
				decoration.addSuffix(" [Smart Core Simulator]");
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		try {
			if (((IProject) element).hasNature(ProjectNature.MODULE_PROJECT_ID)) {
				return ImageDescriptor.createFromFile(ProjectDecorator.class, "/icons/view/module.gif").createImage();
			} else if (((IProject) element).hasNature(ProjectNature.SIMULATOR_PROJECT_ID)) {
				return ImageDescriptor.createFromFile(ProjectDecorator.class, "/icons/view/simu.gif").createImage();
			} else if (((IProject) element).hasNature(ProjectNature.APP_PROJECT_ID)) {
				return ImageDescriptor.createFromFile(ProjectDecorator.class, "/icons/view/app.gif").createImage();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return image;
	}

	@Override
	public String decorateText(String text, Object element) {
		try {
			if (((IProject) element).hasNature(ProjectNature.MODULE_PROJECT_ID)) {
				String moduleConfigPath = ((IProject) element)
						.getPersistentProperty(new QualifiedName(Messages.Qualifier, Messages.Property_1));
				if (moduleConfigPath != null) {
					// moduleConfigPath=/xx/xx/xx/moduleType.conf
					String moduleType = moduleConfigPath.substring(moduleConfigPath.lastIndexOf(File.separator) + 1,
							moduleConfigPath.lastIndexOf('.'));
					return text + " [Module Type:" + moduleType + "]";
				}
				return text + " [Module Type:Unknown]";
			} else if (((IProject) element).hasNature(ProjectNature.SIMULATOR_PROJECT_ID)) {
				return text + " [Simulator]";
			} else if (((IProject) element).hasNature(ProjectNature.APP_PROJECT_ID)) {
				return text + " [Application]";
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return text;
	}

}
