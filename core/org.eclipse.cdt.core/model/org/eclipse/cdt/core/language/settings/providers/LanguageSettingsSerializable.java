/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.core.resources.IResource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class is the base class for language settings providers able to serialize
 * into XML storage.
 *
 * TODO - more JavaDoc, info and hints about class hierarchy
 *
 */
public class LanguageSettingsSerializable extends LanguageSettingsBaseProvider {
	public static final String ELEM_PROVIDER = "provider"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$

	private static final String ELEM_LANGUAGE_SCOPE = "language-scope"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_PARAMETER = "parameter"; //$NON-NLS-1$
	private static final String ATTR_STORE_ENTRIES = "store-entries"; //$NON-NLS-1$
	private static final String VALUE_WORKSPACE = "workspace"; //$NON-NLS-1$
	private static final String VALUE_PROJECT = "project"; //$NON-NLS-1$

	/** Tells if language settings entries are persisted with the project or in workspace area while serializing. */
	private boolean storeEntriesInProjectArea = false;

	private LanguageSettingsStorage fStorage = new LanguageSettingsStorage();

	/**
	 * Default constructor. This constructor has to be always followed with setting id and name of the provider.
	 */
	public LanguageSettingsSerializable() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param id - id of the provider.
	 * @param name - name of the provider. Note that this name may show up in UI.
	 */
	public LanguageSettingsSerializable(String id, String name) {
		super(id, name);
	}

	/**
	 * Constructor which allows to instantiate provider defined via XML markup.
	 *
	 * @param elementProvider
	 */
	public LanguageSettingsSerializable(Element elementProvider) {
		super();
		load(elementProvider);
	}

	@Override
	public void configureProvider(String id, String name, List<String> languages, List<ICLanguageSettingEntry> entries, String customParameter) {
		// do not pass entries to super, keep them in local storage
		super.configureProvider(id, name, languages, null, customParameter);

		fStorage.clear();

		if (entries!=null) {
			// note that these entries are intended to be retrieved by LanguageSettingsManager.getSettingEntriesUpResourceTree()
			// when the whole resource hierarchy has been traversed up
			setSettingEntries(null, null, null, entries);
		}
	}

	/**
	 * @return {@code true} if the provider does not keep any settings yet or {@code false} if there are some.
	 */
	public boolean isEmpty() {
		return fStorage.isEmpty();
	}

	/**
	 * Set the language scope of the provider.
	 *
	 * @param languages - the list of languages this provider provides for.
	 *    If {@code null}, the provider provides for any language.
	 *
	 * @see #getLanguageScope()
	 */
	public void setLanguageScope(List <String> languages) {
		if (languages==null)
			this.languageScope = null;
		else
			this.languageScope = new ArrayList<String>(languages);
	}

	/**
	 * Set custom parameter for the provider.
	 * Subclasses are free to define how their behavior depends on custom parameter.
	 *
	 * @param customParameter
	 */
	public void setCustomParameter(String customParameter) {
		this.customParameter = customParameter;
	}

	/**
	 * Tells if language settings entries are persisted with the project (under .settings folder)
	 * or in workspace area. Persistence in the project area lets the entries migrate with the
	 * project.
	 *
	 * @return {@code true} if LSE persisted with the project or {@code false} if in the workspace.
	 */
	public boolean isStoringEntriesInProjectArea() {
		return storeEntriesInProjectArea;
	}

	/**
	 * Setter to define where language settings are persisted.
	 * @param storeEntriesWithProject - {@code true} if with the project,
	 *    {@code false} if in workspace area.
	 */
	public void setStoringEntriesInProjectArea(boolean storeEntriesWithProject) {
		this.storeEntriesInProjectArea = storeEntriesWithProject;
	}

	/**
	 * Clear all the entries for all configurations, all resources and all languages.
	 */
	public void clear() {
		fStorage.clear();
	}

	/**
	 * Sets language settings entries for the provider.
	 * Note that the entries are not persisted at that point. To persist use TODO
	 *
	 * @param cfgDescription - configuration description.
	 * @param rc - resource such as file or folder.
	 * @param languageId - language id. If {@code null}, then entries are considered to be defined for
	 *    the language scope. See {@link #getLanguageScope()}
	 * @param entries - language settings entries to set.
	 */
	public void setSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId, List<ICLanguageSettingEntry> entries) {
		String rcProjectPath = rc!=null ? rc.getProjectRelativePath().toString() : null;
		fStorage.setSettingEntries(rcProjectPath, languageId, entries);
		
//		// TODO - not sure what is more efficient, to do that or not to do that?
//		if (fStorage.equals(lastPersistedState)) {
//			lastPersistedState = null;
//		}
	}

	/**
	 * {@inheritDoc}
	 * <br> Note that this list is <b>unmodifiable</b>. To modify the list copy it, change and use
	 * {@link #setSettingEntries(ICConfigurationDescription, IResource, String, List)}.
	 * 
	 * <br/> Note also that <b>you can compare these lists with simple equality operator ==</b>,
	 * as lists themselves are backed by WeakHashSet<List<ICLanguageSettingEntry>> where
	 * identical copies (deep comparison is used) are replaced with the same one instance.
	 */
	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		List<ICLanguageSettingEntry> entries = fStorage.getSettingEntries(cfgDescription, rc, languageId);
		if (entries == null) {
			if (languageId!=null && (languageScope==null || languageScope.contains(languageId))) {
				entries = getSettingEntries(cfgDescription, rc, null);
			}
		}

		return entries;
	}

	/**
	 * Serialize the provider under parent XML element.
	 * This is convenience method not intended to be overridden on purpose.
	 * Override {@link #serializeAttributes(Element)} or
	 * {@link #serializeEntries(Element)} instead.
	 * 
	 * @param parentElement - element where to serialize.
	 * @return - newly created <provider> element. That element will already be
	 *    attached to the parent element.
	 */
	final public Element serialize(Element parentElement) {
		/*
		<provider id="provider.id" ...>
			<language-scope id="lang.id"/>
			<language id="lang.id">
				<resource project-relative-path="/">
					<entry flags="" kind="includePath" name="path"/>
				</resource>
			</language>
		</provider>
		 */
		Element elementProvider = serializeAttributes(parentElement);
		serializeEntries(elementProvider);
		return elementProvider;
	}

	/**
	 * Serialize the provider attributes under parent XML element. That is
	 * equivalent to serializing everything (including language scope) except entries.
	 *
	 * @param parentElement - element where to serialize.
	 * @return - newly created <provider> element. That element will already be
	 *    attached to the parent element.
	 */
	public Element serializeAttributes(Element parentElement) {
		Element elementProvider = XmlUtil.appendElement(parentElement, ELEM_PROVIDER, new String[] {
				ATTR_ID, getId(),
				ATTR_NAME, getName(),
				ATTR_CLASS, getClass().getCanonicalName(),
				ATTR_PARAMETER, getCustomParameter(),
				ATTR_STORE_ENTRIES, isStoringEntriesInProjectArea() ? VALUE_PROJECT : VALUE_WORKSPACE,
			});

		if (languageScope!=null) {
			for (String langId : languageScope) {
				XmlUtil.appendElement(elementProvider, ELEM_LANGUAGE_SCOPE, new String[] {ATTR_ID, langId});
			}
		}
		return elementProvider;
	}

	/**
	 * Serialize the provider entries under parent XML element.
	 * @param elementProvider - element where to serialize the entries.
	 */
	public void serializeEntries(Element elementProvider) {
		fStorage.serializeEntries(elementProvider);
	}

	/**
	 * Load provider from XML provider element.
	 * This is convenience method not intended to be overridden on purpose.
	 * Override {@link #loadAttributes(Element)} or
	 * {@link #loadEntries(Element)} instead.
	 * 
	 * @param providerNode - XML element <provider> to load provider from.
	 */
	final public void load(Element providerNode) {
		fStorage.clear();
		languageScope = null;

		// provider/configuration/language/resource/entry
		if (providerNode!=null) {
			loadAttributes(providerNode);
			loadEntries(providerNode);
		}
	}

	/**
	 * Determine and set language scope from given XML node.
	 */
	private void loadLanguageScopeElement(Node parentNode) {
		if (languageScope==null) {
			languageScope = new ArrayList<String>();
		}
		String id = XmlUtil.determineAttributeValue(parentNode, ATTR_ID);
		languageScope.add(id);
	
	}

	/**
	 * Load attributes from XML provider element.
	 * @param providerNode - XML element <provider> to load attributes from.
	 */
	public void loadAttributes(Element providerNode) {
		String providerId = XmlUtil.determineAttributeValue(providerNode, ATTR_ID);
		String providerName = XmlUtil.determineAttributeValue(providerNode, ATTR_NAME);
		String providerParameter = XmlUtil.determineAttributeValue(providerNode, ATTR_PARAMETER);
		String providerStoreEntries = XmlUtil.determineAttributeValue(providerNode, ATTR_STORE_ENTRIES);

		this.setId(providerId);
		this.setName(providerName);
		this.setCustomParameter(providerParameter);
		this.setStoringEntriesInProjectArea(VALUE_PROJECT.equals(providerStoreEntries));

		NodeList nodes = providerNode.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			Node elementNode = nodes.item(i);
			if(elementNode.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if (ELEM_LANGUAGE_SCOPE.equals(elementNode.getNodeName())) {
				loadLanguageScopeElement(elementNode);
			}
		}

	}

	/**
	 * Load provider entries from XML provider element.
	 * @param providerNode - parent XML element <provider> where entries are defined.
	 */
	public void loadEntries(Element providerNode) {
		fStorage.loadEntries(providerNode);
	}
	
	/**
	 * See {@link #cloneShallow()}. This method is extracted
	 * to avoid expressing {@link #clone()} via {@link #cloneShallow()}.
	 */
	private LanguageSettingsSerializable cloneShallowInternal() throws CloneNotSupportedException {
		LanguageSettingsSerializable clone = (LanguageSettingsSerializable)super.clone();
		if (languageScope!=null)
			clone.languageScope = new ArrayList<String>(languageScope);

		clone.fStorage = new LanguageSettingsStorage();
		return clone;
	}

	/**
	 * Shallow clone of the provider. "Shallow" is defined here as the exact copy except that
	 * the copy will have zero language settings entries.
	 *
	 * @return shallow copy of the provider.
	 * @throws CloneNotSupportedException in case {@link #clone()} throws the exception.
	 */
	protected LanguageSettingsSerializable cloneShallow() throws CloneNotSupportedException {
		return cloneShallowInternal();
	}

	@Override
	protected LanguageSettingsSerializable clone() throws CloneNotSupportedException {
		LanguageSettingsSerializable clone = cloneShallowInternal();
		clone.fStorage = fStorage.cloneStorage();
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((languageScope == null) ? 0 : languageScope.hashCode());
		result = prime * result + ((customParameter == null) ? 0 : customParameter.hashCode());
		result = prime * result + (storeEntriesInProjectArea ? 0 : 1);
		result = prime * result + ((fStorage == null) ? 0 : fStorage.hashCode());
		result = prime * result + getClass().hashCode();
		return result;
	}

	/**
	 * @return {@code true} if the objects are equal, {@code false } otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LanguageSettingsSerializable other = (LanguageSettingsSerializable) obj;

		String id = getId();
		String otherId = other.getId();
		if (id == null) {
			if (otherId != null)
				return false;
		} else if (!id.equals(otherId))
			return false;

		String name = getName();
		String otherName = other.getName();
		if (name == null) {
			if (otherName != null)
				return false;
		} else if (!name.equals(otherName))
			return false;

		if (languageScope == null) {
			if (other.languageScope != null)
				return false;
		} else if (!languageScope.equals(other.languageScope))
			return false;

		if (customParameter == null) {
			if (other.customParameter != null)
				return false;
		} else if (!customParameter.equals(other.customParameter))
			return false;

		if (storeEntriesInProjectArea!=other.storeEntriesInProjectArea)
			return false;

		if (fStorage == null) {
			if (other.fStorage != null)
				return false;
		} else if (!fStorage.equals(other.fStorage))
			return false;
		return true;
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public LanguageSettingsStorage getStorageInternal() {
		return fStorage;
	}
}
