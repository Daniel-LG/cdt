package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.launch.ui.SmartSimuModuleMainTab;

public class SmartSimuModuleMainTab2 extends SmartSimuModuleMainTab {
	
	public SmartSimuModuleMainTab2() {
		super();
	}

	public SmartSimuModuleMainTab2(int flags) {
		super(flags);
	}

    @Override
    public String getId() {
    	// Return the old id as to be backwards compatible
        return "org.eclipse.cdt.dsf.gdb.launch.mainTab"; //$NON-NLS-1$
    }
    
}
