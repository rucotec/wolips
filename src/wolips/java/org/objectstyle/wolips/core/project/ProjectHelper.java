/* ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0 
 *
 * Copyright (c) 2002 The ObjectStyle Group 
 * and individual authors of the software.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne" 
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */
package org.objectstyle.wolips.core.project;
import java.lang.reflect.InvocationTargetException;
import java.org.objectstyle.wolips.logging.WOLipsLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.objectstyle.wolips.core.plugin.IWOLipsPluginConstants;
import org.objectstyle.wolips.core.plugin.WOLipsPlugin;
import org.objectstyle.woproject.env.Environment;
/**
 * @author uli
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 * @deprecated use WOLipsProject's methods instead
 */
public class ProjectHelper implements IWOLipsPluginConstants {
	public static String WOGENERATOR_ID =
		"org.objectstyle.wolips.wogenerator";
		public static String WOFRAMEWORK_BUILDER_ID =
		"org.objectstyle.wolips.woframeworkbuilder";
	public static String WOAPPLICATION_BUILDER_ID =
		"org.objectstyle.wolips.woapplicationbuilder";
	public static String JAVA_BUILDER_ID = "org.eclipse.jdt.core.javabuilder";
	public static final int NotFound = -1;
	/**
	 * Constructor for ProjectHelper.
	 */
	public ProjectHelper() {
		super();
	}
	/**
	 * Method removeJavaBuilder.
	 * @param project
	 */
	public static void removeBuilder(IProject aProject, String aBuilder)
		throws CoreException {
		IProjectDescription desc = null;
		ICommand[] coms = null;
		ArrayList comList = null;
		List tmp = null;
		ICommand[] newCom = null;
		try {
			desc = aProject.getDescription();
			coms = desc.getBuildSpec();
			comList = new ArrayList();
			tmp = Arrays.asList(coms);
			comList.addAll(tmp);
			boolean foundJBuilder = false;
			for (int i = 0; i < comList.size(); i++) {
				if (((ICommand) comList.get(i))
					.getBuilderName()
					.equals(aBuilder)) {
					comList.remove(i);
					foundJBuilder = true;
				}
			}
			if (foundJBuilder) {
				newCom = new ICommand[comList.size()];
				for (int i = 0; i < comList.size(); i++) {
					newCom[i] = (ICommand) comList.get(i);
				}
				desc.setBuildSpec(newCom);
				aProject.setDescription(desc, null);
			}
		} finally {
			desc = null;
			coms = null;
			comList = null;
			tmp = null;
			newCom = null;
		}
	}
	/**
	 * Method installBuilder.
	 * @param aProject
	 * @param aBuilder
	 * @throws CoreException
	 */
	public static void installBuilder(IProject aProject, String aBuilder)
		throws CoreException {
		IProjectDescription desc = null;
		ICommand[] coms = null;
		ICommand[] newIc = null;
		ICommand command = null;
		try {
			desc = aProject.getDescription();
			coms = desc.getBuildSpec();
			boolean foundJBuilder = false;
			for (int i = 0; i < coms.length; i++) {
				if (coms[i].getBuilderName().equals(aBuilder)) {
					foundJBuilder = true;
				}
			}
			if (!foundJBuilder) {
				newIc = null;
				command = desc.newCommand();
				command.setBuilderName(aBuilder);
				newIc = new ICommand[coms.length + 1];
				System.arraycopy(coms, 0, newIc, 0, coms.length);
				newIc[coms.length] = command;
				desc.setBuildSpec(newIc);
				aProject.setDescription(desc, null);
			}
		} finally {
			desc = null;
			coms = null;
			newIc = null;
			command = null;
		}
	}
	/**
	 * Method addWOFrameworkStuffToJavaProject.
	 * @param aProject
	 * @param aMonitor
	 * @throws CoreException
	 */
	public static void addWOFrameworkStuffToJavaProject(
		IProject aProject,
		IProgressMonitor aMonitor)
		throws CoreException {
		ProjectHelper.addCommonStuff(aProject, aMonitor);
	}
	/**
	 * Method addWOApplicationStuffToJavaProject.
	 * @param aProject
	 * @param aMonitor
	 * @throws CoreException
	 */
	public static void addWOApplicationStuffToJavaProject(
		IProject aProject,
		IProgressMonitor aMonitor)
		throws CoreException {
		ProjectHelper.addCommonStuff(aProject, aMonitor);
	}
	/**
	 * Method addCommonStuff.
	 * @param aProject
	 * @param aMonitor
	 * @throws CoreException
	 */
	private static void addCommonStuff(
		IProject aProject,
		IProgressMonitor aMonitor)
		throws CoreException {
		ProjectHelper.createFolder("Resources", aProject, aMonitor);
		ProjectHelper.createFolder("WSResources", aProject, aMonitor);
	}
	/**
	 * Method createFolder.
	 * @param aFolderName
	 * @param aProject
	 * @param aMonitor
	 * @throws CoreException
	 */
	private static void createFolder(
		String aFolderName,
		IProject aProject,
		IProgressMonitor aMonitor)
		throws CoreException {
		IFolder folder = null;
		IPath path = null;
		try {
			folder = aProject.getFolder(aFolderName);
			path = folder.getFullPath();
			if (!folder.exists()) {
				CoreUtility.createFolder(folder, true, true, aMonitor);
			}
		} finally {
			folder = null;
			path = null;
		}
	}
	/**
	 * Method isWOGeneratorInstalled.
	 * @param aProject
	 * @return boolean
	 */
	public static boolean isWOGeneratorInstalled(IProject aProject) {
		return ProjectHelper.isBuilderInstalled(
			aProject,
			ProjectHelper.WOGENERATOR_ID);
	}
	/**
	 * Method isWOAppBuilderInstalled.
	 * @param aProject
	 * @return boolean
	 */
	public static boolean isWOAppBuilderInstalled(IProject aProject) {
		return ProjectHelper.isBuilderInstalled(
			aProject,
			ProjectHelper.WOAPPLICATION_BUILDER_ID);
	}
	/**
	 * Method isWOFwBuilderInstalled.
	 * @param aProject
	 * @return boolean
	 */
	public static boolean isWOFwBuilderInstalled(IProject aProject) {
		return ProjectHelper.isBuilderInstalled(
			aProject,
			ProjectHelper.WOFRAMEWORK_BUILDER_ID);
	}
	/**
	 * Method isBuilderInstalled.
	 * @param aProject
	 * @param anID
	 * @return boolean
	 */
	private static boolean isBuilderInstalled(IProject aProject, String anID) {
		try {
			ICommand[] nids = aProject.getDescription().getBuildSpec();
			for (int i = 0; i < nids.length; i++) {
				if (nids[i].getBuilderName().equals(anID))
					return true;
			}
		} catch (Exception anException) {
			WOLipsLog.log(anException);
			return false;
		}
		return false;
	}
	////////////////// source folder stuff
	/**
	 * Method getProjectSourceFolder. Searches classpath source entries for project source folder.
	 * The project source folder is the first found source folder the project container contains.
	 * @param project
	 * @return IContainer found source folder
	 */
	public static IContainer getProjectSourceFolder(IProject project) {
		IClasspathEntry[] classpathEntries;
		IJavaProject javaProject;
		try {
			javaProject = JavaCore.create(project);
			classpathEntries = javaProject.getRawClasspath();
		} catch (JavaModelException e) {
			WOLipsLog.log(e);
			return null;
		}
		for (int i = 0; i < classpathEntries.length; i++) {
			if (IClasspathEntry.CPE_SOURCE
				== classpathEntries[i].getEntryKind()) {
				// source entry found
				if (classpathEntries[i].getPath() != null
					&& classpathEntries[i].getPath().removeLastSegments(1).equals(
						project.getFullPath())) {
					// source folder's parent is project
					// project source folder found
					return project.getWorkspace().getRoot().getFolder(
						classpathEntries[i].getPath());
				}
				/*
				if (classpathEntries[i].getPath() != null
						&& classpathEntries[i].getPath().toString().indexOf(
					"."
				+ IWOLipsPluginConstants.EXT_SUBPROJECT
				+ "."
				+ IWOLipsPluginConstants.EXT_SRC)
				== -1) {
					// non subproject entry found
					if (classpathEntries[i].getPath().segmentCount() > 1) {
							return project.getWorkspace().getRoot().getFolder(
						classpathEntries[i].getPath());
					}
						break;
				}
				*/
			}
		}
		// no source folder found -> create new one
		IFolder projectSourceFolder =
			project.getFolder(IWOLipsPluginConstants.EXT_SRC);
		if (!projectSourceFolder.exists()) {
			try {
				projectSourceFolder.create(true, true, null);
			} catch (CoreException e) {
				WOLipsLog.log(e);
			}
		}
		// add to classpath
		try {
			addNewSourcefolderToClassPath(projectSourceFolder, null);
		} catch (InvocationTargetException e) {
			WOLipsLog.log(e);
		}
		return projectSourceFolder;
	}
	/**
	 * Method getSubprojectSourceFolder. Searches classpath source entries for correspondending
	 * subproject source folder (first found source folder in subproject folder)
	 * @param subprojectFolder
	 * @param forceCreation - create folder if necessary
	 * @return IFolder
	 */
	public static IFolder getSubprojectSourceFolder(
		IFolder subprojectFolder,
		boolean forceCreation) {
		//ensure that the folder is a subproject
		if (!EXT_SUBPROJECT.equals(subprojectFolder.getFileExtension())) {
			IFolder parentFolder =
				getParentFolderWithPBProject(subprojectFolder);
			//this belongs to the project and not a subproject
			if (parentFolder == null)
				return subprojectFolder.getProject().getFolder(
					ProjectHelper
						.getProjectSourceFolder(subprojectFolder.getProject())
						.getProjectRelativePath());
			subprojectFolder = parentFolder;
		}
		List subprojectFolders =
			getSubProjectsSourceFolder(subprojectFolder.getProject());
		for (int i = 0; i < subprojectFolders.size(); i++) {
			if (((IFolder) subprojectFolders.get(i))
				.getFullPath()
				.removeLastSegments(1)
				.equals(subprojectFolder.getFullPath())) {
				return (IFolder) subprojectFolders.get(i);
			}
		}
		if (forceCreation) {
			// no folder found - create new source folder
			IFolder subprojectSourceFolder =
				subprojectFolder.getProject().getFolder(
					subprojectFolder.getName()
						+ "/"
						+ IWOLipsPluginConstants.EXT_SRC);
			if (!subprojectSourceFolder.exists()) {
				try {
					subprojectSourceFolder.create(true, true, null);
				} catch (CoreException e) {
					WOLipsLog.log(e);
				}
			} // add folder to classpath
			try {
				addNewSourcefolderToClassPath(subprojectSourceFolder, null);
			} catch (InvocationTargetException e) {
				WOLipsLog.log(e);
			}
			return subprojectSourceFolder;
		}
		return null;
	}
	/**
	 * Method getSubProjectsSourceFolder. Searches classpath source entries for all source
	 * folders who's parents are NOT project.
	 * @param project
	 * @return List
	 */
	public static List getSubProjectsSourceFolder(IProject project) {
		IClasspathEntry[] classpathEntries;
		IJavaProject javaProject;
		ArrayList foundFolders = new ArrayList();
		try {
			javaProject = JavaCore.create(project);
			classpathEntries = javaProject.getRawClasspath();
		} catch (JavaModelException e) {
			WOLipsLog.log(e);
			return null;
		}
		for (int i = 0; i < classpathEntries.length; i++) {
			if (IClasspathEntry.CPE_SOURCE
				== classpathEntries[i].getEntryKind()) {
				// source entry found
				if (classpathEntries[i].getPath() != null
					&& !classpathEntries[i].getPath().removeLastSegments(
						1).equals(
						project.getFullPath())) {
					// source folder's parent is not project
					// project source folder found
					foundFolders.add(
						project.getWorkspace().getRoot().getFolder(
							classpathEntries[i].getPath()));
				}
				/*
				if (classpathEntries[i].getPath() != null
					&& classpathEntries[i].getPath().toString().indexOf(
						"."
							+ IWOLipsPluginConstants.EXT_SUBPROJECT
							+ "/"
							+ IWOLipsPluginConstants.EXT_SRC)
						!= -1) {
					foundFolders.add(
						project.getWorkspace().getRoot().getFolder(
							classpathEntries[i].getPath()));
				}
				*/
			}
		}
		return foundFolders;
	}
	/**
	 * Method addNewSourcefolderToClassPath.
	 * @param newSourceFolder
	 * @param monitor
	 * @throws InvocationTargetException
	 */
	public static void addNewSourcefolderToClassPath(
		IFolder newSourceFolder,
		IProgressMonitor monitor)
		throws InvocationTargetException {
		// add source classpath entry for project
		IJavaProject actualJavaProject = null;
		IClasspathEntry[] oldClassPathEntries = null;
		;
		IClasspathEntry[] newClassPathEntries = null;
		try {
			actualJavaProject = JavaCore.create(newSourceFolder.getProject());
			oldClassPathEntries = actualJavaProject.getRawClasspath();
		} catch (JavaModelException e) {
			actualJavaProject = null;
			oldClassPathEntries = null;
			throw new InvocationTargetException(e);
		}
		newClassPathEntries =
			new IClasspathEntry[oldClassPathEntries.length + 1];
		System.arraycopy(
			oldClassPathEntries,
			0,
			newClassPathEntries,
			1,
			oldClassPathEntries.length);
		newClassPathEntries[0] =
			JavaCore.newSourceEntry(newSourceFolder.getFullPath());
		try {
			actualJavaProject.setRawClasspath(newClassPathEntries, monitor);
		} catch (JavaModelException e) {
			actualJavaProject = null;
			oldClassPathEntries = null;
			newClassPathEntries = null;
			throw new InvocationTargetException(e);
		}
	}
	/**
	 * Method removeSourcefolderFromClassPath.
	 * @param folderToRemove
	 * @param monitor
	 * @throws InvocationTargetException
	 */
	public static void removeSourcefolderFromClassPath(
		IFolder folderToRemove,
		IProgressMonitor monitor)
		throws InvocationTargetException {
		if (folderToRemove != null) {
			IJavaProject actualJavaProject =
				JavaCore.create(folderToRemove.getProject());
			IClasspathEntry[] oldClassPathEntries;
			try {
				oldClassPathEntries = actualJavaProject.getRawClasspath();
			} catch (JavaModelException e) {
				actualJavaProject = null;
				oldClassPathEntries = null;
				throw new InvocationTargetException(e);
			}
			IClasspathEntry[] newClassPathEntries =
				new IClasspathEntry[oldClassPathEntries.length - 1];
			int offSet = 0;
			for (int i = 0; i < oldClassPathEntries.length; i++) {
				if (IClasspathEntry.CPE_SOURCE
					== oldClassPathEntries[i].getEntryKind()
					&& oldClassPathEntries[i].getPath().equals(
						folderToRemove.getFullPath())) {
					offSet = 1;
				} else {
					newClassPathEntries[i - offSet] = oldClassPathEntries[i];
				}
			}
			if (offSet != 0) {
				try {
					actualJavaProject.setRawClasspath(
						newClassPathEntries,
						monitor);
				} catch (JavaModelException e) {
					actualJavaProject = null;
					oldClassPathEntries = null;
					newClassPathEntries = null;
					throw new InvocationTargetException(e);
				}
			}
		}
	}
	/**
	 * Method addFrameworkListToClasspathEntries.
	 * @param frameworkList
	 * @param projectToUpdate
	 * @return IClasspathEntry[]
	 * @throws JavaModelException
	 */
	public static IClasspathEntry[] addFrameworkListToClasspathEntries(
		List frameworkList,
		IJavaProject projectToUpdate)
		throws JavaModelException {
		IClasspathEntry[] oldClasspathEntries =
			projectToUpdate.getResolvedClasspath(true);
		IPath nextRootAsPath = new Path(WOLipsPlugin.getDefault().getWOEnvironment().getWOVariables().systemRoot());
		ArrayList classpathEntries = new ArrayList(frameworkList.size());
		IPath frameworkPath;
		String jarName;
		String frameworkName;
		int frameworkExtIndex;
		for (int i = 0; i < frameworkList.size(); i++) {
			frameworkName = (String) frameworkList.get(i);
			// check for framework extentsion
			frameworkExtIndex = frameworkName.indexOf(EXT_FRAMEWORK);
			if (frameworkExtIndex == -1
				|| frameworkExtIndex == 0) { // invalid framework name
				continue;
			}
			jarName =
				frameworkName.substring(0, frameworkExtIndex - 1).toLowerCase()
					+ ".jar";
			// check for root
			frameworkPath = new Path(WOLipsPlugin.getDefault().getWOEnvironment().getWOVariables().libraryDir());
			frameworkPath = frameworkPath.append("Frameworks");
			frameworkPath = frameworkPath.append(frameworkName);
			if (!frameworkPath.toFile().isDirectory()) {
				frameworkPath = new Path(WOLipsPlugin.getDefault().getWOEnvironment().getWOVariables().localLibraryDir());
				frameworkPath = frameworkPath.append("Frameworks");
				frameworkPath = frameworkPath.append(frameworkName);
			}
			if (!frameworkPath.toFile().isDirectory()) { // invalid path
				continue;
			} // check for jar existance
			int j = 0;
			frameworkPath = frameworkPath.append("Resources/Java/");
			String[] frameJarDirContent = frameworkPath.toFile().list();
			for (j = 0; j < frameJarDirContent.length; j++) {
				if (jarName.equals(frameJarDirContent[j].toLowerCase())) {
					// get case sensitive jar name
					jarName = frameJarDirContent[j];
					break;
				}
			}
			if (j == frameJarDirContent.length) { // jar doesn't exists
				continue;
			} // add case-sensitive jar name
			frameworkPath = frameworkPath.append(jarName);
			// check for existing classpath entries
			for (j = 0; j < oldClasspathEntries.length; j++) {
				if (oldClasspathEntries[j].getPath().equals(frameworkPath)) {
					break;
				}
			}
			if (j != oldClasspathEntries.length) { // entry already set
				continue;
			} // determine if new class path begins with next root
			if ((frameworkPath.segmentCount() > nextRootAsPath.segmentCount())
				&& frameworkPath
					.removeLastSegments(
						frameworkPath.segmentCount()
							- nextRootAsPath.segmentCount())
					.equals(nextRootAsPath)) {
				// replace beginning of class path with next root
				frameworkPath =
					new Path(Environment.NEXT_ROOT).append(
						frameworkPath.removeFirstSegments(
							nextRootAsPath.segmentCount()));
				// set path as variable entry			
				classpathEntries.add(
					JavaCore.newVariableEntry(frameworkPath, null, null));
			} else {
				classpathEntries.add(
					JavaCore.newLibraryEntry(frameworkPath, null, null));
			}
		} // build new class path entry array
		oldClasspathEntries = projectToUpdate.getRawClasspath();
		IClasspathEntry[] newClasspathEntries =
			new IClasspathEntry[classpathEntries.size()
				+ oldClasspathEntries.length];
		for (int i = 0; i < oldClasspathEntries.length; i++) {
			newClasspathEntries[i] = oldClasspathEntries[i];
		}
		for (int i = 0; i < classpathEntries.size(); i++) {
			newClasspathEntries[i + oldClasspathEntries.length] =
				(IClasspathEntry) classpathEntries.get(i);
		}
		return newClasspathEntries;
	} ///////////////////////////////// builder stuff ////////////////////////
	/**
	 * Method positionForBuilder.
	 * @param aProject
	 * @param aBuilder
	 * @return int
	 * @throws CoreException
	 */
	public static int positionForBuilder(IProject aProject, String aBuilder)
		throws CoreException {
		IProjectDescription desc = null;
		ICommand[] coms = null;
		try {
			desc = aProject.getDescription();
			coms = desc.getBuildSpec();
			for (int i = 0; i < coms.length; i++) {
				if (coms[i].getBuilderName().equals(aBuilder))
					return i;
			}
		} finally {
			desc = null;
			coms = null;
		}
		return ProjectHelper.NotFound;
	}
	/**
	 * Method installBuilderAtPosition.
	 * @param aProject
	 * @param aBuilder
	 * @param installPos
	 * @param arguments
	 * @throws CoreException
	 */
	public static void installBuilderAtPosition(
		IProject aProject,
		String aBuilder,
		int installPos,
		Map arguments)
		throws CoreException {
		IProjectDescription desc = aProject.getDescription();
		ICommand[] coms = desc.getBuildSpec();
		if (arguments == null)
			arguments = new HashMap();
		for (int i = 0; i < coms.length; i++) {
			if (coms[i].getBuilderName().equals(aBuilder)
				&& coms[i].getArguments().equals(arguments))
				return;
		}
		ICommand[] newIc = null;
		ICommand command = desc.newCommand();
		command.setBuilderName(aBuilder);
		command.setArguments(arguments);
		newIc = new ICommand[coms.length + 1];
		if (installPos <= 0) {
			System.arraycopy(coms, 0, newIc, 1, coms.length);
			newIc[0] = command;
		} else if (installPos >= coms.length) {
			System.arraycopy(coms, 0, newIc, 0, coms.length);
			newIc[coms.length] = command;
		} else {
			System.arraycopy(coms, 0, newIc, 0, installPos);
			newIc[installPos] = command;
			System.arraycopy(
				coms,
				installPos,
				newIc,
				installPos + 1,
				coms.length - installPos);
		}
		desc.setBuildSpec(newIc);
		aProject.setDescription(desc, null);
	}
	/**
	 * Method getParentFolderWithPBProject.
	 * @param aFolder
	 * @return IFolder or one the parents with PB.project if one is found. Null
	 * is returned when Projects PB.project is found
	 */
	public static IFolder getParentFolderWithPBProject(IFolder aFolder) {
		IFolder findFolder = aFolder;
		while ((findFolder.findMember(IWOLipsPluginConstants.PROJECT_FILE_NAME)
			== null)
			&& (findFolder.getParent() != null)
			&& (findFolder.getParent().getType() != IProject.PROJECT)) {
			findFolder = (IFolder) findFolder.getParent();
		}
		if (findFolder.getParent() == null)
			return null;
		if (findFolder.findMember(IWOLipsPluginConstants.PROJECT_FILE_NAME)
			!= null)
			return findFolder;
		return null;
	}
}
