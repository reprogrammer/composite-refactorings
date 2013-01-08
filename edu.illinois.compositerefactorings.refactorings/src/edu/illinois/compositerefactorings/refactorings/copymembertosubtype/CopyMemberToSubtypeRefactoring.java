/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.copymembertosubtype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;
import edu.illinois.compositerefactorings.refactorings.ChangeUtils;

@SuppressWarnings("restriction")
public class CopyMemberToSubtypeRefactoring extends Refactoring {

	private RefactoringProcessor fRefactoringProcessor;

	public CopyMemberToSubtypeRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
		fRefactoringProcessor= new CopyMemberToSubtypeRefactoringProcessor(arguments, status);
	}

	@Override
	public String getName() {
		return CompositeRefactoringsMessages.CopyMemberToSubstype_name;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return fRefactoringProcessor.checkInitialConditions(pm);
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return fRefactoringProcessor.checkFinalConditions(pm, ChangeUtils.createCheckConditionsContext(getValidationContext()));
	}

	@Override
	public final Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
		DynamicValidationRefactoringChange change= (DynamicValidationRefactoringChange)fRefactoringProcessor.createChange(pm);
		if (change == null) {
			return null;
		}
		RefactoringChangeDescriptor changeDescriptor= (RefactoringChangeDescriptor)change.getDescriptor();
		JavaRefactoringDescriptor descriptor= (JavaRefactoringDescriptor)changeDescriptor.getRefactoringDescriptor();
		return new DynamicValidationRefactoringChange(descriptor, getName(), ChangeUtils.createChangesWithNullParents(change.getChildren()));
	}

}
