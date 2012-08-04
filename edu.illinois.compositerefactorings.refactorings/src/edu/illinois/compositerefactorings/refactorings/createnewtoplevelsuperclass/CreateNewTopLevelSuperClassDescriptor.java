/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.createnewtoplevelsuperclass;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;

@SuppressWarnings("restriction")
public final class CreateNewTopLevelSuperClassDescriptor extends JavaRefactoringDescriptor {

	public static final String ATTRIBUTE_TYPES= "types";

	/**
	 * This ID should match the of the refactoring contribution used in the corresponding extension.
	 */
	public static final String ID= "edu.illinois.compositerefactorings.createnewtoplevelsuperclass";

	private IType fType= null;

	private IType[] fSubTypes= null;

	private String fNewClassName= null;

	public CreateNewTopLevelSuperClassDescriptor() {
		super(ID);
	}

	public CreateNewTopLevelSuperClassDescriptor(String project, String description, String comment, Map<String, String> arguments, int flags) {
		super(ID, project, description, comment, arguments, flags);
		fType= (IType)JavaRefactoringDescriptorUtil.getJavaElement(arguments, ATTRIBUTE_INPUT, project);
		fSubTypes= (IType[])JavaRefactoringDescriptorUtil.getJavaElementArray(arguments, ATTRIBUTE_TYPES, ATTRIBUTE_ELEMENT, 1, project, IType.class);
		fNewClassName= JavaRefactoringDescriptorUtil.getString(arguments, ATTRIBUTE_NAME);
	}

	public void setNewClassName(String newClassName) {
		Assert.isNotNull(newClassName);
		fNewClassName= newClassName;
	}

	public void setType(final IType type) {
		Assert.isNotNull(type);
		fType= type;
	}

	public void setSubTypes(final IType[] subTypes) {
		Assert.isNotNull(subTypes);
		Assert.isTrue(subTypes.length > 0);
		fSubTypes= subTypes;
	}

	protected void populateArgumentMap() {
		super.populateArgumentMap();
		JavaRefactoringDescriptorUtil.setJavaElement(fArguments, ATTRIBUTE_INPUT, getProject(), fType);
		JavaRefactoringDescriptorUtil.setJavaElementArray(fArguments, ATTRIBUTE_TYPES, ATTRIBUTE_ELEMENT, getProject(), fSubTypes, 1);
		JavaRefactoringDescriptorUtil.setString(fArguments, ATTRIBUTE_NAME, fNewClassName);
	}

	public RefactoringStatus validateDescriptor() {
		RefactoringStatus status= super.validateDescriptor();
		if (fType == null) {
			status.merge(RefactoringStatus.createFatalErrorStatus(CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_no_type));
		}
		if (fSubTypes == null || fSubTypes.length == 0) {
			status.merge(RefactoringStatus.createFatalErrorStatus(CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_no_type));
		}
		if (fNewClassName == null) {
			status.merge(RefactoringStatus.createFatalErrorStatus(CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_no_super_class_name));
		}
		return status;
	}

}
