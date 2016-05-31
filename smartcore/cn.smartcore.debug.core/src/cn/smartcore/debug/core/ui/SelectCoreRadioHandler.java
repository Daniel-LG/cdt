package cn.smartcore.debug.core.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import cn.smartcore.debug.core.Activator;
import cn.smartcore.debug.core.ILaunchSimulator;
import cn.smartcore.debug.core.launch.LaunchSimulator;

public class SelectCoreRadioHandler extends AbstractHandler {

	private ILaunchSimulator launchSimulator;

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

		getLaunchSimulator();
		String currentState = event.getParameter("org.eclipse.ui.commands.radioStateParameter");
		IProject project = launchSimulator.getProject();
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

	private void getLaunchSimulator() {
		if (launchSimulator == null) {
			BundleContext context = Activator.getBundleContext();
			ServiceReference[] references = null;
			try {
				references = context.getServiceReferences(ILaunchSimulator.class.getName(),
						"(ServiceName=LaunchSimulator)");
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
			}
			if (references != null && references.length > 0) {
				launchSimulator = (ILaunchSimulator) context.getService(references[0]);
			}
		}
	}

}
