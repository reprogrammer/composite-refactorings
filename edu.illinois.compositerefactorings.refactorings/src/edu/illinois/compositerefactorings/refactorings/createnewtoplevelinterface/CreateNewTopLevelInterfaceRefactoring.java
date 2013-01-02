/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.createnewtoplevelinterface;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractInterfaceDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.structure.ExtractInterfaceProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;
import edu.illinois.compositerefactorings.refactorings.ChangeUtils;

@SuppressWarnings("restriction")
public class CreateNewTopLevelInterfaceRefactoring extends Refactoring {

	private static final String ATTRIBUTE_ABSTRACT= "abstract";

	private static final String ATTRIBUTE_COMMENTS= "comments";

	private static final String ATTRIBUTE_PUBLIC= "public";

	private static final String ATTRIBUTE_REPLACE= "replace";

	private static final String ATTRIBUTE_INSTANCEOF= "instanceof";

	private ExtractInterfaceProcessor fRefactoringProcessor;

	public CreateNewTopLevelInterfaceRefactoring(JavaRefactoringArguments arguments, RefactoringStatus status) {
		fRefactoringProcessor= new ExtractInterfaceProcessor(createArguments(arguments), status);
	}

	private JavaRefactoringArguments createArguments(JavaRefactoringArguments arguments) {
		Map<String, String> argumentsMap= new HashMap<String, String>();
		argumentsMap.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME));
		argumentsMap.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT));
		argumentsMap.put(ATTRIBUTE_ABSTRACT, String.valueOf(Boolean.FALSE));
		argumentsMap.put(ATTRIBUTE_COMMENTS, String.valueOf(Boolean.FALSE));
		argumentsMap.put(ATTRIBUTE_PUBLIC, String.valueOf(Boolean.FALSE));
		argumentsMap.put(ATTRIBUTE_REPLACE, String.valueOf(Boolean.FALSE));
		argumentsMap.put(ATTRIBUTE_INSTANCEOF, String.valueOf(Boolean.FALSE));
		return new JavaRefactoringArguments(arguments.getProject(), argumentsMap);
	}

	@Override
	public String getName() {
		return CompositeRefactoringsMessages.CreateNewTopLevelInterface_name;
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
		ExtractInterfaceDescriptor descriptor= (ExtractInterfaceDescriptor)changeDescriptor.getRefactoringDescriptor();
		@SuppressWarnings("unchecked")
		Map<String, String> arguments= new CreateNewTopLevelInterfaceRefactoringContribution().retrieveArgumentMap(descriptor);
		arguments.remove(ATTRIBUTE_ABSTRACT);
		arguments.remove(ATTRIBUTE_COMMENTS);
		arguments.remove(ATTRIBUTE_PUBLIC);
		arguments.remove(ATTRIBUTE_REPLACE);
		arguments.remove(ATTRIBUTE_INSTANCEOF);
		CreateNewTopLevelInterfaceDescriptor newDescriptor= new CreateNewTopLevelInterfaceDescriptor(descriptor.getProject(), descriptor.getDescription(),
				descriptor.getComment(), arguments, descriptor.getFlags());
		return new DynamicValidationRefactoringChange(newDescriptor, getName(), ChangeUtils.createChangesWithNullParents(change.getChildren()));
	}

}
