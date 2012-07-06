/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.steps;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;

public abstract class CompositeRefactoringStep {

	protected IInvocationContext context;

	protected ASTNode coveringNode;

	protected boolean problemsAtLocation;

	public CompositeRefactoringStep(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation) {
		this.context= context;
		this.coveringNode= coveringNode;
		this.problemsAtLocation= problemsAtLocation;
	}

	protected ICompilationUnit getCompilationUnit() {
		return context.getCompilationUnit();
	}

	protected IJavaProject getJavaProject() {
		return getCompilationUnit().getJavaProject();
	}

	protected abstract Collection<? extends IJavaElement> getInputs();

	public boolean hasInputs() {
		return !getInputs().isEmpty();
	}

	public abstract Collection<ICommandAccess> getProposals() throws CoreException;

}
