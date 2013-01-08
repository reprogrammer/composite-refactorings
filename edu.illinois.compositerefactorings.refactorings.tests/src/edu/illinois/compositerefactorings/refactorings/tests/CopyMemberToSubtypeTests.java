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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTestSetup;

import edu.illinois.compositerefactorings.refactorings.copymembertosubtype.CopyMemberToSubtypeDescriptor;

@SuppressWarnings("restriction")
public class CopyMemberToSubtypeTests extends RefactoringTest {

	private static final Class<CopyMemberToSubtypeTests> clazz= CopyMemberToSubtypeTests.class;

	private static final String REFACTORING_PATH= "CopyMemberToSubtype/";

	public CopyMemberToSubtypeTests(String name) {
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

	private IMethod getMethod(IType type, String methodName, String[] methodSignature) throws Exception {
		String[][] methodSignatures= new String[1][];
		methodSignatures[0]= methodSignature;
		IMethod[] methods= getMethods(type, new String[] { methodName }, methodSignatures);
		if (methods.length != 1) {
			throw new RuntimeException("Unexpected number of methods matched.");
		}
		return methods[0];
	}

	private IField getField(IType type, String fieldName) throws Exception {
		IField[] fields= getFields(type, new String[] { fieldName });
		if (fields.length != 1) {
			throw new RuntimeException("Unexpected number of fields matched.");
		}
		return fields[0];
	}

	private void validatePassingTest(String superclassName, String subclassName, List<String> otherClassNames, String fieldName) throws Exception {
		IType supertype= getClassFromTestFile(getPackageP(), superclassName);
		IField field= getField(supertype, fieldName);
		validatePassingTest(supertype, superclassName, subclassName, otherClassNames, field);
	}

	private void validatePassingTest(String superclassName, String subclassName, List<String> otherClassNames, String methodName, String[] methodSignature) throws Exception {
		IType supertype= getClassFromTestFile(getPackageP(), superclassName);
		IMethod method= getMethod(supertype, methodName, methodSignature);
		validatePassingTest(supertype, superclassName, subclassName, otherClassNames, method);
	}

	private void validatePassingTest(IType supertype, String superclassName, String subclassName, List<String> otherClassNames, IMember memberToCopy) throws Exception {
		final Map<String, ICompilationUnit> units= new HashMap<String, ICompilationUnit>();
		units.put(superclassName, supertype.getCompilationUnit());
		IType subtype= getClassFromTestFile(getPackageP(), subclassName);
		units.put(subclassName, subtype.getCompilationUnit());
		IPackageFragment packageFragment= supertype.getPackageFragment();
		units.putAll(getCompilationUnits(packageFragment, otherClassNames));

		final CopyMemberToSubtypeDescriptor descriptor= new CopyMemberToSubtypeDescriptor();
		descriptor.setMember(memberToCopy);
		descriptor.setSupertype(supertype);
		descriptor.setSubtype(subtype);
		createPerformCheckRefactoring(descriptor);

		compareCompilationUnits(units);
	}

	public void test0() throws Exception {
		validatePassingTest("C", "D", new ArrayList<String>(), "m", new String[] {});
	}

	public void test1() throws Exception {
		validatePassingTest("C", "D", Arrays.asList("E"), "m", new String[] {});
	}

	public void test2() throws Exception {
		validatePassingTest("C", "D", Arrays.asList("E"), "f");
	}

}
