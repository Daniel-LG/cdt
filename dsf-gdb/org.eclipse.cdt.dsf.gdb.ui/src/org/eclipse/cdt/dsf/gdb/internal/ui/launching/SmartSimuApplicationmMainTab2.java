package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.launch.ui.SmartSimuApplicationMainTab;

public class SmartSimuApplicationmMainTab2 extends SmartSimuApplicationMainTab{

	public SmartSimuApplicationmMainTab2() {
		super();
	}

	public SmartSimuApplicationmMainTab2(int flags) {
		super(flags);
	}

    @Override
    public String getId() {
    	// Return the old id as to be backwards compatible
        return "org.eclipse.cdt.dsf.gdb.launch.mainTab"; //$NON-NLS-1$
    }
    
}
