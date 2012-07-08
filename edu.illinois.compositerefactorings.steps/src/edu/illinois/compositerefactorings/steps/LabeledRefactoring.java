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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.InsertEdit;

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
			RefactoringStatus status= refactoring.checkFinalConditions(new NullProgressMonitor());
			Change change= null;
			if (status.hasFatalError()) {
				change= new TextFileChange("unavailable", (IFile)compilationUnit.getResource()); //$NON-NLS-1$
				((TextFileChange)change).setEdit(new InsertEdit(0, "")); //$NON-NLS-1$
			} else {
				change= refactoring.createChange(new NullProgressMonitor());
			}
			Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
			ChangeCorrectionProposal proposal= new ChangeCorrectionProposal(label, change, relevance, image);
			proposals.add(proposal);
		}
		return proposals;
	}

}
