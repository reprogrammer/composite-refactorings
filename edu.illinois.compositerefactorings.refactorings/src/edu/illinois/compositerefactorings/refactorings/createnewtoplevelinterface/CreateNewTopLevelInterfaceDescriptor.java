/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.createnewtoplevelinterface;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;

@SuppressWarnings("restriction")
public final class CreateNewTopLevelInterfaceDescriptor extends JavaRefactoringDescriptor {

	/**
	 * This ID should match the of the refactoring contribution used in the corresponding extension.
	 */
	public static final String ID= "edu.illinois.compositerefactorings.createnewtoplevelinterface";

	private IType fSubType= null;

	private String fNewInterfaceName= null;

	public CreateNewTopLevelInterfaceDescriptor() {
		super(ID);
	}

	public CreateNewTopLevelInterfaceDescriptor(String project, String description, String comment, Map<String, String> arguments, int flags) {
		super(ID, project, description, comment, arguments, flags);
		fSubType= (IType)JavaRefactoringDescriptorUtil.getJavaElement(arguments, ATTRIBUTE_INPUT, project);
		fNewInterfaceName= JavaRefactoringDescriptorUtil.getString(arguments, ATTRIBUTE_NAME);
	}

	public void setNewInterfaceName(String newInterface) {
		Assert.isNotNull(newInterface);
		fNewInterfaceName= newInterface;
	}

	public void setType(final IType type) {
		Assert.isNotNull(type);
		fSubType= type;
	}

	protected void populateArgumentMap() {
		super.populateArgumentMap();
		JavaRefactoringDescriptorUtil.setJavaElement(fArguments, ATTRIBUTE_INPUT, getProject(), fSubType);
		JavaRefactoringDescriptorUtil.setString(fArguments, ATTRIBUTE_NAME, fNewInterfaceName);
	}

	public RefactoringStatus validateDescriptor() {
		RefactoringStatus status= super.validateDescriptor();
		if (fSubType == null) {
			status.merge(RefactoringStatus.createFatalErrorStatus(CompositeRefactoringsMessages.CreateNewTopLevelInterface_no_type));
		}
		if (fNewInterfaceName == null) {
			status.merge(RefactoringStatus.createFatalErrorStatus(CompositeRefactoringsMessages.CreateNewTopLevelInterface_no_interface_name));
		}
		return status;
	}

}
