package cn.smartcore.debug.core;

import org.apache.commons.exec.DefaultExecutor;
import org.eclipse.core.resources.IProject;

public interface ILaunchSimulator {

	boolean isFirstStart();
	
	void setFirstStart(boolean isFirstStart);
	
	DefaultExecutor getExecutor();
	
	void setExecutor(DefaultExecutor executor);
	
	IProject getProject();

	void setProject(IProject project);

	void launch(String binFilePath, String simulatorProjectName, String port, String coreConfigPath);

	void updateControlGDBServerCommandState();
	
}
