/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.tests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTestSetup;

import edu.illinois.compositerefactorings.refactorings.usesupertypeininstanceof.UseSuperTypeInInstanceOfDescriptor;

@SuppressWarnings("restriction")
public class UseSuperTypeInInstanceOfTests extends RefactoringTest {

	private static final Class<UseSuperTypeInInstanceOfTests> clazz= UseSuperTypeInInstanceOfTests.class;

	private static final String REFACTORING_PATH= "UseSuperTypeInInstanceOf/";

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

	private void validatePassingTest(String className, List<String> cuNames, String superTypeFullName, boolean replaceInstanceOf) throws Exception {
		final Map<String, ICompilationUnit> units= new HashMap<String, ICompilationUnit>();
		IType subtype= getClassFromTestFile(getPackageP(), className);
		units.put(className, subtype.getCompilationUnit());
		IPackageFragment packageFragment= subtype.getPackageFragment();
		units.putAll(getCompilationUnits(packageFragment, cuNames));

		final IType superType= subtype.getJavaProject().findType(superTypeFullName, (IProgressMonitor)null);
		final UseSuperTypeInInstanceOfDescriptor descriptor= new UseSuperTypeInInstanceOfDescriptor();
		descriptor.setSubtype(subtype);
		descriptor.setSupertype(superType);
		createPerformCheckRefactoring(descriptor);
		compareCompilationUnits(units);
	}

	private void validatePassingTest(String className, List<String> cuNames, String superTypeFullName) throws Exception {
		validatePassingTest(className, cuNames, superTypeFullName, false);
	}

	public void test0() throws Exception {
		validatePassingTest("C", Arrays.<String> asList(), "p.I");
	}

	public void test1() throws Exception {
		validatePassingTest("C", Arrays.asList("I"), "p.I");
	}

	public void test2() throws Exception {
		validatePassingTest("C", Arrays.asList("D"), "p.D");
	}

}
