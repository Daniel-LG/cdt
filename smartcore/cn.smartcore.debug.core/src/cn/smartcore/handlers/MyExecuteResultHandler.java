package cn.smartcore.handlers;

import java.io.IOException;

import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IEvaluationService;

public class MyExecuteResultHandler extends DefaultExecuteResultHandler {

	private final IWorkbenchWindow window;

	private final Command command;

	private final MessageConsoleStream outputStream;

	private final MessageConsoleStream errorStream;

	public MyExecuteResultHandler(IWorkbenchWindow window, Command command, MessageConsoleStream outputStream,
			MessageConsoleStream errorStream) {
		this.window = window;
		this.command = command;
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
		ControlGDBServerHandler.executor = null;
		ControlGDBServerHandler.isFirstStart = true;
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
		try {
			HandlerUtil.toggleCommandState(command);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		final IEvaluationService service = (IEvaluationService) window.getService(IEvaluationService.class);
		if (service != null)
			service.requestEvaluation("org.eclipse.ui.commands.toggleState");
	}
}
