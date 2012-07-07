/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;

public abstract class UseSuperTypeWherePossible extends RefactoringBasedStep {

	public UseSuperTypeWherePossible(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation) {
		super(context, coveringNode, problemsAtLocation);
	}

	protected static List<IType> getClosestSupertypes(IJavaProject project, IType type) throws CoreException {
		final int MAX_NUMBER_OF_SUPERTYPES= 2;
		List<IType> closestsSuperTypes= new LinkedList<IType>();
		LinkedList<IType> remainingTypes= new LinkedList<IType>();
		remainingTypes.add(type);
		while (!remainingTypes.isEmpty() && closestsSuperTypes.size() < MAX_NUMBER_OF_SUPERTYPES) {
			IType currentType= remainingTypes.removeFirst();
			if (currentType != type) {
				closestsSuperTypes.add(currentType);
			}
			ITypeHierarchy currentTypeHierarchy= currentType.newTypeHierarchy(project, new NullProgressMonitor());
			IType currentSupertype= currentTypeHierarchy.getSuperclass(currentType);
			if (currentSupertype != null) {
				remainingTypes.addLast(currentSupertype);
			}
			remainingTypes.addAll(Arrays.asList(currentTypeHierarchy.getSuperInterfaces(currentType)));
		}
		return closestsSuperTypes;
	}

	@Override
	protected Collection<IType> getInputs() {
		Collection<IType> inputs= new ArrayList<IType>();

		TypeDeclaration typeDeclaration= null;
		if (context.getCoveredNode() instanceof TypeDeclaration) {
			typeDeclaration= (TypeDeclaration)context.getCoveredNode();
		} else if (coveringNode instanceof TypeDeclaration) {
			typeDeclaration= (TypeDeclaration)coveringNode;
		} else if (!(coveringNode instanceof BodyDeclaration) && coveringNode.getParent() != null && coveringNode.getParent() instanceof TypeDeclaration) {
			typeDeclaration= (TypeDeclaration)coveringNode.getParent();
		}

		if (typeDeclaration != null) {
			ITypeBinding typeBinding= typeDeclaration.resolveBinding();
			IType type= (IType)typeBinding.getJavaElement();
			inputs.add(type);
		}
		return inputs;
	}

}
