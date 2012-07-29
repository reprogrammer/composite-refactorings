/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.createnewtoplevelsuperclass;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractSuperclassDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.structure.ExtractSupertypeProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;
import edu.illinois.compositerefactorings.refactorings.ChangeUtils;

@SuppressWarnings("restriction")
public class CreateNewTopLevelSuperClassRefactoring extends Refactoring {

	private ExtractSupertypeProcessor fExtractSuperTypeProcessor;

	public CreateNewTopLevelSuperClassRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
		fExtractSuperTypeProcessor= new ExtractSupertypeProcessor(createArgumentsForExtractSupertype(arguments), status);
	}

	private JavaRefactoringArguments createArgumentsForExtractSupertype(JavaRefactoringArguments arguments) {
		Map<String, String> argumentsMap= new HashMap<String, String>();
		argumentsMap.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME));
		argumentsMap.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
		argumentsMap.put("stubs", String.valueOf(Boolean.FALSE));
		argumentsMap.put("instanceof", String.valueOf(Boolean.FALSE));
		argumentsMap.put("replace", String.valueOf(Boolean.FALSE));
		argumentsMap.put("abstract", Integer.toString(0));
		argumentsMap.put("delete", Integer.toString(0));
		argumentsMap.put("extract", Integer.toString(0));
		argumentsMap.put("types", Integer.toString(0));
		return new JavaRefactoringArguments(arguments.getProject(), argumentsMap);
	}

	@Override
	public String getName() {
		return CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_name;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return fExtractSuperTypeProcessor.checkInitialConditions(pm);
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return fExtractSuperTypeProcessor.checkFinalConditions(pm, ChangeUtils.createCheckConditionsContext(getValidationContext()));
	}

	@Override
	public final Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
		DynamicValidationRefactoringChange extractSuperTypeChange= (DynamicValidationRefactoringChange)fExtractSuperTypeProcessor.createChange(pm);
		if (extractSuperTypeChange == null) {
			return null;
		}
		RefactoringChangeDescriptor extractSuperTypeChangeDescriptor= (RefactoringChangeDescriptor)extractSuperTypeChange.getDescriptor();
		ExtractSuperclassDescriptor extractSuperTypeDescriptor= (ExtractSuperclassDescriptor)extractSuperTypeChangeDescriptor.getRefactoringDescriptor();
		@SuppressWarnings("unchecked")
		Map<String, String> arguments= new CreateNewTopLevelSuperClassRefactoringContribution().retrieveArgumentMap(extractSuperTypeDescriptor);
		arguments.remove("stubs");
		arguments.remove("instanceof");
		arguments.remove("replace");
		arguments.remove("abstract");
		arguments.remove("delete");
		arguments.remove("extract");
		arguments.remove("types");
		CreateNewTopLevelSuperClassDescriptor newDescriptor= new CreateNewTopLevelSuperClassDescriptor(extractSuperTypeDescriptor.getProject(), extractSuperTypeDescriptor.getDescription(),
				extractSuperTypeDescriptor.getComment(), arguments, extractSuperTypeDescriptor.getFlags());
		return new DynamicValidationRefactoringChange(newDescriptor, getName(), ChangeUtils.createChangesWithNullParents(extractSuperTypeChange.getChildren()));
	}

}
