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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class LabeledRefactoring {

	private String label;

	private Refactoring refactoring;

	private ICompilationUnit compilationUnit;

	private boolean problemsAtLocation;

	public LabeledRefactoring(String label, Refactoring refactoring, ICompilationUnit compilationUnit, boolean problemsAtLocation) {
		this.label= label;
		this.refactoring= refactoring;
		this.compilationUnit= compilationUnit;
		this.problemsAtLocation= problemsAtLocation;
	}

	protected Collection<? extends ICommandAccess> getRefactoringProposals() throws CoreException {
		Collection<ICommandAccess> proposals= new ArrayList<ICommandAccess>();
		if (refactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
			int relevance= problemsAtLocation ? 1 : 4;
			Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
			RefactoringBasedProposal proposal= new RefactoringBasedProposal(label, compilationUnit, refactoring, relevance, image);
			proposals.add(proposal);
		}
		return proposals;
	}

}
