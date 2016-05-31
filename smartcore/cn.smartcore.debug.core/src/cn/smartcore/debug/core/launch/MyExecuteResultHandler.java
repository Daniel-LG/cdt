package cn.smartcore.debug.core.launch;

import java.io.IOException;

import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import org.eclipse.ui.console.MessageConsoleStream;

import cn.smartcore.debug.core.ILaunchSimulator;

public class MyExecuteResultHandler extends DefaultExecuteResultHandler {

	private final ILaunchSimulator launchSimulator;

	private final MessageConsoleStream outputStream;

	private final MessageConsoleStream errorStream;

	public MyExecuteResultHandler(ILaunchSimulator launchSimulator, MessageConsoleStream outputStream,
			MessageConsoleStream errorStream) {
		this.launchSimulator = launchSimulator;
		this.outputStream = outputStream;
		this.errorStream = errorStream;
	}

	@Override
	public void onProcessComplete(int exitValue) {
		process();
		super.onProcessComplete(exitValue);
	}

	@Override
	public void onProcessFailed(ExecuteException e) {
		process();
		super.onProcessFailed(e);
	}

	private void process() {
		launchSimulator.setExecutor(null);
		launchSimulator.setFirstStart(true);
		// StartOrRestartProcessSequence_7_0.flag = true;
		outputStream.println("simu has been stoped");
		try {
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			errorStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		launchSimulator.updateControlGDBServerCommandState();
	}
}
