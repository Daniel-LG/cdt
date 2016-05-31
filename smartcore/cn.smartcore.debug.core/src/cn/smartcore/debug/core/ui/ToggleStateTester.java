package cn.smartcore.debug.core.ui;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.services.IServiceLocator;

public class ToggleStateTester extends PropertyTester {

	public static final String PROPERTY_NAMESPACE = "cn.smartcore";
	public static final String TOGGLE_PROPERTY_NAME = "toggle";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		// System.out.println("property:" + property);
		if (receiver instanceof IServiceLocator && args.length == 1 && args[0] instanceof String) {
			final IServiceLocator locator = (IServiceLocator) receiver;
			if (TOGGLE_PROPERTY_NAME.equals(property)) {
				final String commandId = args[0].toString();
				final ICommandService commandService = (ICommandService) locator.getService(ICommandService.class);
				if (commandService == null)
					return false;
				final Command command = commandService.getCommand(commandId);
				final State state = command.getState(RegistryToggleState.STATE_ID);
				// System.out.println(state.getValue());
				if (state != null)
					return state.getValue().equals(expectedValue);
			}
		}
		return false;
	}

}
