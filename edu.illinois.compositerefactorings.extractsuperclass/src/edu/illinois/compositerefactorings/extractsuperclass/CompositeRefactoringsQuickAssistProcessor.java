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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInnerToTopRefactoring;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
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
		String label= "Create new superclass in file";
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

	private static boolean getMoveToImmediateSuperclassProposal(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation, Collection<ICommandAccess> proposals)
			throws CoreException {
		if (proposals == null) {
			return true;
		}

		BodyDeclaration bodyDeclaration= getSelectedBodyDeclaration(context, coveringNode);

		if (bodyDeclaration == null) {
			return false;
		}

		TypeDeclaration typeDeclaration= null;

		if (bodyDeclaration.getParent() instanceof TypeDeclaration) {
			typeDeclaration= (TypeDeclaration)bodyDeclaration.getParent();
		} else {
			return false;
		}

		final ICompilationUnit cu= context.getCompilationUnit();
		CompilationUnit cuASTNode= (CompilationUnit)bodyDeclaration.getRoot();
		ASTRewrite rewrite= ASTRewrite.create(typeDeclaration.getAST());
		String label= "Move to immediate superclass";
		Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
		ASTRewriteCorrectionProposal proposal= new ASTRewriteCorrectionProposal(label, cu, rewrite, 0, image);
		ASTNode placeHolderForMethodDeclaration= rewrite.createMoveTarget(bodyDeclaration);
		SimpleType superTypeASTNode= (SimpleType)typeDeclaration.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY);
		if (superTypeASTNode == null) {
			return false;
		}
		ITypeBinding superTypeBinding= superTypeASTNode.resolveBinding();
		// See http://wiki.eclipse.org/JDT/FAQ#From_an_IBinding_to_its_declaring_ASTNode
		ASTNode superTypeDeclarationASTNode= cuASTNode.findDeclaringNode(superTypeBinding);
		if (superTypeDeclarationASTNode == null) {
			//FIXME: We should be able to find the super type in some other compilation unit.
			return false;
		}
		ListRewrite superTypeMembersListRewrite= rewrite.getListRewrite(superTypeDeclarationASTNode, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		superTypeMembersListRewrite.insertFirst(placeHolderForMethodDeclaration, null);
		rewrite.remove(bodyDeclaration, null);
		proposals.add(proposal);
		return true;
	}

	private static BodyDeclaration getSelectedBodyDeclaration(IInvocationContext context, ASTNode coveringNode) {
		BodyDeclaration bodyDeclaration= null;

		if (context.getCoveredNode() instanceof BodyDeclaration) {
			bodyDeclaration= (BodyDeclaration)context.getCoveredNode();
		} else if (coveringNode instanceof BodyDeclaration) {
			bodyDeclaration= (BodyDeclaration)coveringNode;
		} else if (coveringNode.getParent() != null) {
			if (coveringNode.getParent() instanceof BodyDeclaration) {
				bodyDeclaration= (BodyDeclaration)coveringNode.getParent();
			} else if (coveringNode.getParent().getParent() != null && coveringNode.getParent().getParent() instanceof BodyDeclaration) {
				bodyDeclaration= (BodyDeclaration)coveringNode.getParent().getParent();
			}
		} else {
			bodyDeclaration= null;
		}
		return bodyDeclaration;
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
			String label= String.format("Move type %s to a new file", type.getElementName());

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
