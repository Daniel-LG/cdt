/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 * Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *                                Save build output (bug 294106)
 * Andrew Gvozdev (Quoin Inc)   - Saving build output implemented in different way (bug 306222)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.util.ListComparator;
import org.eclipse.cdt.internal.core.BuildRunnerHelper;
import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildDescription;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildStateManager;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IBuildModelBuilder;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IConfigurationBuildState;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IProjectBuildState;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.StepBuilder;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator2;
import org.eclipse.cdt.newmake.core.IMakeBuilderInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import cn.smartcore.dev.ui.SmartSimuDevPlugin;

public class CommonBuilder extends ACBuilder {

	public final static String BUILDER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier()
			+ ".genmakebuilder"; //$NON-NLS-1$
	private static final String ERROR_HEADER = "GeneratedmakefileBuilder error ["; //$NON-NLS-1$
	private static final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String TRACE_FOOTER = "]: "; //$NON-NLS-1$
	private static final String TRACE_HEADER = "GeneratedmakefileBuilder trace ["; //$NON-NLS-1$
	public static boolean VERBOSE = false;

	private static final int PROGRESS_MONITOR_SCALE = 100;
	private static final int TICKS_STREAM_PROGRESS_MONITOR = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_EXECUTE_COMMAND = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_REFRESH_PROJECT = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_DELETE_OUTPUTS = 1 * PROGRESS_MONITOR_SCALE;

	private static CfgBuildSet fBuildSet = new CfgBuildSet();

	private boolean fBuildErrOccured;

	// added by jwy
	private String SMARTSIMU_SIMULATOR_NATURE = "cn.smartcore.dev.ui.SimulatorProject"; //$NON-NLS-1$

	public CommonBuilder() {
	}

	public static void outputTrace(String resourceName, String message) {
		if (VERBOSE) {
			System.out.println(TRACE_HEADER + resourceName + TRACE_FOOTER + message + NEWLINE);
		}
	}

	public static void outputError(String resourceName, String message) {
		if (VERBOSE) {
			System.err.println(ERROR_HEADER + resourceName + TRACE_FOOTER + message + NEWLINE);
		}
	}

	private static class CfgBuildSet {
		Map<IProject, Set<String>> fMap = new HashMap<IProject, Set<String>>();

		public Set<String> getCfgIdSet(IProject project, boolean create) {
			Set<String> set = fMap.get(project);
			if (set == null && create) {
				set = new HashSet<String>();
				fMap.put(project, set);
			}
			return set;
		}

		public void start(CommonBuilder bld) {
			checkClean(bld);
		}

		private boolean checkClean(CommonBuilder bld) {
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject wproject : projects) {
				if (bld.hasBeenBuilt(wproject)) {
					if (VERBOSE)
						outputTrace(null, "checking clean: the project " + wproject.getName() //$NON-NLS-1$
								+ " was built, no clean needed"); //$NON-NLS-1$

					return false;
				}
			}

			if (VERBOSE)
				outputTrace(null, "checking clean: no projects were built.. cleanning"); //$NON-NLS-1$

			fMap.clear();
			return true;
		}
	}

	private static class CfgBuildInfo {
		private final IProject fProject;
		private final IManagedBuildInfo fBuildInfo;
		private final IConfiguration fCfg;
		private final IBuilder fBuilder;
		private IConsole fConsole;

		CfgBuildInfo(IBuilder builder, boolean isForegound) {
			this.fBuilder = builder;
			this.fCfg = builder.getParent().getParent();
			this.fProject = this.fCfg.getOwner().getProject();
			this.fBuildInfo = ManagedBuildManager.getBuildInfo(this.fProject);
		}

		public IProject getProject() {
			return fProject;
		}

		public IConsole getConsole() {
			if (fConsole == null) {
				fConsole = CCorePlugin.getDefault().getConsole();
				fConsole.start(fProject);
			}
			return fConsole;
		}

		// public boolean isForeground(){
		// return fIsForeground;
		// }

		public IBuilder getBuilder() {
			return fBuilder;
		}

		public IConfiguration getConfiguration() {
			return fCfg;
		}

		public IManagedBuildInfo getBuildInfo() {
			return fBuildInfo;
		}
	}

	public class ResourceDeltaVisitor implements IResourceDeltaVisitor {
		private String buildGoalName;
		private final IProject project;
		private final IPath buildPaths[];
		private boolean incrBuildNeeded = false;
		private boolean fullBuildNeeded = false;
		private final List<String> reservedNames;

		public ResourceDeltaVisitor(IConfiguration cfg, IConfiguration allConfigs[]) {
			this.project = cfg.getOwner().getProject();
			buildPaths = new IPath[allConfigs.length];
			for (int i = 0; i < buildPaths.length; i++) {
				buildPaths[i] = ManagedBuildManager.getBuildFullPath(allConfigs[i],
						allConfigs[i].getBuilder());
			}
			String ext = cfg.getArtifactExtension();
			// try to resolve build macros in the build artifact extension
			try {
				ext = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(ext, "", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION, cfg);
			} catch (BuildMacroException e) {
			}

			String name = cfg.getArtifactName();
			// try to resolve build macros in the build artifact name
			try {
				String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
						name, "", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION, cfg);
				if ((resolved = resolved.trim()).length() > 0)
					name = resolved;
			} catch (BuildMacroException e) {
			}

			if (ext.length() > 0) {
				buildGoalName = cfg.getOutputPrefix(ext) + name + IManagedBuilderMakefileGenerator.DOT + ext;
			} else {
				buildGoalName = name;
			}
			reservedNames = Arrays.asList(new String[] { ".cdtbuild", ".cdtproject", ".project" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		private boolean isGeneratedResource(IResource resource) {
			// Is this a generated directory ...
			IPath path = resource.getFullPath();
			for (IPath buildPath : buildPaths) {
				if (buildPath != null && buildPath.isPrefixOf(path)) {
					return true;
				}
			}
			return false;
		}

		private boolean isProjectFile(IResource resource) {
			return reservedNames.contains(resource.getName());
		}

		public boolean shouldBuildIncr() {
			return incrBuildNeeded;
		}

		public boolean shouldBuildFull() {
			return fullBuildNeeded;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			// If the project has changed, then a build is needed and we can stop
			if (resource != null && resource.getProject() == project) {
				switch (resource.getType()) {
				case IResource.FILE:
					String name = resource.getName();
					if ((!name.equals(buildGoalName) &&
					// TODO: Also need to check for secondary outputs
							(resource.isDerived() || (isProjectFile(resource))
									|| (isGeneratedResource(resource))))) {
						// The resource that changed has attributes which make it uninteresting,
						// so don't do anything
					} else {
						// TODO: Should we do extra checks here to determine if a build is really needed,
						// or do you just do exclusion checks like above?
						// We could check for:
						// o The build goal name
						// o A secondary output
						// o An input file to a tool:
						// o Has an extension of a source file used by a tool
						// o Has an extension of a header file used by a tool
						// o Has the name of an input file specified in an InputType via:
						// o An Option
						// o An AdditionalInput
						//
						// if (resourceName.equals(buildGoalName) ||
						// (buildInfo.buildsFileType(ext) || buildInfo.isHeaderFile(ext))) {

						// We need to do an incremental build, at least
						incrBuildNeeded = true;
						if (delta.getKind() == IResourceDelta.REMOVED) {
							// If a meaningful resource was removed, then force a full build
							// This is required because an incremental build will trigger make to
							// do nothing for a missing source, since the state after the file
							// removal is uptodate, as far as make is concerned
							// A full build will clean, and ultimately trigger a relink without
							// the object generated from the deleted source, which is what we want
							fullBuildNeeded = true;
							// There is no point in checking anything else since we have
							// decided to do a full build anyway
							break;
						}

						// }
					}

					return false;
				}
			}
			return true;
		}
	}

	private static class OtherConfigVerifier implements IResourceDeltaVisitor {
		IPath buildFullPaths[];
		// IConfiguration buildConfigs[];
		Configuration otherConfigs[];
		int resourceChangeState;

		private static final IPath[] ignoreList = { new Path(".cdtproject"), //$NON-NLS-1$
				new Path(".cproject"), //$NON-NLS-1$
				new Path(".cdtbuild"), //$NON-NLS-1$
				new Path(".settings"), //$NON-NLS-1$
		};

		OtherConfigVerifier(IBuilder builders[], IConfiguration allCfgs[]) {
			Set<IConfiguration> buildCfgSet = new HashSet<IConfiguration>();
			for (IBuilder builder : builders) {
				buildCfgSet.add(builder.getParent().getParent());
			}
			@SuppressWarnings("unchecked")
			List<Configuration> othersList = ListComparator.getAdded(allCfgs, buildCfgSet.toArray());
			if (othersList != null)
				otherConfigs = othersList.toArray(new Configuration[othersList.size()]);
			else
				otherConfigs = new Configuration[0];

			List<IPath> list = new ArrayList<IPath>(builders.length);
			// buildFullPaths = new IPath[builders.length];
			for (IBuilder builder : builders) {
				IPath path = ManagedBuildManager.getBuildFullPath(builder.getParent().getParent(), builder);
				if (path != null)
					list.add(path);
				// buildFullPaths[i] =
				// ManagedBuildManager.getBuildFullPath(builders[i].getParent().getParent(), builders[i]);
			}
			buildFullPaths = list.toArray(new IPath[list.size()]);

		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {

			IResource rc = delta.getResource();
			if (rc.getType() == IResource.FILE) {
				if (isResourceValuable(rc))
					resourceChangeState |= delta.getKind();
				return false;
			}

			if (!isResourceValuable(rc))
				return false;
			for (IPath buildFullPath : buildFullPaths) {
				if (buildFullPath.isPrefixOf(rc.getFullPath()))
					return false;
			}
			return true;
		}

		public void updateOtherConfigs(IResourceDelta delta) {
			if (delta == null)
				resourceChangeState = ~0;
			else {
				try {
					delta.accept(this);
				} catch (CoreException e) {
					resourceChangeState = ~0;
				}
			}

			setResourceChangeStateForOtherConfigs();
		}

		private void setResourceChangeStateForOtherConfigs() {
			for (Configuration otherConfig : otherConfigs) {
				otherConfig.addResourceChangeState(resourceChangeState);
			}
		}

		private boolean isResourceValuable(IResource rc) {
			IPath path = rc.getProjectRelativePath();
			for (IPath ignoredPath : ignoreList) {
				if (ignoredPath.equals(path))
					return false;
			}
			return true;
		}
	}

	protected boolean isCdtProjectCreated(IProject project) {
		ICProjectDescription des = CoreModel.getDefault().getProjectDescription(project, false);
		return des != null && !des.isCdtProjectCreating();
	}

	private class MyBoolean {
		private boolean value;

		public MyBoolean(boolean value) {
			this.value = value;
		}

		public boolean getValue() {
			return value;
		}

		public void setValue(boolean value) {
			this.value = value;
		}

	}

	/**
	 * @see IncrementalProjectBuilder#build
	 */
	@Override
	protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map argsMap, IProgressMonitor monitor)
			throws CoreException {
		@SuppressWarnings("unchecked")
		Map<String, String> args = argsMap;
		if (DEBUG_EVENTS)
			printEvent(kind, args);

		fBuildSet.start(this);
		IProject project = getProject();

		// added by jwy, if this is a simulator project, first we generate a C file from the conf file
		if (project.hasNature(SMARTSIMU_SIMULATOR_NATURE)) {
			// Add the C file to project
			// TODO: how to generate the C file?
			// InputStream resourceStream = new ByteArrayInputStream(
			// ("#include<stdio.h>\nint main(){\n\tprintf(\"aaa\");\n}").getBytes());
			cleanSoFiles(project.getFile(new Path("resources")).getLocation().toString()); //$NON-NLS-1$
			InputStream resourceStream = null;
			try {
				resourceStream = generateCFile(project, new Path("resources/conf.h"), //$NON-NLS-1$
						new Path("address.conf"), new Path(project.getName() + ".conf")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			addFileToProject(project, new Path("resources/conf.c"), resourceStream, monitor); //$NON-NLS-1$
			try {
				resourceStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (!isCdtProjectCreated(project))
			return project.getReferencedProjects();

		if (VERBOSE)
			outputTrace(project.getName(), ">>build requested, type = " + kind); //$NON-NLS-1$

		IProject[] projects = null;
		if (needAllConfigBuild()) {
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration[] cfgs = info.getManagedProject().getConfigurations();
			IConfiguration defCfg = info.getDefaultConfiguration();
			for (IConfiguration cfg : cfgs) {
				info.setDefaultConfiguration(cfg);
				IBuilder builders[] = ManagedBuilderCorePlugin.createBuilders(project, args);
				projects = build(kind, project, builders, true, monitor, new MyBoolean(false));
			}
			info.setDefaultConfiguration(defCfg);
		} else {
			IBuilder builders[] = ManagedBuilderCorePlugin.createBuilders(project, args);
			projects = build(kind, project, builders, true, monitor, new MyBoolean(false));
		}

		if (VERBOSE)
			outputTrace(project.getName(), "<<done build requested, type = " + kind); //$NON-NLS-1$

		return projects;
	}

	private void cleanSoFiles(String directoryPath) {
		File directory = new File(directoryPath);
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName().endsWith(".so")) { //$NON-NLS-1$
				file.delete();
			}
		}
	}

	// Added by jwy. Generate the conf.c file
	private InputStream generateCFile(IProject project, Path confH, Path addressConf, Path simuConf)
			throws CoreException, IOException {
		StringBuffer cFileContent = new StringBuffer();
		String tmp;

		appendWithLineFeed(cFileContent, "// Part I"); //$NON-NLS-1$
		final IFile confHFile = project.getFile(confH);
		BufferedReader br = new BufferedReader(new InputStreamReader(confHFile.getContents()));
		try {
			while ((tmp = br.readLine()) != null) {
				appendWithLineFeed(cFileContent, tmp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			br.close();
		}

		appendWithLineFeed(cFileContent, "// Part II"); //$NON-NLS-1$
		int count = 1;
		ArrayList<String> itemList = new ArrayList<>();
		final IFile addressConfFile = project.getFile(addressConf);
		br = new BufferedReader(new InputStreamReader(addressConfFile.getContents()));
		try {
			while ((tmp = br.readLine()) != null) {
				if (isBlankOrComment(tmp)) {
					continue;
				}

				tmp = tmp.trim();
				String[] parameters = tmp.split("\\s+"); //$NON-NLS-1$
				String item = parameters[0];
				String startAddr = parameters[1];
				String endAddr = parameters[2];
				String offset = parameters[3];
				itemList.add(item);
				appendAddressAttrs(cFileContent, item, startAddr, endAddr, offset, count);
				count++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			br.close();
		}
		appendOverallAddressAttrs(cFileContent, itemList);

		appendWithLineFeed(cFileContent, "// Part III"); //$NON-NLS-1$
		// record type, name and number of total attrs of each module
		ArrayList<Tuple3<String, String, Integer>> moduleAttrsList = new ArrayList<>();
		final IFile simuConfFile = project.getFile(simuConf);
		br = new BufferedReader(new InputStreamReader(simuConfFile.getContents()));
		try {
			while ((tmp = br.readLine()) != null) {
				if (isBlankOrComment(tmp)) {
					continue;
				}

				int pos;
				if ((pos = tmp.indexOf('{')) != -1) {
					String projName = tmp.substring(0, pos).trim();
					Module module = new Module(SmartSimuDevPlugin.getModuleType(projName));
					while (true) {
						tmp = br.readLine();
						if (isBlankOrComment(tmp)) {
							continue;
						}

						tmp = tmp.trim();
						// the end of this module
						if (tmp.equals("}")) { //$NON-NLS-1$
							addModuleAttrs(cFileContent, module);
							int attrsCount = module.STRINGAttrs.size() + module.U64Attrs.size()
									+ module.INTERFACEAttrs.size() + module.ARRAYAttrs.size();
							if (module.type.startsWith("core")) { //$NON-NLS-1$
								// the core module needs a default reference interface to the "access" of the
								// memory_space
								moduleAttrsList.add(new Tuple3<String, String, Integer>(module.type,
										module.name, attrsCount + 1));
							} else {
								moduleAttrsList.add(new Tuple3<String, String, Integer>(module.type,
										module.name, attrsCount));
							}
							copySoFile(project.getFile(new Path("resources")).getLocation().toString(), //$NON-NLS-1$
									module.type, SmartSimuDevPlugin.getModuleSoPath(projName));
							break;
						}

						// add attrs to module
						String[] parameters = tmp.split("\\s+"); //$NON-NLS-1$
						if (parameters[0].equals("STRING")) { //$NON-NLS-1$
							if (parameters[1].equals("name")) { //$NON-NLS-1$
								module.name = parameters[2];
							}
							module.addSTRINGAttr(parameters[1], parameters[2]);
						} else if (parameters[0].equals("U64")) { //$NON-NLS-1$
							module.addU64Attr(parameters[1], parameters[2]);
						} else if (parameters[0].equals("INTERFACE")) { //$NON-NLS-1$
							module.addINTERFACEAttr(parameters[1], parameters[2]);
						} else if (parameters[0].equals("ARRAY")) { //$NON-NLS-1$
							module.addARRAYAttr(parameters[1], parameters[2]);
						} else {
							System.out.println("error line:" + tmp); //$NON-NLS-1$
						}

					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			br.close();
		}

		appendWithLineFeed(cFileContent, "// Part IV"); //$NON-NLS-1$
		addOverallModuleAttrs(cFileContent, moduleAttrsList);

		return new ByteArrayInputStream(cFileContent.toString().getBytes());
	}

	private void appendWithLineFeed(StringBuffer sb, String content) {
		sb.append(content).append(System.lineSeparator());
	}

	private void appendAddressAttrs(StringBuffer cFileContent, String item, String startAddr, String endAddr,
			String offset, int count) {
		appendWithLineFeed(cFileContent, "static attribute_value_interface_t memory_space_to_" + count + "_" //$NON-NLS-1$ //$NON-NLS-2$
				+ item + "_interface = {"); //$NON-NLS-1$
		appendWithLineFeed(cFileContent, "\t\"" + item + "\", \"access\", NULL, NULL"); //$NON-NLS-1$ //$NON-NLS-2$
		appendWithLineFeed(cFileContent, "};"); //$NON-NLS-1$

		appendWithLineFeed(cFileContent,
				"static attribute_value_t memory_space_to_" + count + "_" + item + "_data[] = {"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		appendWithLineFeed(cFileContent, "\t{ U64, (void *)" + startAddr + " },"); //$NON-NLS-1$ //$NON-NLS-2$
		appendWithLineFeed(cFileContent, "\t{ U64, (void *)" + endAddr + " },"); //$NON-NLS-1$ //$NON-NLS-2$
		appendWithLineFeed(cFileContent,
				"\t{ INTERFACE, &memory_space_to_" + count + "_" + item + "_interface },"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		appendWithLineFeed(cFileContent, "\t{ U64, (void *)" + offset + " }"); //$NON-NLS-1$ //$NON-NLS-2$
		appendWithLineFeed(cFileContent, "};"); //$NON-NLS-1$

		appendWithLineFeed(cFileContent,
				"static attribute_value_array_t memory_space_to_" + count + "_" + item + " = {"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		appendWithLineFeed(cFileContent, "\t4, memory_space_to_" + count + "_" + item + "_data"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		appendWithLineFeed(cFileContent, "};"); //$NON-NLS-1$
	}

	private void appendOverallAddressAttrs(StringBuffer cFileContent, ArrayList<String> itemList) {
		appendWithLineFeed(cFileContent, "static attribute_value_t memory_space_address_entry[] = {"); //$NON-NLS-1$
		for (int i = 0; i < itemList.size(); i++) {
			appendWithLineFeed(cFileContent,
					"\t{ ARRAY, &memory_space_to_" + (i + 1) + "_" + itemList.get(i) + " },"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		if (itemList.size() > 0) {
			// remove the last ","
			cFileContent.replace(cFileContent.length() - System.lineSeparator().length() - 1,
					cFileContent.length() - System.lineSeparator().length(), ""); //$NON-NLS-1$
		}
		// appendWithLineFeed(cFileContent, "\t{ ARRAY, &memory_space_to_" + itemList.size() + "_"
		// + itemList.get(itemList.size() - 1) + " }");
		appendWithLineFeed(cFileContent, "};"); //$NON-NLS-1$

		appendWithLineFeed(cFileContent, "static attribute_value_array_t memory_space_address_map = {"); //$NON-NLS-1$
		appendWithLineFeed(cFileContent, "\t" + itemList.size() + ", memory_space_address_entry"); //$NON-NLS-1$ //$NON-NLS-2$
		appendWithLineFeed(cFileContent, "};"); //$NON-NLS-1$

		appendWithLineFeed(cFileContent, "static attribute_t memory_space_attribute_confs[] = {"); //$NON-NLS-1$
		appendWithLineFeed(cFileContent, "\t{ \"name\", { STRING, \"memory_space\" }},"); //$NON-NLS-1$
		appendWithLineFeed(cFileContent, "\t{ \"size\", { U64, (void *)128 }},"); //$NON-NLS-1$
		appendWithLineFeed(cFileContent, "\t{ \"address_map\", { ARRAY, &memory_space_address_map }}"); //$NON-NLS-1$
		appendWithLineFeed(cFileContent, "};"); //$NON-NLS-1$
	}

	private boolean isBlankOrComment(String line) {
		String blankLine = "^\\s*$"; //$NON-NLS-1$
		Pattern blank = Pattern.compile(blankLine);
		String commentLine = "^\\s*#"; //$NON-NLS-1$
		Pattern comment = Pattern.compile(commentLine);

		Matcher m1 = blank.matcher(line);
		if (m1.find()) {
			return true;
		}
		Matcher m2 = comment.matcher(line);
		if (m2.find()) {
			return true;
		}

		return false;
	}

	private void addModuleAttrs(StringBuffer cFileContent, Module module) {
		// add the default memory_space reference
		appendWithLineFeed(cFileContent,
				"static attribute_value_interface_t " + module.name + "_memory_space_ref = {"); //$NON-NLS-1$ //$NON-NLS-2$
		appendWithLineFeed(cFileContent, "\t\"memory_space\", \"access\", NULL, NULL"); //$NON-NLS-1$
		appendWithLineFeed(cFileContent, "};"); //$NON-NLS-1$

		// add reference interface statement
		for (Tuple2<String, String> item : module.INTERFACEAttrs) {
			appendWithLineFeed(cFileContent,
					"static attribute_value_interface_t " + module.name + "_" + item.x + " = {"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String[] parameters = item.y.split("\\s*:\\s*"); //$NON-NLS-1$
			appendWithLineFeed(cFileContent,
					"\t\"" + parameters[0] + "\", \"" + parameters[1] + "\", NULL, NULL"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			appendWithLineFeed(cFileContent, "};"); //$NON-NLS-1$
		}

		// add attrs
		appendWithLineFeed(cFileContent, "static attribute_t " + module.name + "_attribute_confs[] = {"); //$NON-NLS-1$ //$NON-NLS-2$
		for (Tuple2<String, String> item : module.STRINGAttrs) {
			appendWithLineFeed(cFileContent, "\t{ \"" + item.x + "\", { STRING, \"" + item.y + "\" }},"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		for (Tuple2<String, String> item : module.U64Attrs) {
			appendWithLineFeed(cFileContent, "\t{ \"" + item.x + "\", { U64, (void *)" + item.y + " }},"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		for (Tuple2<String, String> item : module.INTERFACEAttrs) {
			appendWithLineFeed(cFileContent,
					"\t{ \"" + item.x + "\", { INTERFACE, &" + module.name + "_" + item.x + " }},"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		// add the default memory_space reference
		appendWithLineFeed(cFileContent,
				"\t{ \"memory_space_ref\", { INTERFACE, &" + module.name + "_memory_space_ref }}"); //$NON-NLS-1$ //$NON-NLS-2$
		appendWithLineFeed(cFileContent, "};"); //$NON-NLS-1$
	}

	private void addOverallModuleAttrs(StringBuffer cFileContent,
			ArrayList<Tuple3<String, String, Integer>> moduleNameList) {
		appendWithLineFeed(cFileContent, "object_conf_t objects[] = {"); //$NON-NLS-1$
		for (Tuple3<String, String, Integer> item : moduleNameList) {
			appendWithLineFeed(cFileContent,
					"\t{ \"" + item.x + ".so\", \"" + item.y + "\", " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ item.z + ", " + item.y + "_attribute_confs },"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// append the confs of address space
		appendWithLineFeed(cFileContent,
				"\t{ \"" + "memory_space" + "/" //$NON-NLS-1$ //$NON-NLS-2$
						+ "memory_space.so\", \"memory_space\", "
						+ 3 + ", memory_space_attribute_confs }"); //$NON-NLS-1$
		appendWithLineFeed(cFileContent, "};"); //$NON-NLS-1$

		appendWithLineFeed(cFileContent, "u64 nr_objects = sizeof(objects) / sizeof(objects[0]);"); //$NON-NLS-1$
	}

	class Module {
		public final String type;
		public String name;
		public ArrayList<Tuple2<String, String>> STRINGAttrs = new ArrayList<>();
		public ArrayList<Tuple2<String, String>> U64Attrs = new ArrayList<>();
		public ArrayList<Tuple2<String, String>> INTERFACEAttrs = new ArrayList<>();
		// not used yet
		public ArrayList<Tuple2<String, String>> ARRAYAttrs = new ArrayList<>();

		public Module(String type) {
			this.type = type;
		}

		public void addSTRINGAttr(String key, String value) {
			this.STRINGAttrs.add(new Tuple2<String, String>(key, value));
		}

		public void addU64Attr(String key, String value) {
			this.U64Attrs.add(new Tuple2<String, String>(key, value));
		}

		public void addINTERFACEAttr(String key, String value) {
			this.INTERFACEAttrs.add(new Tuple2<String, String>(key, value));
		}

		public void addARRAYAttr(String key, String value) {
			this.ARRAYAttrs.add(new Tuple2<String, String>(key, value));
		}
	}

	class Tuple2<X, Y> {
		public final X x;
		public final Y y;

		public Tuple2(X x, Y y) {
			this.x = x;
			this.y = y;
		}
	}

	class Tuple3<X, Y, Z> {
		public final X x;
		public final Y y;
		public final Z z;

		public Tuple3(X x, Y y, Z z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	private void copySoFile(String targetDirectory, String moduleType, String moduleSoPath) {
		File directory = new File(targetDirectory);
		for (File file : directory.listFiles()) {
			if (file.isFile() && file.getName().equals(moduleType + ".so")) { //$NON-NLS-1$
				return;
			}
		}

		try {
			copy(moduleSoPath, targetDirectory + File.separator + moduleType + ".so"); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copy(String oldPath, String newPath) throws IOException {
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
			System.out.println("error copy"); //$NON-NLS-1$
			e.printStackTrace();
		} finally {
			is.close();
			fos.close();
		}
	}

	// Added by jwy. Adds a new file to the project.
	private void addFileToProject(IProject project, Path path, InputStream contentStream,
			IProgressMonitor monitor) throws CoreException {
		final IFile file = project.getFile(path);
		if (file.exists()) {
			file.setContents(contentStream, true, true, monitor);
		} else {
			file.create(contentStream, true, monitor);
		}
	}

	protected IProject[] build(int kind, IProject project, IBuilder[] builders, boolean isForeground,
			IProgressMonitor monitor) throws CoreException {
		return build(kind, project, builders, isForeground, monitor, new MyBoolean(false));
	}

	private IProject[] build(int kind, IProject project, IBuilder[] builders, boolean isForeground,
			IProgressMonitor monitor, MyBoolean isBuild) throws CoreException {
		if (!isCdtProjectCreated(project))
			return project.getReferencedProjects();

		int num = builders.length;
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration activeCfg = info.getDefaultConfiguration();
		IProject[] refProjects = project.getReferencedProjects();
		if (num != 0) {
			MultiStatus status = checkBuilders(builders, activeCfg);
			if (status.getSeverity() != IStatus.OK)
				throw new CoreException(status);

			IConfiguration rcfgs[] = getReferencedConfigs(builders);

			monitor.beginTask("", num + rcfgs.length); //$NON-NLS-1$

			if (rcfgs.length != 0) {
				Set<IProject> set = buildReferencedConfigs(rcfgs, new SubProgressMonitor(monitor, 1),
						isBuild);// = getProjectsSet(cfgs);
				if (set.size() != 0) {
					set.addAll(Arrays.asList(refProjects));
					refProjects = set.toArray(new IProject[set.size()]);
				}
			}

			for (int i = 0; i < num; i++) {
				// bug 219337
				if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
					if (buildConfigResourceChanges()) { // only build projects with project resource changes
						IResourceDelta delta = getDelta(project);
						if (delta != null && delta.getAffectedChildren().length > 0) { // project resource has
																						// changed within
																						// Eclipse, need to
																						// build this
																						// configuration
							isBuild.setValue(true);
							build(kind, new CfgBuildInfo(builders[i], isForeground),
									new SubProgressMonitor(monitor, 1));
						} else if (isBuild.getValue()) { // one of its dependencies have rebuilt, need to
															// rebuild this configuration
							build(kind, new CfgBuildInfo(builders[i], isForeground),
									new SubProgressMonitor(monitor, 1));
						}
					} else { // the default behaviour: 'make' is invoked on all configurations and incremental
								// build is handled by 'make'
						build(kind, new CfgBuildInfo(builders[i], isForeground),
								new SubProgressMonitor(monitor, 1));
					}
				} else { // FULL_BUILD or CLEAN
					build(kind, new CfgBuildInfo(builders[i], isForeground),
							new SubProgressMonitor(monitor, 1));
				}
			}
		}

		if (isForeground)
			updateOtherConfigs(info, builders, kind);

		monitor.done();
		return refProjects;
	}

	private Set<IProject> buildReferencedConfigs(IConfiguration[] cfgs, IProgressMonitor monitor,
			MyBoolean refConfigChanged) {
		Set<IProject> projSet = getProjectsSet(cfgs);
		cfgs = filterConfigsToBuild(cfgs);
		MyBoolean nextConfigChanged = new MyBoolean(false);

		if (cfgs.length != 0) {
			monitor.beginTask(ManagedMakeMessages.getResourceString("CommonBuilder.22"), cfgs.length); //$NON-NLS-1$
			for (IConfiguration cfg : cfgs) {
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
				nextConfigChanged.setValue(false);
				try {
					IBuilder builder = cfg.getEditableBuilder();
					// CfgBuildInfo bInfo = new CfgBuildInfo(builder, false);

					if (VERBOSE)
						outputTrace(cfg.getOwner().getProject().getName(),
								">>>>building reference cfg " + cfg.getName()); //$NON-NLS-1$

					IProject[] projs = build(INCREMENTAL_BUILD, cfg.getOwner().getProject(),
							new IBuilder[] { builder }, false, subMonitor, nextConfigChanged);

					if (VERBOSE)
						outputTrace(cfg.getOwner().getProject().getName(),
								"<<<<done building reference cfg " + cfg.getName()); //$NON-NLS-1$

					projSet.addAll(Arrays.asList(projs));
				} catch (CoreException e) {
					ManagedBuilderCorePlugin.log(e);
				} finally {
					subMonitor.done();
				}
				refConfigChanged.setValue(refConfigChanged.getValue() || nextConfigChanged.getValue());
			}
		} else {
			monitor.done();
		}

		return projSet;
	}

	private IConfiguration[] filterConfigsToBuild(IConfiguration[] cfgs) {
		List<IConfiguration> cfgList = new ArrayList<IConfiguration>(cfgs.length);
		for (IConfiguration cfg : cfgs) {
			IProject project = cfg.getOwner().getProject();
			Set<String> set = fBuildSet.getCfgIdSet(project, true);
			if (set.add(cfg.getId())) {
				if (VERBOSE) {
					outputTrace(cfg.getOwner().getProject().getName(),
							"set: adding cfg " + cfg.getName() + " ( id=" + cfg.getId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					outputTrace(cfg.getOwner().getProject().getName(),
							"filtering regs: adding cfg " + cfg.getName() + " ( id=" + cfg.getId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}

				cfgList.add(cfg);
			} else if (VERBOSE)
				outputTrace(cfg.getOwner().getProject().getName(),
						"filtering regs: excluding cfg " + cfg.getName() + " ( id=" + cfg.getId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		}
		return cfgList.toArray(new IConfiguration[cfgList.size()]);
	}

	@Override
	protected void startupOnInitialize() {
		super.startupOnInitialize();

	}

	private IConfiguration[] getReferencedConfigs(IBuilder[] builders) {
		Set<IConfiguration> set = new HashSet<IConfiguration>();
		for (IBuilder builder : builders) {
			IConfiguration cfg = builder.getParent().getParent();
			IConfiguration refs[] = ManagedBuildManager.getReferencedConfigurations(cfg);
			for (IConfiguration ref : refs) {
				set.add(ref);
			}
		}
		return set.toArray(new Configuration[set.size()]);
	}

	private Set<IProject> getProjectsSet(IConfiguration[] cfgs) {
		if (cfgs.length == 0)
			return new HashSet<IProject>(0);

		Set<IProject> set = new HashSet<IProject>();
		for (IConfiguration cfg : cfgs) {
			set.add(cfg.getOwner().getProject());
		}

		return set;
	}

	protected MultiStatus checkBuilders(IBuilder builders[], IConfiguration activeCfg) {
		MultiStatus status = null;
		for (IBuilder builder : builders) {
			boolean supportsCustomization = builder.supportsCustomizedBuild();
			boolean isManagedBuildOn = builder.isManagedBuildOn();
			if (isManagedBuildOn && !supportsCustomization) {
				if (builder.isCustomBuilder()) {
					if (status == null) {
						status = new MultiStatus(ManagedBuilderCorePlugin.getUniqueIdentifier(),
								IStatus.ERROR, new String(), null);
					}

					status.add(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), 0,
							ManagedMakeMessages.getResourceString("CommonBuilder.1"), //$NON-NLS-1$
							null));

				} else if (builder.getParent().getParent() != activeCfg) {
					if (status == null) {
						status = new MultiStatus(ManagedBuilderCorePlugin.getUniqueIdentifier(),
								IStatus.ERROR, new String(), null);
					}

					status.add(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), 0,
							ManagedMakeMessages.getResourceString("CommonBuilder.2"), //$NON-NLS-1$
							null));

				}
			}
		}

		if (status == null) {
			status = new MultiStatus(ManagedBuilderCorePlugin.getUniqueIdentifier(), IStatus.OK, new String(),
					null);
		}

		return status;
	}

	private void updateOtherConfigs(IManagedBuildInfo info, IBuilder builders[], int buildKind) {
		IConfiguration allCfgs[] = info.getManagedProject().getConfigurations();
		new OtherConfigVerifier(builders, allCfgs).updateOtherConfigs(
				buildKind == FULL_BUILD ? null : getDelta(info.getManagedProject().getOwner().getProject()));
	}

	protected class BuildStatus {
		private final boolean fManagedBuildOn;
		private boolean fRebuild;
		private boolean fBuild = true;
		private final List<String> fConsoleMessages = new ArrayList<String>();
		private IManagedBuilderMakefileGenerator fMakeGen;

		public BuildStatus(IBuilder builder) {
			fManagedBuildOn = builder.isManagedBuildOn();
		}

		public void setRebuild() {
			fRebuild = true;
		}

		public boolean isRebuild() {
			return fRebuild;
		}

		public boolean isManagedBuildOn() {
			return fManagedBuildOn;
		}

		public boolean isBuild() {
			return fBuild;
		}

		public void cancelBuild() {
			fBuild = false;
		}

		public List<String> getConsoleMessagesList() {
			return fConsoleMessages;
		}

		public IManagedBuilderMakefileGenerator getMakeGen() {
			return fMakeGen;
		}

		public void setMakeGen(IManagedBuilderMakefileGenerator makeGen) {
			fMakeGen = makeGen;
		}
	}

	protected void build(int kind, CfgBuildInfo bInfo, IProgressMonitor monitor) throws CoreException {
		if (VERBOSE)
			outputTrace(bInfo.getProject().getName(), "building cfg " + bInfo.getConfiguration().getName() //$NON-NLS-1$
					+ " with builder " + bInfo.getBuilder().getName()); //$NON-NLS-1$
		IBuilder builder = bInfo.getBuilder();
		BuildStatus status = new BuildStatus(builder);

		if (!shouldBuild(kind, builder)) {
			return;
		}
		// if (kind == IncrementalProjectBuilder.AUTO_BUILD) {
		// IResourceDelta delta = getDelta(getProject());
		// if (delta != null) {
		// IResource res = delta.getResource();
		// if (res != null) {
		// bPerformBuild = res.getProject().equals(getProject());
		// }
		// } else {
		// bPerformBuild = false;
		// }
		// }

		if (status.isBuild()) {
			IConfiguration cfg = bInfo.getConfiguration();

			if (!builder.isCustomBuilder()) {
				Set<String> set = fBuildSet.getCfgIdSet(bInfo.getProject(), true);
				if (VERBOSE)
					outputTrace(bInfo.getProject().getName(),
							"set: adding cfg " + cfg.getName() + " ( id=" + cfg.getId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				set.add(cfg.getId());
			}

			if (status.isManagedBuildOn()) {
				status = performPrebuildGeneration(kind, bInfo, status, monitor);
			}

			if (status.isBuild()) {
				try {
					boolean isClean = builder.getBuildRunner().invokeBuild(kind, bInfo.getProject(),
							bInfo.getConfiguration(), builder, bInfo.getConsole(), this, this, monitor);
					if (isClean) {
						forgetLastBuiltState();
						cfg.setRebuildState(true);
					} else {
						if (status.isManagedBuildOn()) {
							performPostbuildGeneration(kind, bInfo, status, monitor);
						}
						cfg.setRebuildState(false);
					}
				} catch (CoreException e) {
					cfg.setRebuildState(true);
					throw e;
				}

				PropertyManager.getInstance().serialize(cfg);
			} else if (status.getConsoleMessagesList().size() != 0) {
				emitMessage(bInfo, concatMessages(status.getConsoleMessagesList()));
			}
		}
		checkCancel(monitor);
	}

	private String concatMessages(List<String> msgs) {
		int size = msgs.size();
		if (size == 0) {
			return ""; //$NON-NLS-1$
		} else if (size == 1) {
			return msgs.get(0);
		}

		StringBuffer buf = new StringBuffer();
		buf.append(msgs.get(0));
		for (int i = 1; i < size; i++) {
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append(msgs.get(i));
		}
		return buf.toString();
	}

	/*
	 * (non-javadoc) Emits a message to the console indicating that there were no source files to build
	 * 
	 * @param buildType
	 * 
	 * @param status
	 * 
	 * @param configName
	 */
	private String createNoSourceMessage(int buildType, IStatus status, CfgBuildInfo bInfo)
			throws CoreException {
		StringBuffer buf = new StringBuffer();
		String[] consoleHeader = new String[3];
		String configName = bInfo.getConfiguration().getName();
		String projName = bInfo.getProject().getName();
		if (buildType == FULL_BUILD || buildType == INCREMENTAL_BUILD) {
			consoleHeader[0] = ManagedMakeMessages.getResourceString("ManagedMakeBuider.type.incremental"); //$NON-NLS-1$
		} else {
			consoleHeader[0] = new String();
			outputError(projName, "The given build type is not supported in this context"); //$NON-NLS-1$
		}
		consoleHeader[1] = configName;
		consoleHeader[2] = projName;
		buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.console.header", //$NON-NLS-1$
				consoleHeader));
		buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(status.getMessage());
		buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
		return buf.toString();
	}

	private void emitMessage(CfgBuildInfo info, String msg) throws CoreException {
		try {
			IConsole console = info.getConsole();
			ConsoleOutputStream consoleOutStream = console.getOutputStream();
			// Report a successful clean
			consoleOutStream.write(msg.getBytes());
			consoleOutStream.flush();
			consoleOutStream.close();
		} catch (CoreException e) {
			// Throw the exception back to the builder
			throw e;
		} catch (IOException io) { // Ignore console failures...
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(),
					io.getLocalizedMessage(), io));
		}
	}

	protected BuildStatus performPostbuildGeneration(int kind, CfgBuildInfo bInfo, BuildStatus buildStatus,
			IProgressMonitor monitor) throws CoreException {
		IBuilder builder = bInfo.getBuilder();
		if (builder.isInternalBuilder())
			return buildStatus;

		if (buildStatus.isRebuild()) {
			buildStatus.getMakeGen().regenerateDependencies(false);
		} else {
			buildStatus.getMakeGen().generateDependencies();
		}

		return buildStatus;
	}

	protected BuildStatus performPrebuildGeneration(int kind, CfgBuildInfo bInfo, BuildStatus buildStatus,
			IProgressMonitor monitor) throws CoreException {
		IBuilder builder = bInfo.getBuilder();
		if (builder.isInternalBuilder())
			return buildStatus;

		buildStatus = performCleanning(kind, bInfo, buildStatus, monitor);
		IManagedBuilderMakefileGenerator generator = builder.getBuildFileGenerator();
		if (generator != null) {
			initializeGenerator(generator, kind, bInfo, monitor);
			buildStatus.setMakeGen(generator);

			MultiStatus result = performMakefileGeneration(bInfo, generator, buildStatus, monitor);
			if (result.getCode() == IStatus.WARNING || result.getCode() == IStatus.INFO) {
				IStatus[] kids = result.getChildren();
				for (int index = 0; index < kids.length; ++index) {
					// One possibility is that there is nothing to build
					IStatus status = kids[index];
					// if(messages == null){
					// messages = new MultiStatus(
					// ManagedBuilderCorePlugin.getUniqueIdentifier(),
					// IStatus.INFO,
					// "",
					// null);
					//
					// }
					if (status.getCode() == IManagedBuilderMakefileGenerator.NO_SOURCE_FOLDERS) {
						// performBuild = false;
						buildStatus.getConsoleMessagesList().add(createNoSourceMessage(kind, status, bInfo));
						buildStatus.cancelBuild();
						// break;

					} else {
						// Stick this in the list of stuff to warn the user about

						// TODO: messages.add(status);
					}
				}
			} else if (result.getCode() == IStatus.ERROR) {
				StringBuffer buf = new StringBuffer();
				buf.append(ManagedMakeMessages.getString("CommonBuilder.23")).append(NEWLINE); //$NON-NLS-1$
				String message = result.getMessage();
				if (message != null && message.length() != 0) {
					buf.append(message).append(NEWLINE);
				}

				buf.append(ManagedMakeMessages.getString("CommonBuilder.24")).append(NEWLINE); //$NON-NLS-1$
				message = buf.toString();
				buildStatus.getConsoleMessagesList().add(message);
				buildStatus.cancelBuild();
			}

			checkCancel(monitor);

			// if(result.getSeverity() != IStatus.OK)
			// throw new CoreException(result);
		} else {
			buildStatus.cancelBuild();
		}

		// if(messages == null){
		// messages = createMultiStatus(IStatus.OK);
		// }

		return buildStatus;
	}

	protected BuildStatus performCleanning(int kind, CfgBuildInfo bInfo, BuildStatus status,
			IProgressMonitor monitor) throws CoreException {
		IConfiguration cfg = bInfo.getConfiguration();
		IProject curProject = bInfo.getProject();
		// IBuilder builder = bInfo.getBuilder();

		boolean makefileRegenerationNeeded = false;
		// perform necessary cleaning and build type calculation
		if (cfg.needsFullRebuild()) {
			// configuration rebuild state is set to true,
			// full rebuild is needed in any case
			// clean first, then make a full build
			outputTrace(curProject.getName(), "config rebuild state is set to true, making a full rebuild"); //$NON-NLS-1$
			clean(bInfo, new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
			makefileRegenerationNeeded = true;
		} else {
			makefileRegenerationNeeded = cfg.needsRebuild();
			IBuildDescription des = null;

			IResourceDelta delta = kind == FULL_BUILD ? null : getDelta(curProject);
			if (delta == null)
				makefileRegenerationNeeded = true;
			if (cfg.needsRebuild() || delta != null) {
				// use a build desacription model to calculate the resources to be cleaned
				// only in case there are some changes to the project sources or build information
				try {
					int flags = BuildDescriptionManager.REBUILD | BuildDescriptionManager.DEPFILES
							| BuildDescriptionManager.DEPS;
					if (delta != null)
						flags |= BuildDescriptionManager.REMOVED;

					outputTrace(curProject.getName(), "using a build description.."); //$NON-NLS-1$

					des = BuildDescriptionManager.createBuildDescription(cfg, getDelta(curProject), flags);

					BuildDescriptionManager.cleanGeneratedRebuildResources(des);
				} catch (Throwable e) {
					// TODO: log error
					outputError(curProject.getName(),
							"error occured while build description calculation: " + e.getLocalizedMessage()); //$NON-NLS-1$
					// in case an error occured, make it behave in the old stile:
					if (cfg.needsRebuild()) {
						// make a full clean if an info needs a rebuild
						clean(new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
						makefileRegenerationNeeded = true;
					} else if (delta != null && !makefileRegenerationNeeded) {
						// Create a delta visitor to detect the build type
						ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(cfg,
								bInfo.getBuildInfo().getManagedProject().getConfigurations());
						delta.accept(visitor);
						if (visitor.shouldBuildFull()) {
							clean(new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
							makefileRegenerationNeeded = true;
						}
					}
				}
			}
		}

		if (makefileRegenerationNeeded) {
			status.setRebuild();
		}
		return status;
	}

	protected MultiStatus performMakefileGeneration(CfgBuildInfo bInfo,
			IManagedBuilderMakefileGenerator generator, BuildStatus buildStatus, IProgressMonitor monitor)
			throws CoreException {
		// Need to report status to the user
		IProject curProject = bInfo.getProject();
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		// Ask the makefile generator to generate any makefiles needed to build delta
		checkCancel(monitor);
		String statusMsg = ManagedMakeMessages
				.getFormattedString("ManagedMakeBuilder.message.update.makefiles", curProject.getName()); //$NON-NLS-1$
		monitor.subTask(statusMsg);

		MultiStatus result;
		if (buildStatus.isRebuild()) {
			result = generator.regenerateMakefiles();
		} else {
			result = generator.generateMakefiles(getDelta(curProject));
		}

		return result;
	}

	// private MultiStatus createMultiStatus(int severity){
	// return new MultiStatus(
	// ManagedBuilderCorePlugin.getUniqueIdentifier(),
	// severity,
	// new String(),
	// null);
	// }

	protected void initializeGenerator(IManagedBuilderMakefileGenerator generator, int kind,
			CfgBuildInfo bInfo, IProgressMonitor monitor) {
		if (generator instanceof IManagedBuilderMakefileGenerator2) {
			IManagedBuilderMakefileGenerator2 gen2 = (IManagedBuilderMakefileGenerator2) generator;
			gen2.initialize(kind, bInfo.getConfiguration(), bInfo.getBuilder(), monitor);
		} else {
			generator.initialize(bInfo.getProject(), bInfo.getBuildInfo(), monitor);
		}

	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		if (DEBUG_EVENTS)
			printEvent(IncrementalProjectBuilder.CLEAN_BUILD, null);

		IProject curProject = getProject();

		if (!isCdtProjectCreated(curProject))
			return;

		IBuilder[] builders = ManagedBuilderCorePlugin.createBuilders(curProject, null);
		for (IBuilder builder : builders) {
			CfgBuildInfo bInfo = new CfgBuildInfo(builder, true);
			clean(bInfo, monitor);
		}
	}

	@Override
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		super.addMarker(file, lineNumber, errorDesc, severity, errorVar);
		if (severity == IStatus.ERROR)
			fBuildErrOccured = true;
	}

	@Override
	public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
		super.addMarker(problemMarkerInfo);
		if (problemMarkerInfo.severity == IStatus.ERROR)
			fBuildErrOccured = true;
	}

	protected void clean(CfgBuildInfo bInfo, IProgressMonitor monitor) throws CoreException {
		if (shouldBuild(CLEAN_BUILD, bInfo.getBuilder())) {
			BuildStateManager bsMngr = BuildStateManager.getInstance();
			IProject project = bInfo.getProject();
			IConfiguration cfg = bInfo.getConfiguration();
			IProjectBuildState pbs = bsMngr.getProjectBuildState(project);
			IConfigurationBuildState cbs = pbs.getConfigurationBuildState(cfg.getId(), false);
			if (cbs != null) {
				pbs.removeConfigurationBuildState(cfg.getId());
				bsMngr.setProjectBuildState(project, pbs);
			}

			if (!cfg.getEditableBuilder().isManagedBuildOn()) {
				performExternalClean(bInfo, false, monitor);
			} else {
				boolean programmatically = true;
				IPath path = ManagedBuildManager.getBuildFullPath(cfg, bInfo.getBuilder());
				IResource rc = path != null ? ResourcesPlugin.getWorkspace().getRoot().findMember(path)
						: null;

				if (path == null || (rc != null && rc.getType() != IResource.FILE)) {
					if (!cfg.getEditableBuilder().isInternalBuilder()) {
						fBuildErrOccured = false;
						try {
							performExternalClean(bInfo, false, monitor);
						} catch (CoreException e) {
							fBuildErrOccured = true;
						}
						if (!fBuildErrOccured)
							programmatically = false;
					}

					if (programmatically) {
						try {
							cleanWithInternalBuilder(bInfo, monitor);
						} catch (CoreException e) {
							cleanProgrammatically(bInfo, monitor);
						}
					}
				}
			}
		}

	}

	protected void performExternalClean(final CfgBuildInfo bInfo, boolean separateJob,
			IProgressMonitor monitor) throws CoreException {
		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		final ISchedulingRule rule = ruleFactory.modifyRule(bInfo.getProject());

		if (separateJob) {
			Job backgroundJob = new Job("CDT Common Builder") { //$NON-NLS-1$
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

							@Override
							public void run(IProgressMonitor monitor) throws CoreException {
								bInfo.fBuilder.getBuildRunner().invokeBuild(CLEAN_BUILD, bInfo.getProject(),
										bInfo.getConfiguration(), bInfo.getBuilder(), bInfo.getConsole(),
										CommonBuilder.this, CommonBuilder.this, monitor);
							}
						}, rule, IWorkspace.AVOID_UPDATE, monitor);
					} catch (CoreException e) {
						return e.getStatus();
					}
					IStatus returnStatus = Status.OK_STATUS;
					return returnStatus;
				}

			};

			backgroundJob.setRule(rule);
			backgroundJob.schedule();
		} else {
			bInfo.fBuilder.getBuildRunner().invokeBuild(CLEAN_BUILD, bInfo.getProject(),
					bInfo.getConfiguration(), bInfo.getBuilder(), bInfo.getConsole(), this, this, monitor);
		}

	}

	protected boolean shouldCleanProgrammatically(CfgBuildInfo bInfo) {
		if (!bInfo.getBuilder().isManagedBuildOn())
			return false;
		return true;
		// IConfiguration cfg = builder.getParent().getParent();
		// IPath path = ManagedBuildManager.getBuildFullPath(cfg, builder);
		// if(path == null)
		// return false;
		//
		// return cfg.getOwner().getProject().getFullPath().isPrefixOf(path);
	}

	protected void cleanWithInternalBuilder(CfgBuildInfo bInfo, IProgressMonitor monitor)
			throws CoreException {
		// referencedProjects = getProject().getReferencedProjects();
		IProject project = bInfo.getProject();
		outputTrace(project.getName(), "Clean build with Internal Builder requested"); //$NON-NLS-1$
		IConfiguration configuration = bInfo.getConfiguration();
		int flags = BuildDescriptionManager.DEPFILES;
		BuildDescription des = (BuildDescription) BuildDescriptionManager
				.createBuildDescription(configuration, null, null, flags);

		IBuildStep cleanStep = des.getCleanStep();

		StepBuilder sBuilder = new StepBuilder(cleanStep, null, null);

		BuildRunnerHelper buildRunnerHelper = new BuildRunnerHelper(project);
		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask("", //$NON-NLS-1$
					TICKS_STREAM_PROGRESS_MONITOR + TICKS_EXECUTE_COMMAND + TICKS_REFRESH_PROJECT);

			IConsole console = bInfo.getConsole();

			IBuilder builder = bInfo.getBuilder();
			String[] errorParsers = builder.getErrorParsers();
			URI workingDirectoryURI = ManagedBuildManager.getBuildLocationURI(configuration, builder);
			ErrorParserManager epm = new ErrorParserManager(project, workingDirectoryURI, this, errorParsers);

			buildRunnerHelper.prepareStreams(epm, null, console,
					new SubProgressMonitor(monitor, TICKS_STREAM_PROGRESS_MONITOR));
			OutputStream stdout = buildRunnerHelper.getOutputStream();
			OutputStream stderr = buildRunnerHelper.getErrorStream();

			String cfgName = configuration.getName();
			String toolchainName = configuration.getToolChain().getName();
			boolean isConfigurationSupported = configuration.isSupported();

			buildRunnerHelper.greeting(CLEAN_BUILD, cfgName, toolchainName, isConfigurationSupported);
			int status = sBuilder.build(stdout, stderr,
					new SubProgressMonitor(monitor, TICKS_EXECUTE_COMMAND));
			buildRunnerHelper.close();
			buildRunnerHelper.goodbye();

			if (status != ICommandLauncher.ILLEGAL_COMMAND) {
				buildRunnerHelper.refreshProject(cfgName,
						new SubProgressMonitor(monitor, TICKS_REFRESH_PROJECT));
			}

			// Throw a core exception indicating that the clean command failed
			if (status == IBuildModelBuilder.STATUS_ERROR_LAUNCH) {
				Status st = new Status(IStatus.INFO, ManagedBuilderCorePlugin.PLUGIN_ID,
						"Failed to execute delete command"); //$NON-NLS-1$
				throw new CoreException(st);
			}
		} catch (Exception e) {
			String msg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.error.build", //$NON-NLS-1$
					new String[] { project.getName(), configuration.getName() });
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, msg, e));
		} finally {
			try {
				buildRunnerHelper.close();
			} catch (IOException e) {
				ManagedBuilderCorePlugin.log(e);
			}
			monitor.done();
		}

	}

	protected void cleanProgrammatically(CfgBuildInfo bInfo, IProgressMonitor monitor) throws CoreException {
		// referencedProjects = getProject().getReferencedProjects();
		IProject project = bInfo.getProject();
		outputTrace(project.getName(), "Clean build requested"); //$NON-NLS-1$
		IBuilder builder = bInfo.getBuilder();
		IConfiguration configuration = bInfo.getConfiguration();
		IPath buildPath = ManagedBuildManager.getBuildFullPath(configuration, builder);
		if (buildPath == null) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(),
					ManagedMakeMessages.getResourceString("CommonBuilder.0"))); //$NON-NLS-1$
		}

		IPath projectFullPath = project.getFullPath();
		if (!projectFullPath.isPrefixOf(buildPath)) {
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(),
					ManagedMakeMessages.getResourceString("CommonBuilder.16"))); //$NON-NLS-1$
		}

		IWorkspace workspace = CCorePlugin.getWorkspace();
		IResource rc = workspace.getRoot().findMember(buildPath);
		if (rc != null) {
			if (rc.getType() != IResource.FOLDER) {
				throw new CoreException(
						new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(),
								ManagedMakeMessages.getResourceString("CommonBuilder.12"))); //$NON-NLS-1$
			}

			IFolder buildDir = (IFolder) rc;
			if (!buildDir.isAccessible()) {
				outputError(buildDir.getName(), "Could not delete the build directory"); //$NON-NLS-1$
				throw new CoreException(
						new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(),
								ManagedMakeMessages.getResourceString("CommonBuilder.13"))); //$NON-NLS-1$
			}

			BuildRunnerHelper buildRunnerHelper = new BuildRunnerHelper(project);
			try {
				if (monitor == null) {
					monitor = new NullProgressMonitor();
				}
				monitor.beginTask("", TICKS_STREAM_PROGRESS_MONITOR + TICKS_DELETE_OUTPUTS); //$NON-NLS-1$

				// try the brute force approach first
				String status = ManagedMakeMessages.getFormattedString(
						"ManagedMakeBuilder.message.clean.deleting.output", buildDir.getName()); //$NON-NLS-1$
				monitor.subTask(status);

				IConsole console = bInfo.getConsole();

				String[] errorParsers = builder.getErrorParsers();
				URI workingDirectoryURI = ManagedBuildManager.getBuildLocationURI(configuration, builder);
				ErrorParserManager epm = new ErrorParserManager(project, workingDirectoryURI, this,
						errorParsers);

				buildRunnerHelper.prepareStreams(epm, null, console,
						new SubProgressMonitor(monitor, TICKS_STREAM_PROGRESS_MONITOR));

				String cfgName = configuration.getName();
				String toolchainName = configuration.getToolChain().getName();
				boolean isConfigurationSupported = configuration.isSupported();

				buildRunnerHelper.greeting(CLEAN_BUILD, cfgName, toolchainName, isConfigurationSupported);
				workspace.delete(new IResource[] { buildDir }, true,
						new SubProgressMonitor(monitor, TICKS_DELETE_OUTPUTS));
				buildRunnerHelper.close();
				buildRunnerHelper.goodbye();

			} catch (Exception e) {
				String msg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.error.build", //$NON-NLS-1$
						new String[] { project.getName(), configuration.getName() });
				throw new CoreException(
						new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, msg, e));
			} finally {
				try {
					buildRunnerHelper.close();
				} catch (IOException e) {
					ManagedBuilderCorePlugin.log(e);
				}
				monitor.done();
			}
		}
	}

	/**
	 * Check whether the build has been canceled.
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();
	}

	protected boolean shouldBuild(int kind, IMakeBuilderInfo info) {
		switch (kind) {
		case IncrementalProjectBuilder.AUTO_BUILD:
			return info.isAutoBuildEnable();
		case IncrementalProjectBuilder.INCREMENTAL_BUILD: // now treated as the same!
		case IncrementalProjectBuilder.FULL_BUILD:
			return info.isFullBuildEnabled() | info.isIncrementalBuildEnabled();
		case IncrementalProjectBuilder.CLEAN_BUILD:
			return info.isCleanBuildEnabled();
		}
		return true;
	}

	/**
	 * Only lock the workspace is this is a ManagedBuild, or this project references others.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ISchedulingRule getRule(int trigger, Map args) {
		IResource WR_rule = ResourcesPlugin.getWorkspace().getRoot();
		if (needAllConfigBuild() || !isCdtProjectCreated(getProject()))
			return WR_rule;

		// Get the builders to run
		IBuilder builders[] = ManagedBuilderCorePlugin.createBuilders(getProject(), args);
		// Be pessimistic if we referenced other configs
		if (getReferencedConfigs(builders).length > 0)
			return WR_rule;
		// If any builder isManaged => pessimistic
		for (IBuilder builder : builders) {
			if (builder.isManagedBuildOn())
				return WR_rule;
		}

		// Success!
		return null;
	}
}
