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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.refactoring.descriptors.UseSupertypeDescriptor;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;

public class ReplaceTypeBySupertypeInVariableDeclarations extends UseSuperTypeWherePossible {

	public ReplaceTypeBySupertypeInVariableDeclarations(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation) {
		super(context, coveringNode, problemsAtLocation);
	}

	@Override
	protected Collection<RefactoringDescriptor> getDescriptors(Object input) throws CoreException {
		IType type= (IType)input;
		Collection<RefactoringDescriptor> descriptors= new ArrayList<RefactoringDescriptor>();
		List<IType> supertypes= getClosestSupertypes(getJavaProject(), type);
		for (IType supertype : supertypes) {
			UseSupertypeDescriptor descriptor= new UseSupertypeDescriptor();
			String description= MessageFormat.format(CompositeRefactoringsMessages.ReplaceTypeBySupertypeInVariableDeclarations_description, type.getElementName(), supertype.getElementName());
			descriptor.setDescription(description);
			descriptor.setProject(getJavaProject().getElementName());
			descriptor.setReplaceInstanceof(false);
			descriptor.setSubtype(type);
			descriptor.setSupertype(supertype);
			descriptors.add(descriptor);
		}
		return descriptors;
	}

}
