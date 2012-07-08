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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.internal.corext.refactoring.code.IntroduceParameterRefactoring;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;

@SuppressWarnings("restriction")
public class AddMethodParameterForExpression extends RefactoringBasedStep {

	public AddMethodParameterForExpression(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation) {
		super(context, coveringNode, problemsAtLocation);
	}

	@Override
	protected Collection<? extends Expression> getInputs() {
		Collection<Expression> inputs= new ArrayList<Expression>();
		if (coveringNode instanceof Expression) {
			inputs.add((Expression)coveringNode);
		}
		return inputs;
	}

	private IMethod getEnclosingMethod() throws CoreException {
		IJavaElement enclosingElement= SelectionConverter.resolveEnclosingElement(getCompilationUnit(), new TextSelection(context.getSelectionOffset(), context.getSelectionLength()));
		if (enclosingElement instanceof IMethod) {
			return ((IMethod)enclosingElement);
		}
		else {
			return null;
		}
	}

	@Override
	protected Collection<RefactoringDescriptor> getDescriptors(Object input) throws CoreException {
		throw new UnsupportedOperationException();
	}

	protected Collection<LabeledRefactoring> getLabeledRefactorings(Object input) throws CoreException {
		Collection<LabeledRefactoring> labeledRefactorings= new ArrayList<LabeledRefactoring>();
		IMethod enclosingMethod= getEnclosingMethod();
		if (enclosingMethod != null) {
			IntroduceParameterRefactoring refactoring= new IntroduceParameterRefactoring(getCompilationUnit(), context.getSelectionOffset(), context.getSelectionLength());
			String label= MessageFormat.format(CompositeRefactoringsMessages.AddMethodParameterForExpression_description, enclosingMethod.getElementName());
			LabeledRefactoring labeledRefactoring= new LabeledRefactoring(label, refactoring, getCompilationUnit(), problemsAtLocation);
			labeledRefactorings.add(labeledRefactoring);
		}
		return labeledRefactorings;
	}

}
