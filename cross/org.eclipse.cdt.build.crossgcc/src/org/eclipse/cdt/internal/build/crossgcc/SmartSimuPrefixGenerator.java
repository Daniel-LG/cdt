package org.eclipse.cdt.internal.build.crossgcc;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCommandGenerator;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;

// added by jwy
public class SmartSimuPrefixGenerator implements IOptionCommandGenerator {

	@Override
	public String generateCommand(IOption option, IVariableSubstitutor macroSubstitutor) {
		return "sparc-rtems4.11-";
	}

}
