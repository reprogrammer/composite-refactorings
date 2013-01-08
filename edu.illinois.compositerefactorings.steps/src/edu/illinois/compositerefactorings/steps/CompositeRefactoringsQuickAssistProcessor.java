/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.steps;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.correction.ICommandAccess;


public class CompositeRefactoringsQuickAssistProcessor implements IQuickAssistProcessor {

	private static boolean isCreateNewSuperclassEnabled= false;

	private static boolean isMoveToImmediateSuperclassEnabled= true;

	private static boolean isMoveTypeToNewFileEnabled= true;

	private static boolean isReplaceTypeBySupertypeInVariableDeclarationsEnabled= false;

	private static boolean isReplaceTypeBySupertypeInInstanceOfEnabled= false;

	private static boolean isAddMethodParameterForExpressionEnabled= true;

	private static boolean isCreateNewTopLevelSuperClassEnabled= true;

	private static boolean isCreateNewTopLevelInterfaceEnabled= true;

	private static boolean isCopyMemberToSubtypeEnabled= true;

	@Override
	public boolean hasAssists(IInvocationContext context) throws CoreException {
		ASTNode coveringNode= context.getCoveringNode();
		if (coveringNode != null) {
			return (isCreateNewSuperclassEnabled && new CreateNewSuperclass(context, coveringNode, false).hasInputs()) ||
					(isMoveToImmediateSuperclassEnabled && new MoveToImmediateSuperclass(context, coveringNode, false).hasInputs())
					|| (isMoveTypeToNewFileEnabled && new MoveTypeToNewFile(context, coveringNode, false).hasInputs()) ||
					(isReplaceTypeBySupertypeInVariableDeclarationsEnabled && new ReplaceTypeBySupertypeInVariableDeclarations(context, coveringNode, false).hasInputs()) ||
					(isReplaceTypeBySupertypeInInstanceOfEnabled && new ReplaceTypeBySupertypeInInstanceOf(context, coveringNode, false).hasInputs()) ||
					(isAddMethodParameterForExpressionEnabled && new AddMethodParameterForExpression(context, coveringNode, false).hasInputs()) ||
					(isCreateNewTopLevelSuperClassEnabled && new CreateNewTopLevelSuperClass(context, coveringNode, false).hasInputs())
					|| (isCreateNewTopLevelInterfaceEnabled && new CreateNewTopLevelInterface(context, coveringNode, false).hasInputs())
					|| (isCopyMemberToSubtypeEnabled && new CopyMemberToSubtype(context, coveringNode, false).hasInputs());
		}
		return false;
	}

	@Override
	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		ASTNode coveringNode= context.getCoveringNode();
		if (coveringNode != null) {
			ArrayList<ICommandAccess> proposals= new ArrayList<ICommandAccess>();
			if (isCreateNewSuperclassEnabled) {
				proposals.addAll(new CreateNewSuperclass(context, coveringNode, false).getProposals());
			}
			if (isMoveToImmediateSuperclassEnabled) {
				proposals.addAll(new MoveToImmediateSuperclass(context, coveringNode, false).getProposals());
			}
			if (isMoveTypeToNewFileEnabled) {
				proposals.addAll(new MoveTypeToNewFile(context, coveringNode, false).getProposals());
			}
			if (isReplaceTypeBySupertypeInVariableDeclarationsEnabled) {
				proposals.addAll(new ReplaceTypeBySupertypeInVariableDeclarations(context, coveringNode, false).getProposals());
			}
			if (isReplaceTypeBySupertypeInInstanceOfEnabled) {
				proposals.addAll(new ReplaceTypeBySupertypeInInstanceOf(context, coveringNode, false).getProposals());
			}
			if (isAddMethodParameterForExpressionEnabled) {
				proposals.addAll(new AddMethodParameterForExpression(context, coveringNode, false).getProposals());
			}
			if (isCreateNewTopLevelSuperClassEnabled) {
				proposals.addAll(new CreateNewTopLevelSuperClass(context, coveringNode, false).getProposals());
			}
			if (isCreateNewTopLevelInterfaceEnabled) {
				proposals.addAll(new CreateNewTopLevelInterface(context, coveringNode, false).getProposals());
			}
			if (isCopyMemberToSubtypeEnabled) {
				proposals.addAll(new CopyMemberToSubtype(context, coveringNode, false).getProposals());
			}
			return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
		}
		return null;
	}

}
