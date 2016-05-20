package cn.smartcore;

import org.apache.commons.exec.DefaultExecutor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;

public class WBListener implements IWorkbenchListener {

	public static DefaultExecutor executor;

	@Override
	public boolean preShutdown(IWorkbench workbench, boolean forced) {
		// ICommandService iCommandService =
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		// .getService(ICommandService.class);
		// Command command = iCommandService.getCommand("selectCommand");
		// try {
		// HandlerUtil.updateRadioState(command, "");
		// } catch (ExecutionException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		executor.getWatchdog().destroyProcess();
		return true;
	}

	@Override
	public void postShutdown(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
