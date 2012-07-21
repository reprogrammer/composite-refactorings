/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.steps;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.InsertEdit;

public class RefactoringBasedProposal extends ChangeCorrectionProposal {

	private final Refactoring fRefactoring;

	private RefactoringStatus fRefactoringStatus;

	private final ICompilationUnit fCompilationUnit;

	public RefactoringBasedProposal(String name, ICompilationUnit compilationUnit, Refactoring refactoring, int relevance, Image image) {
		super(name, null, relevance, image);
		fRefactoring= refactoring;
		fCompilationUnit= compilationUnit;
	}

	@Override
	protected Change createChange() throws CoreException {
		fRefactoringStatus= fRefactoring.checkFinalConditions(new NullProgressMonitor());
		if (fRefactoringStatus.hasFatalError()) {
			TextFileChange dummyChange= new TextFileChange("unavailable", (IFile)fCompilationUnit.getResource()); //$NON-NLS-1$
			dummyChange.setEdit(new InsertEdit(0, "")); //$NON-NLS-1$
			return dummyChange;
		}
		return fRefactoring.createChange(new NullProgressMonitor());
	}

}
