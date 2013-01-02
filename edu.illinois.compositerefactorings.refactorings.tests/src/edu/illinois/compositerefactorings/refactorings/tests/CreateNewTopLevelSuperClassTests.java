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

	private void validatePassingTest(List<String> subclassNames, List<String> otherClassNames, String newSuperClassName) throws Exception {
		final Map<String, ICompilationUnit> units= new HashMap<String, ICompilationUnit>();
		List<IType> subtypes= new ArrayList<IType>();
		for (String subclassName : subclassNames) {
			IType subtype= getClassFromTestFile(getPackageP(), subclassName);
			subtypes.add(subtype);
			units.put(subclassName, subtype.getCompilationUnit());
		}
		IPackageFragment packageFragment= subtypes.get(0).getPackageFragment();
		units.putAll(getCompilationUnits(packageFragment, otherClassNames));
		final CreateNewTopLevelSuperClassDescriptor descriptor= new CreateNewTopLevelSuperClassDescriptor();
		descriptor.setNewClassName(newSuperClassName);
		descriptor.setType(subtypes.get(0));
		descriptor.setSubTypes(subtypes.toArray(new IType[] {}));
		createPerformCheckRefactoring(descriptor);

		units.put(newSuperClassName, getPackageP().getCompilationUnit(newSuperClassName + JavaModelUtil.DEFAULT_CU_SUFFIX));
		compareCompilationUnits(units);
	}

	public void test0() throws Exception {
		validatePassingTest(Arrays.asList("C"), new ArrayList<String>(), "D");
	}

	public void test1() throws Exception {
		validatePassingTest(Arrays.asList("C"), Arrays.asList("D"), "E");
	}

	public void test2() throws Exception {
		validatePassingTest(Arrays.asList("C"), Arrays.asList("D"), "E");
	}

	public void test3() throws Exception {
		validatePassingTest(Arrays.asList("C"), Arrays.asList("D"), "E");
	}

	public void test4() throws Exception {
		validatePassingTest(Arrays.asList("C", "D"), new ArrayList<String>(), "E");
	}

	public void test5() throws Exception {
		validatePassingTest(Arrays.asList("C", "D"), Arrays.asList("E"), "F");
	}

}
