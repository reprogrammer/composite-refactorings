/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.usesupertypeinvariabledeclarations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.refactoring.descriptors.UseSupertypeDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.structure.UseSuperTypeProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;

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

	// See org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring.createCheckConditionsContext()
	private CheckConditionsContext createCheckConditionsContext() throws CoreException {
		CheckConditionsContext result= new CheckConditionsContext();
		result.add(new ValidateEditChecker(getValidationContext()));
		result.add(new ResourceChangeChecker());
		return result;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return fUseSuperTypeProcessor.checkFinalConditions(pm, createCheckConditionsContext());
	}

	private Change[] createChangesWithNullParents(Change[] changes) throws CoreException {
		for (Change change : changes) {
			try {
				setParentToNull(change);
			} catch (Exception e) {
				throw new CoreException(new Status(Status.ERROR, null, "Failed to make a reflective call.", e));
			}
		}
		return changes;
	}

	// See http://stackoverflow.com/a/880400/130224
	private void setParentToNull(Change change) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method= Change.class.getDeclaredMethod("setParent", Change.class);
		method.setAccessible(true);
		method.invoke(change, new Object[] { null });
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
		return new DynamicValidationRefactoringChange(newDescriptor, getName(), createChangesWithNullParents(useSuperTypeChange.getChildren()));
	}

}
