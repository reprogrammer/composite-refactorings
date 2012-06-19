/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.extractsuperclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInnerToTopRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.structure.PullUpRefactoringProcessor;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.InsertEdit;


@SuppressWarnings("restriction")
public class CompositeRefactoringsQuickAssistProcessor implements IQuickAssistProcessor {

	@Override
	public boolean hasAssists(IInvocationContext context) throws CoreException {
		ASTNode coveringNode= context.getCoveringNode();
		if (coveringNode != null) {
			return getCreateNewSuperclassProposal(context, coveringNode, false, null) ||
					getMoveToImmediateSuperclassProposal(context, coveringNode, false, null) ||
					getMoveTypeToNewFileProposal(context, coveringNode, false, null);
		}
		return false;
	}

	@Override
	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		ASTNode coveringNode= context.getCoveringNode();
		if (coveringNode != null) {
			ArrayList<ICommandAccess> resultingCollections= new ArrayList<ICommandAccess>();
			getCreateNewSuperclassProposal(context, coveringNode, false, resultingCollections);
			getMoveToImmediateSuperclassProposal(context, coveringNode, false, resultingCollections);
			getMoveTypeToNewFileProposal(context, coveringNode, false, resultingCollections);
			return resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
		}
		return null;
	}

	private static boolean getCreateNewSuperclassProposal(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation, Collection<ICommandAccess> proposals) throws CoreException {
		if (proposals == null) {
			return true;
		}

		TypeDeclaration typeDeclaration= null;

		if (context.getCoveredNode() instanceof TypeDeclaration) {
			typeDeclaration= (TypeDeclaration)context.getCoveredNode();
		} else if (coveringNode instanceof TypeDeclaration) {
			typeDeclaration= (TypeDeclaration)coveringNode;
		} else if (!(coveringNode instanceof BodyDeclaration) && coveringNode.getParent() != null && coveringNode.getParent() instanceof TypeDeclaration) {
			typeDeclaration= (TypeDeclaration)coveringNode.getParent();
		} else {
			return false;
		}

		final ICompilationUnit cu= context.getCompilationUnit();
		ASTNode node= typeDeclaration.getParent();
		AST ast= node.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);
		String label= String.format("Create a new super type for '%s' in '%s'", typeDeclaration.getName(), cu.getElementName());
		Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
		ASTRewriteCorrectionProposal proposal= new ASTRewriteCorrectionProposal(label, cu, rewrite, 0, image);
		TypeDeclaration newSuperclass= ast.newTypeDeclaration();
		newSuperclass.setName(ast.newSimpleName("NewSuperclass"));
		ListRewrite listRewrite= rewrite.getListRewrite(node, CompilationUnit.TYPES_PROPERTY);
		listRewrite.insertLast(newSuperclass, null);
		rewrite.set(newSuperclass, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, typeDeclaration.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY), null);
		rewrite.set(typeDeclaration, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, newSuperclass.getName(), null);
		proposals.add(proposal);
		return true;
	}

	// See org.eclipse.jdt.ui.actions.PullUpAction.getSelectedMemberFromEditor()
	private static IMember getSelectedMemberFromEditor(JavaEditor editor) throws JavaModelException {
		IJavaElement element= SelectionConverter.resolveEnclosingElement(editor, (ITextSelection)editor.getSelectionProvider().getSelection());
		if (element == null || !(element instanceof IMember))
			return null;
		return (IMember)element;
	}

	private static boolean getMoveToImmediateSuperclassProposal(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation, Collection<ICommandAccess> proposals)
			throws CoreException {
		if (proposals == null) {
			return true;
		}

		IMember member= getSelectedMemberFromEditor((JavaEditor)((AssistContext)context).getEditor());

		if (member == null) {
			return false;
		}

		final ICompilationUnit cu= context.getCompilationUnit();
		IJavaProject project= cu.getJavaProject();

		IType declaringType= member.getDeclaringType();
		if (declaringType == null) {
			return false;
		}
		IType immediateSuperclass= declaringType.newTypeHierarchy(project, new NullProgressMonitor()).getSuperclass(declaringType);

		String projectName= project.getElementName();
		Map<String, String> refactoringArgumentsMap= new HashMap<String, String>();
		refactoringArgumentsMap.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, JavaRefactoringDescriptorUtil.elementToHandle(projectName, immediateSuperclass));
		refactoringArgumentsMap.put("stubs", String.valueOf(false));
		refactoringArgumentsMap.put("instanceof", String.valueOf(false));
		refactoringArgumentsMap.put("replace", String.valueOf(false));
		refactoringArgumentsMap.put("abstract", String.valueOf(0));
		int numberOfMethodsToRemoveFromSubclasses= 0;
		if (member instanceof IField) {
			numberOfMethodsToRemoveFromSubclasses= 0;
		} else if (member instanceof IMethod) {
			numberOfMethodsToRemoveFromSubclasses= 1;
		}
		refactoringArgumentsMap.put("delete", Integer.toString(numberOfMethodsToRemoveFromSubclasses));
		refactoringArgumentsMap.put("pull", String.valueOf(1));
		refactoringArgumentsMap.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1, JavaRefactoringDescriptorUtil.elementToHandle(projectName, member));
		if (numberOfMethodsToRemoveFromSubclasses > 0) {
			refactoringArgumentsMap.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 2, JavaRefactoringDescriptorUtil.elementToHandle(projectName, member));
		}

		JavaRefactoringArguments refactoringArguments= new JavaRefactoringArguments(projectName, refactoringArgumentsMap);
		PullUpRefactoringProcessor processor= new PullUpRefactoringProcessor(refactoringArguments, new RefactoringStatus());
		Refactoring refactoring= new ProcessorBasedRefactoring(processor);

		if (refactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
			String label= String.format("Move '%s' to super type '%s'", member.getElementName(), immediateSuperclass.getElementName());

			Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
			int relevance= problemsAtLocation ? 1 : 4;
			RefactoringStatus status= refactoring.checkFinalConditions(new NullProgressMonitor());
			Change change= null;
			if (status.hasFatalError()) {
				change= new TextFileChange("fatal error", (IFile)cu.getResource()); //$NON-NLS-1$
				((TextFileChange)change).setEdit(new InsertEdit(0, "")); //$NON-NLS-1$
				return false;
			} else {
				change= refactoring.createChange(new NullProgressMonitor());
			}
			ChangeCorrectionProposal proposal= new ChangeCorrectionProposal(label, change, relevance, image);

			proposals.add(proposal);
		}

		return true;
	}

	private static boolean getMoveTypeToNewFileProposal(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation, Collection<ICommandAccess> proposals) throws CoreException {
		if (proposals == null) {
			return true;
		}

		TypeDeclaration typeDeclaration= null;

		if (context.getCoveredNode() instanceof TypeDeclaration) {
			typeDeclaration= (TypeDeclaration)context.getCoveredNode();
		} else if (coveringNode instanceof TypeDeclaration) {
			typeDeclaration= (TypeDeclaration)coveringNode;
		} else if (!(coveringNode instanceof BodyDeclaration) && coveringNode.getParent() != null && coveringNode.getParent() instanceof TypeDeclaration) {
			typeDeclaration= (TypeDeclaration)coveringNode.getParent();
		} else {
			return false;
		}

		final ICompilationUnit cu= context.getCompilationUnit();
		ITypeBinding typeBinding= typeDeclaration.resolveBinding();
		IType type= (IType)typeBinding.getJavaElement();
		final MoveInnerToTopRefactoring moveInnerToTopRefactoring= new MoveInnerToTopRefactoring(type, null);

		if (moveInnerToTopRefactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
			String label= String.format("Move type '%s' to a new file", type.getElementName());

			Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
			int relevance= problemsAtLocation ? 1 : 4;
			RefactoringStatus status= moveInnerToTopRefactoring.checkFinalConditions(new NullProgressMonitor());
			Change change= null;
			if (status.hasFatalError()) {
				change= new TextFileChange("fatal error", (IFile)cu.getResource()); //$NON-NLS-1$
				((TextFileChange)change).setEdit(new InsertEdit(0, "")); //$NON-NLS-1$
				return false;
			} else {
				change= moveInnerToTopRefactoring.createChange(new NullProgressMonitor());
			}
			ChangeCorrectionProposal proposal= new ChangeCorrectionProposal(label, change, relevance, image);

			proposals.add(proposal);
		}
		return true;
	}

}
