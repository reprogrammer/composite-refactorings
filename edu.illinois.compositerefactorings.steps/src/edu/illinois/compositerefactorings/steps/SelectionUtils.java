/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.steps;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.text.java.IInvocationContext;

public class SelectionUtils {

	public static Collection<IMember> getSelectionMemberDeclarations(IInvocationContext context, ASTNode coveringNode) {
		Collection<IMember> members= new ArrayList<IMember>();
		IMember member= null;

		IMember fieldMember= null;

		FieldDeclaration fieldDeclaration= null;

		if (context.getCoveredNode() instanceof FieldDeclaration) {
			fieldDeclaration= (FieldDeclaration)context.getCoveredNode();
		} else if (coveringNode instanceof FieldDeclaration) {
			fieldDeclaration= (FieldDeclaration)coveringNode;
		} else if (coveringNode.getParent() instanceof FieldDeclaration) {
			fieldDeclaration= (FieldDeclaration)coveringNode.getParent();
		} else if (coveringNode.getParent() != null && coveringNode.getParent().getParent() instanceof FieldDeclaration) {
			fieldDeclaration= (FieldDeclaration)coveringNode.getParent().getParent();
		}

		if (fieldDeclaration != null) {
			// See http://stackoverflow.com/a/11210998/130224
			VariableDeclarationFragment variableDeclarationFragment= (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
			fieldMember= (IMember)variableDeclarationFragment.resolveBinding().getJavaElement();
		}

		IMember methodMember= null;

		MethodDeclaration methodDeclaration= null;

		if (context.getCoveredNode() instanceof MethodDeclaration) {
			methodDeclaration= (MethodDeclaration)context.getCoveredNode();
		} else if (coveringNode instanceof MethodDeclaration) {
			methodDeclaration= (MethodDeclaration)coveringNode;
		} else if (coveringNode.getParent() instanceof MethodDeclaration) {
			methodDeclaration= (MethodDeclaration)coveringNode.getParent();
		}

		if (methodDeclaration != null) {
			methodMember= (IMember)methodDeclaration.resolveBinding().getJavaElement();
		}

		if (fieldMember != null) {
			member= fieldMember;
		}

		if (methodMember != null) {
			member= methodMember;
		}

		if (member != null && member.getDeclaringType() != null) {
			members.add(member);
		}

		return members;
	}

}
