/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.steps;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.refactoring.descriptors.PullUpDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;

@SuppressWarnings("restriction")
public class MoveToImmediateSuperclass extends RefactoringBasedStep {

	public MoveToImmediateSuperclass(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation) {
		super(context, coveringNode, problemsAtLocation);
	}

	@Override
	protected Collection<? extends IJavaElement> getInputs() {
		return SelectionUtils.getSelectionMemberDeclarations(context, coveringNode);
	}

	@Override
	protected Collection<RefactoringDescriptor> getDescriptors(Object input) throws CoreException {
		Collection<RefactoringDescriptor> descriptors= new ArrayList<RefactoringDescriptor>();
		IMember member= (IMember)input;
		IType declaringType= member.getDeclaringType();
		IType immediateSuperclass= declaringType.newTypeHierarchy(getJavaProject(), new NullProgressMonitor()).getSuperclass(declaringType);

		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, JavaRefactoringDescriptorUtil.elementToHandle(getJavaProject().getElementName(), immediateSuperclass));
		arguments.put("stubs", String.valueOf(false));
		arguments.put("instanceof", String.valueOf(false));
		arguments.put("replace", String.valueOf(false));
		arguments.put("abstract", String.valueOf(0));
		int numberOfMethodsToRemoveFromSubclasses= 0;
		if (member instanceof IField) {
			numberOfMethodsToRemoveFromSubclasses= 0;
		} else if (member instanceof IMethod) {
			numberOfMethodsToRemoveFromSubclasses= 1;
		}
		arguments.put("delete", Integer.toString(numberOfMethodsToRemoveFromSubclasses));
		arguments.put("pull", String.valueOf(1));
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1, JavaRefactoringDescriptorUtil.elementToHandle(getJavaProject().getElementName(), member));
		if (numberOfMethodsToRemoveFromSubclasses > 0) {
			arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 2, JavaRefactoringDescriptorUtil.elementToHandle(getJavaProject().getElementName(), member));
		}
		String description= MessageFormat.format(CompositeRefactoringsMessages.MoveToImmediateSuperclass_description, member.getElementName(), immediateSuperclass.getElementName());
		PullUpDescriptor descriptor= new PullUpDescriptor(getJavaProject().getElementName(), description, null, arguments, RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		descriptors.add(descriptor);
		return descriptors;
	}

}
