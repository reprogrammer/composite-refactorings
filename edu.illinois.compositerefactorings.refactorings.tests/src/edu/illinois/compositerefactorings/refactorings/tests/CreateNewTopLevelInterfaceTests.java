/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTestSetup;

import edu.illinois.compositerefactorings.refactorings.createnewtoplevelinterface.CreateNewTopLevelInterfaceDescriptor;

@SuppressWarnings("restriction")
public class CreateNewTopLevelInterfaceTests extends RefactoringTest {

	private static final Class<CreateNewTopLevelInterfaceTests> clazz= CreateNewTopLevelInterfaceTests.class;

	private static final String REFACTORING_PATH= "CreateNewTopLevelInterface/";

	public CreateNewTopLevelInterfaceTests(String name) {
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

	private void validatePassingTest(String subclassName, List<String> otherClassNames, String newInterfaceName) throws Exception {
		final Map<String, ICompilationUnit> units= new HashMap<String, ICompilationUnit>();
		IType subtype= getClassFromTestFile(getPackageP(), subclassName);
		units.put(subclassName, subtype.getCompilationUnit());
		IPackageFragment packageFragment= subtype.getPackageFragment();
		units.putAll(getCompilationUnits(packageFragment, otherClassNames));

		final CreateNewTopLevelInterfaceDescriptor descriptor= new CreateNewTopLevelInterfaceDescriptor();
		descriptor.setNewInterfaceName(newInterfaceName);
		descriptor.setType(subtype);
		createPerformCheckRefactoring(descriptor);

		units.put(newInterfaceName, getPackageP().getCompilationUnit(newInterfaceName + JavaModelUtil.DEFAULT_CU_SUFFIX));
		compareCompilationUnits(units);
	}

	public void test0() throws Exception {
		validatePassingTest("C", new ArrayList<String>(), "I");
	}

	public void test1() throws Exception {
		validatePassingTest("C", Arrays.asList("D"), "I");
	}

	public void test2() throws Exception {
		validatePassingTest("C", Arrays.asList("D"), "I");
	}

	public void test3() throws Exception {
		validatePassingTest("C", Arrays.asList("D"), "I");
	}

	public void test4() throws Exception {
		validatePassingTest("C", Arrays.asList("I"), "J");
	}

}
