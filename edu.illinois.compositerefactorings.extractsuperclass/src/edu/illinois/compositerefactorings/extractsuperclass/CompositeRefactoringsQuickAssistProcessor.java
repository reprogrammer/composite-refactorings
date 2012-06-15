package edu.illinois.compositerefactorings.extractsuperclass;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
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
					getMoveToImmediateSuperclassProposal(context, coveringNode, false, null);
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
			return resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
		}
		return null;
	}

	private static boolean getCreateNewSuperclassProposal(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation, Collection<ICommandAccess> proposals) throws CoreException {
		if (!(coveringNode instanceof TypeDeclaration)) {
			return false;
		}

		if (proposals == null) {
			return true;
		}

		final ICompilationUnit cu= context.getCompilationUnit();
		ASTNode coveringTypeDeclarationASTNode= context.getCoveringNode();
		ASTNode node= coveringTypeDeclarationASTNode.getParent();
		AST ast= node.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);
		String label= "Create new superclass in file";
		Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
		ASTRewriteCorrectionProposal proposal= new ASTRewriteCorrectionProposal(label, cu, rewrite, 0, image);
		TypeDeclaration newSuperclass= ast.newTypeDeclaration();
		newSuperclass.setName(ast.newSimpleName("NewSuperclass"));
		ListRewrite listRewrite= rewrite.getListRewrite(node, CompilationUnit.TYPES_PROPERTY);
		listRewrite.insertLast(newSuperclass, null);
		rewrite.set(newSuperclass, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, coveringTypeDeclarationASTNode.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY), null);
		rewrite.set(coveringTypeDeclarationASTNode, TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, newSuperclass.getName(), null);
		proposals.add(proposal);
		return true;
	}

	private static boolean getMoveToImmediateSuperclassProposal(IInvocationContext context, ASTNode coveringNode, boolean problemsAtLocation, Collection<ICommandAccess> proposals)
			throws CoreException {
		if (!(coveringNode instanceof SimpleName) || !(coveringNode.getParent() instanceof MethodDeclaration)) {
			return false;
		}
		MethodDeclaration methodDeclaration= (MethodDeclaration)coveringNode.getParent();
		if (methodDeclaration.getName() != coveringNode || methodDeclaration.resolveBinding() == null) {
			return false;
		}
		if (proposals == null) {
			return true;
		}

		final ICompilationUnit cu= context.getCompilationUnit();
		CompilationUnit cuASTNode= (CompilationUnit)methodDeclaration.getRoot();
		ASTNode typeDeclarationASTNode= methodDeclaration.getParent();
		ASTRewrite rewrite= ASTRewrite.create(typeDeclarationASTNode.getAST());
		String label= "Move to immediate superclass";
		Image image= JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
		ASTRewriteCorrectionProposal proposal= new ASTRewriteCorrectionProposal(label, cu, rewrite, 0, image);
		ASTNode placeHolderForMethodDeclaration= rewrite.createMoveTarget(methodDeclaration);
		SimpleType superTypeASTNode= (SimpleType)typeDeclarationASTNode.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY);
		ITypeBinding superTypeBinding= superTypeASTNode.resolveBinding();
		// See http://wiki.eclipse.org/JDT/FAQ#From_an_IBinding_to_its_declaring_ASTNode
		ASTNode SuperTypeDeclarationASTNode= cuASTNode.findDeclaringNode(superTypeBinding);
		ListRewrite superTypeMembersListRewrite= rewrite.getListRewrite(SuperTypeDeclarationASTNode, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		superTypeMembersListRewrite.insertFirst(placeHolderForMethodDeclaration, null);
		rewrite.remove(methodDeclaration, null);
		proposals.add(proposal);
		return true;
	}

}
