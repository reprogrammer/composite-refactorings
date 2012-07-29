/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.usesupertypeinvariabledeclarations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.refactoring.descriptors.UseSupertypeDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.structure.UseSuperTypeProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import edu.illinois.compositerefactorings.refactorings.ChangeUtils;

@SuppressWarnings("restriction")
public class UseSuperTypeInVariableDeclarationsRefactoring extends Refactoring {

	private UseSuperTypeProcessor fUseSuperTypeProcessor;

	public UseSuperTypeInVariableDeclarationsRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
		fUseSuperTypeProcessor= new UseSuperTypeProcessor(createArgumentsForUseSupertype(arguments), status);
	}

	private JavaRefactoringArguments createArgumentsForUseSupertype(JavaRefactoringArguments arguments) {
		Map<String, String> argumentsMap= new HashMap<String, String>();
		argumentsMap.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
		argumentsMap.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1, arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1));
		argumentsMap.put("instanceof", String.valueOf(Boolean.FALSE));
		return new JavaRefactoringArguments(arguments.getProject(), argumentsMap);
	}

	@Override
	public String getName() {
		return "Use super type in variable declarations and casts";
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return fUseSuperTypeProcessor.checkInitialConditions(pm);
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return fUseSuperTypeProcessor.checkFinalConditions(pm, ChangeUtils.createCheckConditionsContext(getValidationContext()));
	}

	@Override
	public final Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
		DynamicValidationRefactoringChange useSuperTypeChange= (DynamicValidationRefactoringChange)fUseSuperTypeProcessor.createChange(pm);
		if (useSuperTypeChange == null) {
			return null;
		}
		RefactoringChangeDescriptor useSuperTypeChangeDescriptor= (RefactoringChangeDescriptor)useSuperTypeChange.getDescriptor();
		UseSupertypeDescriptor useSuperTypeDescriptor= (UseSupertypeDescriptor)useSuperTypeChangeDescriptor.getRefactoringDescriptor();
		@SuppressWarnings("unchecked")
		Map<String, String> arguments= new UseSuperTypeInVariableDeclarationsRefactoringContribution().retrieveArgumentMap(useSuperTypeDescriptor);
		arguments.remove("instanceof");
		UseSuperTypeInVariableDeclarationsDescriptor newDescriptor= new UseSuperTypeInVariableDeclarationsDescriptor(useSuperTypeDescriptor.getProject(), useSuperTypeDescriptor.getDescription(),
				useSuperTypeDescriptor.getComment(), arguments, useSuperTypeDescriptor.getFlags());
		return new DynamicValidationRefactoringChange(newDescriptor, getName(), ChangeUtils.createChangesWithNullParents(useSuperTypeChange.getChildren()));
	}

}
