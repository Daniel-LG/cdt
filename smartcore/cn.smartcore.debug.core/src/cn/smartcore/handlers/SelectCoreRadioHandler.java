package cn.smartcore.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.handlers.HandlerUtil;

public class SelectCoreRadioHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		// If we are already in the updated state, do nothing
		try {
			if (HandlerUtil.matchesRadioState(event)) {
				return null;
			}
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}

		String currentState = event.getParameter("org.eclipse.ui.commands.radioStateParameter");

		IProject project = ControlGDBServerHandler.project;

		IFile file = project.getFile("debug-config.txt");

		if (!file.exists()) {
			byte[] bytes = currentState.getBytes();
			InputStream source = new ByteArrayInputStream(bytes);
			try {
				file.create(source, true, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else {
			try {
				file.setContents(new ByteArrayInputStream(currentState.getBytes()), true, true, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		// Update the current state
		try {
			HandlerUtil.updateRadioState(event.getCommand(), currentState);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return null;
	}

}
