/*******************************************************************************
 * Copyright (c) 2006-2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.math.BigInteger;

public interface IMemorySelection 
{
    public boolean hasSelection();
    
    public boolean isSelected(BigInteger address);
   
    public BigInteger getStart();

    public BigInteger getEnd();
    
    public BigInteger getStartLow();
    
    public void setStart(BigInteger high, BigInteger low);
    
    public void setEnd(BigInteger high, BigInteger low);
    
    public BigInteger getHigh();
    
    public BigInteger getLow();
    
    public void clear();
}
