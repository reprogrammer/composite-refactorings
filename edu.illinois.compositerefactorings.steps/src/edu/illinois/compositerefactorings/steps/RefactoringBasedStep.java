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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public abstract class RefactoringBasedStep extends CompositeRefactoringStep {

	public RefactoringBasedStep(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation) {
		super(context, coveringNode, problemsAtLocation);
	}

	protected abstract Collection<RefactoringDescriptor> getDescriptors(Object input) throws CoreException;

	protected Collection<LabeledRefactoring> getLabeledRefactorings(Object input) throws CoreException {
		Collection<RefactoringDescriptor> descriptors= getDescriptors(input);
		Collection<LabeledRefactoring> labeledRefactorings= new ArrayList<LabeledRefactoring>();
		for (RefactoringDescriptor descriptor : descriptors) {
			Refactoring refactoring= descriptor.createRefactoringContext(new RefactoringStatus()).getRefactoring();
			LabeledRefactoring labeledRefactoring= new LabeledRefactoring(descriptor.getDescription(), refactoring, getCompilationUnit(), problemsAtLocation);
			labeledRefactorings.add(labeledRefactoring);
		}
		return labeledRefactorings;
	}

	@Override
	protected Collection<? extends ICommandAccess> getProposals(Object input) throws CoreException {
		Collection<ICommandAccess> proposals= new ArrayList<ICommandAccess>();
		for (LabeledRefactoring labeledRefactoring : getLabeledRefactorings(input)) {
			proposals.addAll(labeledRefactoring.getRefactoringProposals());
		}
		return proposals;
	}

}
