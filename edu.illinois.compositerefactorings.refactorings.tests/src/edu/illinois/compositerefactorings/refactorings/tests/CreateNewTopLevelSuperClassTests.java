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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTest;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTestSetup;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import edu.illinois.compositerefactorings.refactorings.createnewtoplevelsuperclass.CreateNewTopLevelSuperClassDescriptor;

@SuppressWarnings("restriction")
public class CreateNewTopLevelSuperClassTests extends RefactoringTest {

	private static final Class<CreateNewTopLevelSuperClassTests> clazz= CreateNewTopLevelSuperClassTests.class;

	private static final String REFACTORING_PATH= "CreateNewTopLevelSuperClass/";

	public CreateNewTopLevelSuperClassTests(String name) {
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

	private void validatePassingTest(String className, List<String> cuNames, String newSuperClassName) throws Exception {
		final IType type= getClassFromTestFile(getPackageP(), className);
		final Map<String, ICompilationUnit> units= new HashMap<String, ICompilationUnit>();
		for (String cuName : cuNames) {
			if (cuName.equals(type.getCompilationUnit().findPrimaryType().getElementName())) {
				units.put(cuName, type.getCompilationUnit());
			} else {
				units.put(cuName, createCUfromTestFile(type.getPackageFragment(), cuName));
			}
		}
		final CreateNewTopLevelSuperClassDescriptor descriptor= new CreateNewTopLevelSuperClassDescriptor();
		descriptor.setClassName(newSuperClassName);
		descriptor.setType(type);
		final RefactoringStatus status= new RefactoringStatus();
		final Refactoring refactoring= descriptor.createRefactoring(status);
		assertTrue("status should be ok", status.isOK());
		assertNotNull("refactoring should not be null", refactoring);
		assertEquals("was supposed to pass", null, performRefactoring(refactoring));

		units.put(newSuperClassName, getPackageP().getCompilationUnit(newSuperClassName + JavaModelUtil.DEFAULT_CU_SUFFIX));
		for (Map.Entry<String, ICompilationUnit> entry : units.entrySet()) {
			String cuName= entry.getKey();
			ICompilationUnit unit= entry.getValue();
			String expected= getFileContents(getOutputTestFileName(cuName));
			String actual= unit.getSource();
			String message= "incorrect changes in " + unit.getElementName();
			assertEqualLines(message, expected, actual);
		}
	}

	public void test0() throws Exception {
		validatePassingTest("C", Arrays.asList("C"), "D");
	}

}
