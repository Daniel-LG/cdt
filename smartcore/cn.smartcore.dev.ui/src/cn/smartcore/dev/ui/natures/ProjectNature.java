package cn.smartcore.dev.ui.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class ProjectNature implements IProjectNature {

	public static final String MODULE_PROJECT_ID = "cn.smartcore.dev.ui.ModuleProject";
	
	public static final String SIMULATOR_PROJECT_ID = "cn.smartcore.dev.ui.SimulatorProject";
	
	public static final String APP_PROJECT_ID = "cn.smartcore.dev.ui.APPProject";

	@Override
	public void configure() throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deconfigure() throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public IProject getProject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProject(IProject project) {
		// TODO Auto-generated method stub

	}

}
