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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;
import edu.illinois.compositerefactorings.refactorings.copymembertosubtype.CopyMemberToSubtypeDescriptor;
import edu.illinois.compositerefactorings.refactorings.copymembertosubtype.CopyMemberToSubtypeRefactoringProcessor;

@SuppressWarnings("restriction")
public class CopyMemberToSubtype extends RefactoringBasedStep {

	private static final int MAXIMUM_NUMBER_OF_PROPOSALS= 3;

	public CopyMemberToSubtype(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation) {
		super(context, coveringNode, problemsAtLocation);
	}

	@Override
	protected Collection<? extends IJavaElement> getInputs() {
		return SelectionUtils.getSelectionMemberDeclarations(context, coveringNode);
	}

	/**
	 * The computation of {@code flags} is based on
	 * {@link CopyMemberToSubtypeRefactoringProcessor#createChange(IProgressMonitor)}
	 */
	@Override
	protected Collection<RefactoringDescriptor> getDescriptors(Object input) throws CoreException {
		IMember member= (IMember)input;
		IType supertype= member.getDeclaringType();
		IType[] subtypes= supertype.newTypeHierarchy(getJavaProject(), new NullProgressMonitor()).getSubtypes(supertype);

		Collection<RefactoringDescriptor> descriptors= new ArrayList<RefactoringDescriptor>();
		for (int i= 0; i < MAXIMUM_NUMBER_OF_PROPOSALS && i < subtypes.length; ++i) {
			descriptors.add(createDescriptor(supertype, member, subtypes[i]));
		}
		return descriptors;
	}

	private CopyMemberToSubtypeDescriptor createDescriptor(IType supertype, IMember member, IType subtype) {
		String description= MessageFormat.format(CompositeRefactoringsMessages.CopyMemberToSubstype_description, member.getElementName(), subtype.getElementName());
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, JavaRefactoringDescriptorUtil.elementToHandle(getJavaProject().getElementName(), supertype));
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1, JavaRefactoringDescriptorUtil.elementToHandle(getJavaProject().getElementName(), member));
		arguments.put(CopyMemberToSubtypeDescriptor.ATTRIBUTE_SUBTYPE + 1, JavaRefactoringDescriptorUtil.elementToHandle(getJavaProject().getElementName(), subtype));

		int flags= JavaRefactoringDescriptor.JAR_MIGRATION | JavaRefactoringDescriptor.JAR_REFACTORING | RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;
		try {
			if (supertype.isLocal() || supertype.isAnonymous())
				flags|= JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
		} catch (JavaModelException exception) {
			JavaPlugin.log(exception);
		}
		CopyMemberToSubtypeDescriptor descriptor= new CopyMemberToSubtypeDescriptor(getJavaProject().getElementName(), description, null, arguments, flags);
		return descriptor;
	}

}
