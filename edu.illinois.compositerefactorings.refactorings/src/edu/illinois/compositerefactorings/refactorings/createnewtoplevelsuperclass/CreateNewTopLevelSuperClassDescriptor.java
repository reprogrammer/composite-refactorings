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

	/**
	 * This ID should match the of the refactoring contribution used in the corresponding extension.
	 */
	public static final String ID= "edu.illinois.compositerefactorings.createnewtoplevelsuperclass";

	/** The subtype attribute */
	private IType fType= null;

	private String fClassName= null;

	public CreateNewTopLevelSuperClassDescriptor() {
		super(ID);
	}

	public CreateNewTopLevelSuperClassDescriptor(String project, String description, String comment, Map<String, String> arguments, int flags) {
		super(ID, project, description, comment, arguments, flags);
		fType= (IType)JavaRefactoringDescriptorUtil.getJavaElement(arguments, ATTRIBUTE_INPUT, project);
		fClassName= JavaRefactoringDescriptorUtil.getString(arguments, ATTRIBUTE_NAME);
	}

	public void setClassName(String className) {
		Assert.isNotNull(className);
		fClassName= className;
	}

	public void setType(final IType type) {
		Assert.isNotNull(type);
		fType= type;
	}

	protected void populateArgumentMap() {
		super.populateArgumentMap();
		JavaRefactoringDescriptorUtil.setJavaElement(fArguments, ATTRIBUTE_INPUT, getProject(), fType);
		JavaRefactoringDescriptorUtil.setString(fArguments, ATTRIBUTE_NAME, fClassName);
	}

	public RefactoringStatus validateDescriptor() {
		RefactoringStatus status= super.validateDescriptor();
		if (fType == null) {
			status.merge(RefactoringStatus.createFatalErrorStatus(CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_no_type));
		}
		if (fClassName == null) {
			status.merge(RefactoringStatus.createFatalErrorStatus(CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_no_super_class_name));
		}
		return status;
	}

}
