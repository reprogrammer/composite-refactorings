/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.tests;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTestSetup;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import edu.illinois.compositerefactorings.refactorings.usesupertypeininstanceof.UseSupertypeInInstanceOfDescriptor;

@SuppressWarnings("restriction")
public class UseSuperTypeInInstanceOfTests extends RefactoringTest {

	private static final Class clazz= UseSuperTypeInInstanceOfTests.class;

	private static final String REFACTORING_PATH= "UseSupertypeInInstanceOf/";

	public UseSuperTypeInInstanceOfTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new RefactoringTestSetup(new TestSuite(clazz));
	}

	public static Test setUpTest(Test test) {
		return new RefactoringTestSetup(test);
	}

	protected String getRefactoringPath() {
		return REFACTORING_PATH;
	}

	protected void setUp() throws Exception {
		super.setUp();
		StubUtility.setCodeTemplate(CodeTemplateContextType.NEWTYPE_ID,
				"${package_declaration}" +
						System.getProperty("line.separator", "\n") +
						"${" + CodeTemplateContextType.TYPE_COMMENT + "}" +
						System.getProperty("line.separator", "\n") +
						"${type_declaration}", null);

		StubUtility.setCodeTemplate(CodeTemplateContextType.TYPECOMMENT_ID, "/** typecomment template*/", null);
	}

	@Override
	public String getFileContents(String fileName) throws IOException {
		return getContents(getFileInputStream(fileName));
	}

	public static InputStream getFileInputStream(String fileName) throws IOException {
		IPath path= new Path("resources").append(fileName);
		try {
			return Activator.getDefault().getFileInPlugin(path).toURI().toURL().openStream();
		} catch (CoreException e) {
			throw new IOException(e);
		}
	}

	private IType getClassFromTestFile(IPackageFragment pack, String className) throws Exception {
		return getType(createCUfromTestFile(pack, className), className);
	}

	private void validatePassingTest(String className, String[] cuNames, String superTypeFullName, boolean replaceInstanceOf) throws Exception {
		final IType subType= getClassFromTestFile(getPackageP(), className);
		final ICompilationUnit[] units= new ICompilationUnit[cuNames.length];
		for (int i= 0; i < cuNames.length; i++) {
			if (cuNames[i].equals(subType.getCompilationUnit().findPrimaryType().getElementName()))
				units[i]= subType.getCompilationUnit();
			else
				units[i]= createCUfromTestFile(subType.getPackageFragment(), cuNames[i]);

		}
		final IType superType= subType.getJavaProject().findType(superTypeFullName, (IProgressMonitor)null);
		final UseSupertypeInInstanceOfDescriptor descriptor= new UseSupertypeInInstanceOfDescriptor();
		descriptor.setSubtype(subType);
		descriptor.setSupertype(superType);
		final RefactoringStatus status= new RefactoringStatus();
		final Refactoring refactoring= descriptor.createRefactoring(status);
		assertTrue("status should be ok", status.isOK());
		assertNotNull("refactoring should not be null", refactoring);
		assertEquals("was supposed to pass", null, performRefactoring(refactoring));

		for (int i= 0; i < units.length; i++) {
			String expected= getFileContents(getOutputTestFileName(cuNames[i]));
			String actual= units[i].getSource();
			String message= "incorrect changes in " + units[i].getElementName();
			assertEqualLines(message, expected, actual);
		}
	}

	private void validatePassingTest(String className, String[] cuNames, String superTypeFullName) throws Exception {
		validatePassingTest(className, cuNames, superTypeFullName, false);
	}

	public void test0() throws Exception {
		validatePassingTest("A", new String[] { "A" }, "p.I");
	}

}
