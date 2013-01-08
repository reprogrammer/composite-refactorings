/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.copymembertosubtype;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;

@SuppressWarnings("restriction")
public final class CopyMemberToSubtypeDescriptor extends JavaRefactoringDescriptor {

	/**
	 * This ID should match the of the refactoring contribution used in the corresponding extension.
	 */
	public static final String ID= "edu.illinois.compositerefactorings.copymembertosubtype";

	public static final String ATTRIBUTE_SUBTYPE= "subtype"; //$NON-NLS-1$

	private IMember fMember= null;

	private IType fSupertype= null;

	private IType fSubtype= null;

	public CopyMemberToSubtypeDescriptor() {
		super(ID);
	}

	public CopyMemberToSubtypeDescriptor(String project, String description, String comment, Map<String, String> arguments, int flags) {
		super(ID, project, description, comment, arguments, flags);
		fSupertype= (IType)JavaRefactoringDescriptorUtil.getJavaElement(arguments, ATTRIBUTE_INPUT, project);
		fMember= (IMember)JavaRefactoringDescriptorUtil.getJavaElement(arguments, ATTRIBUTE_ELEMENT + 1, project);
		fSubtype= (IType)JavaRefactoringDescriptorUtil.getJavaElement(arguments, ATTRIBUTE_SUBTYPE + 1, project);
	}

	public void setMember(final IMember member) {
		Assert.isNotNull(member);
		fMember= member;
	}

	public void setSupertype(final IType supertype) {
		Assert.isNotNull(supertype);
		fSupertype= supertype;
	}

	public void setSubtype(final IType subtype) {
		Assert.isNotNull(subtype);
		fSubtype= subtype;
	}

	protected void populateArgumentMap() {
		super.populateArgumentMap();
		JavaRefactoringDescriptorUtil.setJavaElement(fArguments, ATTRIBUTE_INPUT, getProject(), fSupertype);
		JavaRefactoringDescriptorUtil.setJavaElement(fArguments, ATTRIBUTE_ELEMENT + 1, getProject(), fMember);
		JavaRefactoringDescriptorUtil.setJavaElement(fArguments, ATTRIBUTE_SUBTYPE + 1, getProject(), fSubtype);
	}

	public RefactoringStatus validateDescriptor() {
		RefactoringStatus status= super.validateDescriptor();
		if (fMember == null) {
			status.merge(RefactoringStatus.createFatalErrorStatus(CompositeRefactoringsMessages.CopyMemberToSubstype_no_member));
		}
		if (fSupertype == null) {
			status.merge(RefactoringStatus.createFatalErrorStatus(CompositeRefactoringsMessages.CopyMemberToSubstype_no_supertype));
		}
		if (fSubtype == null) {
			status.merge(RefactoringStatus.createFatalErrorStatus(CompositeRefactoringsMessages.CopyMemberToSubstype_no_subtype));
		}
		return status;
	}

}
