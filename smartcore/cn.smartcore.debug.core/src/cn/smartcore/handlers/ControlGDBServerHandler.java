package cn.smartcore.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IEvaluationService;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ControlGDBServerHandler extends AbstractHandler {

	public static DefaultExecutor executor;

	static IProject project;

	private static final String CONSOLE_NAME = "Smart Core Debug Info";

	@SuppressWarnings("unused")
	private static final String GDBSERVER_ADDRESS = "127.0.0.1";

	private static final String GDBSERVER_PORT = "2345";
	
	public static IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

	public static final MessageConsole smartsimuConsole = findConsole(CONSOLE_NAME);

	public static boolean isFirstStart = true;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command command = event.getCommand();
		boolean oldValue = HandlerUtil.toggleCommandState(command);
		

		// use the old value and perform the operation
		if (oldValue) {
			if (executor != null)
				executor.getWatchdog().destroyProcess();
			HandlerUtil.toggleCommandState(command);
			// executor = null;
		} else {
			project = getProject(window);
			if (project == null) {
				MessageDialog.openInformation(window.getShell(), "Error", "no project selected");
				HandlerUtil.toggleCommandState(command);
				return null;
			}

			String line = null;
			// String debugFilePath = null;
			File debugDirectory = new File(project.getLocation() + "/Debug/");
			if (!debugDirectory.exists()) {
				MessageDialog.openInformation(window.getShell(), "Error", "no debug file");
				HandlerUtil.toggleCommandState(command);
				return null;
			}

			String homePath = System.getProperty("user.home");
			File[] files = debugDirectory.listFiles();
			for (File f : files) {
				if (f.isFile() && f.getName().equals("a.bin")) {
					// debugFilePath = f.getAbsolutePath();
					// line = "gdbserver :" + GDBSERVER_PORT + " " +
					// debugFilePath;
					try {
						copy(f, homePath + "/simu/ram/a.bin");
					} catch (IOException e) {
						// System.out.println("error copy");
						MessageDialog.openInformation(window.getShell(), "Error", "error copy");
						HandlerUtil.toggleCommandState(command);
						return null;
					}
					line = homePath + "/simu/main 8888 " + project.getLocation() + "/debug-config.txt start " + homePath
							+ "/simu/conf/conf.so";
					break;
				}
			}

			if (line == null) {
				MessageDialog.openInformation(window.getShell(), "Error", "no debug file");
				HandlerUtil.toggleCommandState(command);
				return null;
			}

			// the following codes chould not be executed before getting the
			// project through SelectionService, I dont have a deep research of
			// it, in my view, maybe the display() method changes the focus to
			// the console and the SelectionService no longer works on Package
			// Explorer
			IWorkbenchPage page = window.getActivePage();

			String id = IConsoleConstants.ID_CONSOLE_VIEW;
			IConsoleView view = null;
			try {
				view = (IConsoleView) page.showView(id);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			view.display(smartsimuConsole);

			CommandLine cmdLine = CommandLine.parse(line);
			executor = new DefaultExecutor();
			ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
			executor.setWatchdog(watchdog);
			MessageConsoleStream outputStream = smartsimuConsole.newMessageStream();
			MessageConsoleStream errorStream = smartsimuConsole.newMessageStream();
			errorStream.setColor(new Color(null, 255, 0, 0));
			outputStream.setActivateOnWrite(true);
			errorStream.setActivateOnWrite(true);
			PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
			executor.setStreamHandler(streamHandler);
			MyExecuteResultHandler resultHandler = new MyExecuteResultHandler(window, command, outputStream, errorStream);

			smartsimuConsole.clearConsole();

			try {
				executor.execute(cmdLine, resultHandler);
			} catch (ExecuteException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			try {
				resultHandler.waitFor(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				ExecuteException e = resultHandler.getException();
				String exceptionInfo = e.getMessage();
				errorStream.println(exceptionInfo);
				return null;
			} catch (IllegalStateException e) {
				outputStream.println("simu has been started");
			}

			// Read debug-config.txt and update the state of the radio command
			ICommandService commandService = window.getWorkbench().getService(ICommandService.class);
			Command radioCommand = commandService.getCommand("selectCommand");
			IFile file = project.getFile("debug-config.txt");
			String initialState = "";

			if (file.exists()) {
				try {
					BufferedReader bReader = new BufferedReader(new InputStreamReader(file.getContents()));
					try {
						initialState = bReader.readLine();
						if (initialState == null)
							initialState = "";
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			HandlerUtil.updateRadioState(radioCommand, initialState);
		}

		final IEvaluationService service = (IEvaluationService) window.getService(IEvaluationService.class);
		if (service != null)
			service.requestEvaluation("org.eclipse.ui.commands.toggleState");

		return null;
	}

	private IProject getProject(IWorkbenchWindow window) {
		IProject project = null;
		project = extractProject(window.getSelectionService().getSelection());

		if (project != null) {
			setProperty(project);
			return project;
		}

		project = extractProject(window.getActivePage());

		if (project != null) {
			setProperty(project);
			return project;
		}

		Object property = getProperty();
		if (property == null || !((IProject) property).exists()) {
			// try {
			// The UI will change only if the state changes
			// So every time we need to pass a different state to
			// represent the void state
			// See State.fireStateChanged in State.setValue
			// HandlerUtil.updateRadioState(event.getCommand(),
			// Integer.toString(voidState++));
			// } catch (ExecutionException e) {
			// e.printStackTrace();
			// }
			return null;
		} else {
			return (IProject) property;
		}
	}

	// Get project from selected item
	private IProject extractProject(ISelection sel) {
		if (!(sel instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection ss = (IStructuredSelection) sel;
		Object element = ss.getFirstElement();

		if (element instanceof IResource)
			return ((IResource) element).getProject();

		if (!(element instanceof IAdaptable))
			return null;

		IAdaptable adaptable = (IAdaptable) element;
		Object adapter = adaptable.getAdapter(IResource.class);

		if (adapter != null)
			return ((IResource) adapter).getProject();

		return null;
	}

	// Get project from active editor
	private IProject extractProject(IWorkbenchPage iworkbenchpage) {
		if (iworkbenchpage != null) {
			IEditorPart editor = iworkbenchpage.getActiveEditor();

			if (editor == null)
				return null;

			IEditorInput input = editor.getEditorInput();

			if (!(input instanceof IFileEditorInput))
				return null;
			return ((IFileEditorInput) input).getFile().getProject();
		}

		return null;
	}

	// Get project from last success execution
	private void setProperty(IProject project) {
		try {
			ResourcesPlugin.getWorkspace().getRoot().setSessionProperty(new QualifiedName("MyPlugin", "last"), project);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	// Set project to property
	private Object getProperty() {
		try {
			return ResourcesPlugin.getWorkspace().getRoot().getSessionProperty(new QualifiedName("MyPlugin", "last"));
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	public static void copy(String oldPath, String newPath) throws IOException {
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = new FileInputStream(oldPath);
			fos = new FileOutputStream(newPath);
			byte[] buffer = new byte[1024];
			int byteread = 0;
			while ((byteread = is.read(buffer)) != -1) {
				fos.write(buffer, 0, byteread);
			}
		} catch (IOException e) {
			System.out.println("error copy");
			e.printStackTrace();
		} finally {
			is.close();
			fos.close();
		}
	}

	public static void copy(File oldfile, String newPath) throws IOException {
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = new FileInputStream(oldfile);
			fos = new FileOutputStream(newPath);
			byte[] buffer = new byte[1024];
			int byteread = 0;
			while ((byteread = is.read(buffer)) != -1) {
				fos.write(buffer, 0, byteread);
			}
		} catch (Exception e) {
			System.out.println("error copy");
			e.printStackTrace();
		} finally {
			is.close();
			fos.close();
		}
	}
}
