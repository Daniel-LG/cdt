/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language.settings.providers;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IResource;

/**
 * Contains the details of changes that occurred as a result of modifying
 * language settings entries {@link ICLanguageSettingEntry}. 
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ILanguageSettingsChangeEvent {
	public String getProjectName();
	
	public String[] getConfigurationDescriptionIds();
	
	public IResource[] getResources(String cfgDescriptionId);
	
//	// AG - YAGNI
//	public List<ICLanguageSettingEntry> getSettingEntriesOld(ICConfigurationDescription cfgDescription, IResource rc, String languageId);
//	public List<ICLanguageSettingEntry> getSettingEntriesNew(ICConfigurationDescription cfgDescription, IResource rc, String languageId);
//	// AG - or maybe
//	ILanguageSettingsDelta getDelta(ICConfigurationDescription cfgDescription);

}
