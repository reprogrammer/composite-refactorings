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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;
import org.eclipse.swt.graphics.Image;


@SuppressWarnings("restriction")
public class CompositeRefactoringsQuickAssistProcessor implements IQuickAssistProcessor {

	@Override
	public boolean hasAssists(IInvocationContext context) throws CoreException {
		ASTNode coveringNode= context.getCoveringNode();
		if (coveringNode != null) {
			return getCreateNewSuperclassProposal(context, coveringNode, false, null) ||
					new MoveToImmediateSuperclass(context, coveringNode, false).hasInputs() ||
					new MoveTypeToNewFile(context, coveringNode, false).hasInputs() ||
					new ReplaceTypeBySupertypeInVariableDeclarations(context, coveringNode, false).hasInputs() ||
					new ReplaceTypeBySupertypeInInstanceOf(context, coveringNode, false).hasInputs();
		}
		return false;
	}

	@Override
	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		ASTNode coveringNode= context.getCoveringNode();
		if (coveringNode != null) {
			ArrayList<ICommandAccess> resultingCollections= new ArrayList<ICommandAccess>();
			getCreateNewSuperclassProposal(context, coveringNode, false, resultingCollections);
			resultingCollections.addAll(new MoveToImmediateSuperclass(context, coveringNode, false).getProposals());
			resultingCollections.addAll(new MoveTypeToNewFile(context, coveringNode, false).getProposals());
			resultingCollections.addAll(new ReplaceTypeBySupertypeInVariableDeclarations(context, coveringNode, false).getProposals());
			resultingCollections.addAll(new ReplaceTypeBySupertypeInInstanceOf(context, coveringNode, false).getProposals());
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

}
