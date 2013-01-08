/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.copymembertosubtype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.PushDownDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.internal.corext.refactoring.structure.HierarchyProcessor;
import org.eclipse.jdt.internal.corext.refactoring.structure.IMemberActionInfo;
import org.eclipse.jdt.internal.corext.refactoring.structure.ImportRewriteUtil;
import org.eclipse.jdt.internal.corext.refactoring.structure.MemberVisibilityAdjustor;
import org.eclipse.jdt.internal.corext.refactoring.structure.MemberVisibilityAdjustor.IncomingMemberVisibilityAdjustment;
import org.eclipse.jdt.internal.corext.refactoring.structure.TypeVariableMaplet;
import org.eclipse.jdt.internal.corext.refactoring.structure.TypeVariableUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.TextEditBasedChangeManager;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import edu.illinois.compositerefactorings.refactorings.MemberCheckUtil;

@SuppressWarnings("restriction")
public class CopyMemberToSubtypeRefactoringProcessor extends HierarchyProcessor {

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor.MemberActionInfo}
	 */
	public static class MemberActionInfo implements IMemberActionInfo {

		public static final int NO_ACTION= 2;

		public static final int PUSH_ABSTRACT_ACTION= 1;

		public static final int PUSH_DOWN_ACTION= 0;

		private static void assertValidAction(IMember member, int action) {
			if (member instanceof IMethod)
				Assert.isTrue(action == PUSH_ABSTRACT_ACTION || action == NO_ACTION || action == PUSH_DOWN_ACTION);
			else if (member instanceof IField)
				Assert.isTrue(action == NO_ACTION || action == PUSH_DOWN_ACTION);
		}

		public static MemberActionInfo create(IMember member, int action) {
			return new MemberActionInfo(member, action);
		}

		static IMember[] getMembers(MemberActionInfo[] infos) {
			IMember[] result= new IMember[infos.length];
			for (int i= 0; i < result.length; i++) {
				result[i]= infos[i].getMember();
			}
			return result;
		}

		private int fAction;

		private final IMember fMember;

		private MemberActionInfo(IMember member, int action) {
			assertValidAction(member, action);
			Assert.isTrue(member instanceof IField || member instanceof IMethod);
			fMember= member;
			fAction= action;
		}

		boolean copyJavadocToCopiesInSubclasses() {
			return isToBeDeletedFromDeclaringClass();
		}

		public int getAction() {
			return fAction;
		}

		public int[] getAvailableActions() {
			if (isFieldInfo())
				return new int[] { PUSH_DOWN_ACTION, NO_ACTION };

			return new int[] { PUSH_DOWN_ACTION, PUSH_ABSTRACT_ACTION, NO_ACTION };
		}

		public IMember getMember() {
			return fMember;
		}

		int getNewModifiersForCopyInSubclass(int oldModifiers) throws JavaModelException {
			if (isFieldInfo())
				return oldModifiers;
			if (isToBeDeletedFromDeclaringClass())
				return oldModifiers;
			int modifiers= oldModifiers;
			if (isNewMethodToBeDeclaredAbstract()) {
				if (!JdtFlags.isPublic(fMember))
					modifiers= Modifier.PROTECTED | JdtFlags.clearAccessModifiers(modifiers);
			}
			return modifiers;
		}

		int getNewModifiersForOriginal(int oldModifiers) throws JavaModelException {
			if (isFieldInfo())
				return oldModifiers;
			if (isToBeDeletedFromDeclaringClass())
				return oldModifiers;
			int modifiers= oldModifiers;
			if (isNewMethodToBeDeclaredAbstract()) {
				modifiers= JdtFlags.clearFlag(Modifier.FINAL | Modifier.NATIVE, oldModifiers);
				modifiers|= Modifier.ABSTRACT;

				if (!JdtFlags.isPublic(fMember))
					modifiers= Modifier.PROTECTED | JdtFlags.clearAccessModifiers(modifiers);
			}
			return modifiers;
		}

		public boolean isActive() {
			return getAction() != NO_ACTION;
		}

		public boolean isEditable() {
			if (isFieldInfo())
				return false;
			if (getAction() == MemberActionInfo.NO_ACTION)
				return false;
			return true;
		}

		boolean isFieldInfo() {
			return fMember instanceof IField;
		}

		boolean isNewMethodToBeDeclaredAbstract() throws JavaModelException {
			return !isFieldInfo() && !JdtFlags.isAbstract(fMember) && fAction == PUSH_ABSTRACT_ACTION;
		}

		boolean isToBeCreatedInSubclassesOfDeclaringClass() {
			return fAction != NO_ACTION;
		}

		boolean isToBeDeletedFromDeclaringClass() {
			return isToBePushedDown();
		}

		public boolean isToBePushedDown() {
			return fAction == PUSH_DOWN_ACTION;
		}

		public void setAction(int action) {
			assertValidAction(fMember, action);
			if (isFieldInfo())
				Assert.isTrue(action != PUSH_ABSTRACT_ACTION);
			fAction= action;
		}

	}

	private IType fSubtype;

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor.fCachedClassHierarchy}
	 */
	private ITypeHierarchy fCachedClassHierarchy;

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor.fMemberInfos}
	 */
	private MemberActionInfo[] fMemberInfos;

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor.ATTRIBUTE_PUSH}
	 */
	private static final String ATTRIBUTE_PUSH= "push"; //$NON-NLS-1$

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor.IDENTIFIER}
	 */
	public static final String IDENTIFIER= "org.eclipse.jdt.ui.pushDownProcessor"; //$NON-NLS-1$

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#SET_PUSH_DOWN}
	 * The push down group category set
	 */
	private static final GroupCategorySet SET_PUSH_DOWN= new GroupCategorySet(new GroupCategory("org.eclipse.jdt.internal.corext.pushDown", //$NON-NLS-1$
			RefactoringCoreMessages.PushDownRefactoring_category_name, RefactoringCoreMessages.PushDownRefactoring_category_description));


	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#PushDownRefactoringProcessor(IMember[])}
	 */
	public CopyMemberToSubtypeRefactoringProcessor(IMember[] members) {
		super(members, null, false);
		if (members != null) {
			final IType type= RefactoringAvailabilityTester.getTopLevelType(members);
			try {
				if (type != null && RefactoringAvailabilityTester.getPushDownMembers(type).length != 0) {
					fMembersToMove= new IMember[0];
					fCachedDeclaringType= type;
				}
			} catch (JavaModelException exception) {
				JavaPlugin.log(exception);
			}
		}
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#PushDownRefactoringProcessor(JavaRefactoringArguments, RefactoringStatus)}
	 */
	public CopyMemberToSubtypeRefactoringProcessor(JavaRefactoringArguments arguments, RefactoringStatus status) {
		super(null, null, false);
		RefactoringStatus initializeStatus= initialize(arguments);
		status.merge(initializeStatus);
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#rewriteTypeOccurrences(TextEditBasedChangeManager, ASTRequestor, CompilationUnitRewrite, ICompilationUnit, CompilationUnit, Set, IProgressMonitor)}
	 */
	@Override
	protected void rewriteTypeOccurrences(TextEditBasedChangeManager manager, ASTRequestor requestor, CompilationUnitRewrite rewrite, ICompilationUnit unit, CompilationUnit node,
			Set<String> replacements, IProgressMonitor monitor) throws CoreException {
		// Not needed
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#getElements()}
	 */
	@Override
	public Object[] getElements() {
		return fMembersToMove;
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#getIdentifier()}
	 */
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#getProcessorName()}
	 */
	@Override
	public String getProcessorName() {
		return RefactoringCoreMessages.PushDownRefactoring_name;

	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#isApplicable()}
	 */
	@Override
	public boolean isApplicable() throws CoreException {
		return RefactoringAvailabilityTester.isPushDownAvailable(fMembersToMove);
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#createInfosForAllPushableFieldsAndMethods(IType)}
	 */
	private static MemberActionInfo[] createInfosForAllPushableFieldsAndMethods(IType type) throws JavaModelException {
		List<MemberActionInfo> result= new ArrayList<MemberActionInfo>();
		IMember[] pushableMembers= RefactoringAvailabilityTester.getPushDownMembers(type);
		for (int i= 0; i < pushableMembers.length; i++) {
			result.add(MemberActionInfo.create(pushableMembers[i], MemberActionInfo.NO_ACTION));
		}
		return result.toArray(new MemberActionInfo[result.size()]);
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#getAbstractDestinations(IProgressMonitor)}
	 */
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		try {
			monitor.beginTask(RefactoringCoreMessages.PushDownRefactoring_checking, 1);
			RefactoringStatus status= new RefactoringStatus();
			status.merge(checkPossibleSubclasses(new SubProgressMonitor(monitor, 1)));
			if (status.hasFatalError())
				return status;
			status.merge(checkDeclaringType(new SubProgressMonitor(monitor, 1)));
			if (status.hasFatalError())
				return status;
			status.merge(checkIfMembersExist());
			if (status.hasFatalError())
				return status;
			fMemberInfos= createInfosForAllPushableFieldsAndMethods(getDeclaringType());
			List<IMember> list= Arrays.asList(fMembersToMove);
			for (int offset= 0; offset < fMemberInfos.length; offset++) {
				MemberActionInfo info= fMemberInfos[offset];
				if (list.contains(info.getMember())) {
					info.setAction(MemberActionInfo.PUSH_DOWN_ACTION);
				}
			}
			return status;
		} finally {
			monitor.done();
		}
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#checkFinalConditions(IProgressMonitor, CheckConditionsContext)}
	 */
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor monitor, CheckConditionsContext context) throws CoreException, OperationCanceledException {
		try {
			monitor.beginTask(RefactoringCoreMessages.PushDownRefactoring_checking, 5);
			clearCaches();
			ICompilationUnit unit= getDeclaringType().getCompilationUnit();
			if (fLayer) {
				unit= unit.findWorkingCopy(fOwner);
			}
			resetWorkingCopies(unit);
			final RefactoringStatus result= new RefactoringStatus();
			result.merge(checkMembersInDestinationClasses(new SubProgressMonitor(monitor, 1)));
			monitor.worked(1);
			if (result.hasFatalError())
				return result;
			List<IMember> members= new ArrayList<IMember>(fMemberInfos.length);
			for (int index= 0; index < fMemberInfos.length; index++) {
				if (fMemberInfos[index].getAction() != MemberActionInfo.NO_ACTION)
					members.add(fMemberInfos[index].getMember());
			}
			fMembersToMove= members.toArray(new IMember[members.size()]);
			fChangeManager= createChangeManager(new SubProgressMonitor(monitor, 1), result);
			if (result.hasFatalError())
				return result;

			Checks.addModifiedFilesToChecker(ResourceUtil.getFiles(fChangeManager.getAllCompilationUnits()), context);

			return result;
		} finally {
			monitor.done();
		}
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#createChange(IProgressMonitor)}
	 */
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		try {
			final Map<String, String> arguments= new HashMap<String, String>();
			String project= null;
			final IType declaring= getDeclaringType();
			final IJavaProject javaProject= declaring.getJavaProject();
			if (javaProject != null)
				project= javaProject.getElementName();
			int flags= JavaRefactoringDescriptor.JAR_MIGRATION | JavaRefactoringDescriptor.JAR_REFACTORING | RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;
			try {
				if (declaring.isLocal() || declaring.isAnonymous())
					flags|= JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
			} catch (JavaModelException exception) {
				JavaPlugin.log(exception);
			}
			final String description= fMembersToMove.length == 1 ? Messages.format(RefactoringCoreMessages.PushDownRefactoring_descriptor_description_short_multi,
					BasicElementLabels.getJavaElementName(fMembersToMove[0].getElementName())) : RefactoringCoreMessages.PushDownRefactoring_descriptor_description_short;
			final String header= fMembersToMove.length == 1 ? Messages.format(
					RefactoringCoreMessages.PushDownRefactoring_descriptor_description_full,
					new String[] { JavaElementLabels.getElementLabel(fMembersToMove[0], JavaElementLabels.ALL_FULLY_QUALIFIED),
							JavaElementLabels.getElementLabel(declaring, JavaElementLabels.ALL_FULLY_QUALIFIED) }) : Messages.format(
					RefactoringCoreMessages.PushDownRefactoring_descriptor_description, new String[] { JavaElementLabels.getElementLabel(declaring, JavaElementLabels.ALL_FULLY_QUALIFIED) });
			final JDTRefactoringDescriptorComment comment= new JDTRefactoringDescriptorComment(project, this, header);
			final String[] settings= new String[fMembersToMove.length];
			for (int index= 0; index < settings.length; index++)
				settings[index]= JavaElementLabels.getElementLabel(fMembersToMove[index], JavaElementLabels.ALL_FULLY_QUALIFIED);
			comment.addSetting(JDTRefactoringDescriptorComment.createCompositeSetting(RefactoringCoreMessages.PushDownRefactoring_pushed_members_pattern, settings));
			addSuperTypeSettings(comment, true);
			final PushDownDescriptor descriptor= RefactoringSignatureDescriptorFactory.createPushDownDescriptor(project, description, comment.asString(), arguments, flags);
			if (fCachedDeclaringType != null)
				arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, JavaRefactoringDescriptorUtil.elementToHandle(project, fCachedDeclaringType));
			for (int index= 0; index < fMembersToMove.length; index++) {
				arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + (index + 1), JavaRefactoringDescriptorUtil.elementToHandle(project, fMembersToMove[index]));
				for (int offset= 0; offset < fMemberInfos.length; offset++) {
					if (fMemberInfos[offset].getMember().equals(fMembersToMove[index])) {
						arguments.put(ATTRIBUTE_PUSH + (index + 1), Boolean.valueOf(true).toString());
					}
				}
			}
			return new DynamicValidationRefactoringChange(descriptor, RefactoringCoreMessages.PushDownRefactoring_change_name, fChangeManager.getAllChanges());
		} finally {
			pm.done();
			clearCaches();
		}
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#createChangeManager(IProgressMonitor, RefactoringStatus)}
	 */
	private TextEditBasedChangeManager createChangeManager(final IProgressMonitor monitor, final RefactoringStatus status) throws CoreException {
		Assert.isNotNull(monitor);
		Assert.isNotNull(status);
		try {
			monitor.beginTask(RefactoringCoreMessages.PushDownRefactoring_checking, 7);
			final ICompilationUnit source= getDeclaringType().getCompilationUnit();
			final CompilationUnitRewrite sourceRewriter= new CompilationUnitRewrite(source);
			final Map<ICompilationUnit, CompilationUnitRewrite> rewrites= new HashMap<ICompilationUnit, CompilationUnitRewrite>(2);
			rewrites.put(source, sourceRewriter);
			IType[] types= new IType[] { fSubtype };
			final Set<ICompilationUnit> result= new HashSet<ICompilationUnit>(types.length + 1);
			for (int index= 0; index < types.length; index++) {
				ICompilationUnit cu= types[index].getCompilationUnit();
				if (cu != null) { // subclasses can be in binaries
					result.add(cu);
				}
			}
			result.add(source);
			final Map<IMember, IncomingMemberVisibilityAdjustment> adjustments= new HashMap<IMember, IncomingMemberVisibilityAdjustment>();
			final List<MemberVisibilityAdjustor> adjustors= new ArrayList<MemberVisibilityAdjustor>();
			final ICompilationUnit[] units= result.toArray(new ICompilationUnit[result.size()]);
			ICompilationUnit unit= null;
			CompilationUnitRewrite rewrite= null;
			final IProgressMonitor sub= new SubProgressMonitor(monitor, 4);
			try {
				sub.beginTask(RefactoringCoreMessages.PushDownRefactoring_checking, units.length * 4);
				for (int index= 0; index < units.length; index++) {
					unit= units[index];
					rewrite= getCompilationUnitRewrite(rewrites, unit);
					// TODO: Simplify the code by introducing getAbstractTypes.
					final IMember[] members= getAbstractMembers(new IType[] { fSubtype });
					final IType[] classes= new IType[members.length];
					for (int offset= 0; offset < members.length; offset++)
						classes[offset]= (IType)members[offset];

					copyMembers(adjustors, adjustments, rewrites, status, getAbstractMemberInfos(), classes, sourceRewriter, rewrite, sub);
					copyMembers(adjustors, adjustments, rewrites, status, getAffectedMemberInfos(), new IType[] { fSubtype }, sourceRewriter, rewrite, sub);
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
			} finally {
				sub.done();
			}
			if (!adjustors.isEmpty() && !adjustments.isEmpty()) {
				final MemberVisibilityAdjustor adjustor= adjustors.get(0);
				adjustor.rewriteVisibility(new SubProgressMonitor(monitor, 1));
			}
			final TextEditBasedChangeManager manager= new TextEditBasedChangeManager();
			for (final Iterator<ICompilationUnit> iterator= rewrites.keySet().iterator(); iterator.hasNext();) {
				unit= iterator.next();
				rewrite= rewrites.get(unit);
				if (rewrite != null)
					manager.manage(unit, rewrite.createChange(true));
			}
			return manager;
		} finally {
			monitor.done();
		}
	}

	private void copyMembers(Collection<MemberVisibilityAdjustor> adjustors, Map<IMember, IncomingMemberVisibilityAdjustment> adjustments, Map<ICompilationUnit, CompilationUnitRewrite> rewrites,
			RefactoringStatus status, MemberActionInfo[] infos, IType[] destinations, CompilationUnitRewrite sourceRewriter, CompilationUnitRewrite unitRewriter, IProgressMonitor monitor)
			throws JavaModelException {
		try {
			monitor.beginTask(RefactoringCoreMessages.PushDownRefactoring_checking, 1);
			IType type= null;
			TypeVariableMaplet[] mapping= null;
			for (int index= 0; index < destinations.length; index++) {
				type= destinations[index];
				mapping= TypeVariableUtil.superTypeToInheritedType(getDeclaringType(), type);
				if (unitRewriter.getCu().equals(type.getCompilationUnit())) {
					IMember member= null;
					MemberVisibilityAdjustor adjustor= null;
					AbstractTypeDeclaration declaration= ASTNodeSearchUtil.getAbstractTypeDeclarationNode(type, unitRewriter.getRoot());
					ImportRewriteContext context= new ContextSensitiveImportRewriteContext(declaration, unitRewriter.getImportRewrite());
					for (int offset= infos.length - 1; offset >= 0; offset--) {
						member= infos[offset].getMember();
						adjustor= new MemberVisibilityAdjustor(type, member);
						if (infos[offset].isNewMethodToBeDeclaredAbstract())
							adjustor.setIncoming(false);
						adjustor.setRewrite(sourceRewriter.getASTRewrite(), sourceRewriter.getRoot());
						adjustor.setRewrites(rewrites);

						// TW: set to error if bug 78387 is fixed
						adjustor.setFailureSeverity(RefactoringStatus.WARNING);

						adjustor.setStatus(status);
						adjustor.setAdjustments(adjustments);
						adjustor.adjustVisibility(new SubProgressMonitor(monitor, 1));
						adjustments.remove(member);
						adjustors.add(adjustor);
						status.merge(checkProjectCompliance(getCompilationUnitRewrite(rewrites, getDeclaringType().getCompilationUnit()), type, new IMember[] { infos[offset].getMember() }));
						if (infos[offset].isFieldInfo()) {
							final VariableDeclarationFragment oldField= ASTNodeSearchUtil.getFieldDeclarationFragmentNode((IField)infos[offset].getMember(), sourceRewriter.getRoot());
							if (oldField != null) {
								FieldDeclaration newField= createNewFieldDeclarationNode(infos[offset], sourceRewriter.getRoot(), mapping, unitRewriter.getASTRewrite(), oldField);
								unitRewriter
										.getASTRewrite()
										.getListRewrite(declaration, declaration.getBodyDeclarationsProperty())
										.insertAt(newField, ASTNodes.getInsertionIndex(newField, declaration.bodyDeclarations()),
												unitRewriter.createCategorizedGroupDescription(RefactoringCoreMessages.HierarchyRefactoring_add_member, SET_PUSH_DOWN));
								ImportRewriteUtil.addImports(unitRewriter, context, oldField.getParent(), new HashMap<Name, String>(), new HashMap<Name, String>(), false);
							}
						} else {
							final MethodDeclaration oldMethod= ASTNodeSearchUtil.getMethodDeclarationNode((IMethod)infos[offset].getMember(), sourceRewriter.getRoot());
							if (oldMethod != null) {
								MethodDeclaration newMethod= createNewMethodDeclarationNode(infos[offset], mapping, unitRewriter, oldMethod);
								unitRewriter
										.getASTRewrite()
										.getListRewrite(declaration, declaration.getBodyDeclarationsProperty())
										.insertAt(newMethod, ASTNodes.getInsertionIndex(newMethod, declaration.bodyDeclarations()),
												unitRewriter.createCategorizedGroupDescription(RefactoringCoreMessages.HierarchyRefactoring_add_member, SET_PUSH_DOWN));
								ImportRewriteUtil.addImports(unitRewriter, context, oldMethod, new HashMap<Name, String>(), new HashMap<Name, String>(), false);
							}
						}
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#createNewFieldDeclarationNode(MemberActionInfo, CompilationUnit, TypeVariableMaplet[], ASTRewrite, VariableDeclarationFragment)}
	 */
	private FieldDeclaration createNewFieldDeclarationNode(MemberActionInfo info, CompilationUnit declaringCuNode, TypeVariableMaplet[] mapping, ASTRewrite rewrite,
			VariableDeclarationFragment oldFieldFragment) throws JavaModelException {
		Assert.isTrue(info.isFieldInfo());
		IField field= (IField)info.getMember();
		AST ast= rewrite.getAST();
		VariableDeclarationFragment newFragment= ast.newVariableDeclarationFragment();
		newFragment.setExtraDimensions(oldFieldFragment.getExtraDimensions());
		Expression initializer= oldFieldFragment.getInitializer();
		if (initializer != null) {
			Expression newInitializer= null;
			if (mapping.length > 0)
				newInitializer= createPlaceholderForExpression(initializer, field.getCompilationUnit(), mapping, rewrite);
			else
				newInitializer= createPlaceholderForExpression(initializer, field.getCompilationUnit(), rewrite);
			newFragment.setInitializer(newInitializer);
		}
		newFragment.setName(ast.newSimpleName(oldFieldFragment.getName().getIdentifier()));
		FieldDeclaration newField= ast.newFieldDeclaration(newFragment);
		FieldDeclaration oldField= ASTNodeSearchUtil.getFieldDeclarationNode(field, declaringCuNode);
		if (info.copyJavadocToCopiesInSubclasses())
			copyJavadocNode(rewrite, oldField, newField);
		copyAnnotations(oldField, newField);
		newField.modifiers().addAll(ASTNodeFactory.newModifiers(ast, info.getNewModifiersForCopyInSubclass(oldField.getModifiers())));
		Type oldType= oldField.getType();
		ICompilationUnit cu= field.getCompilationUnit();
		Type newType= null;
		if (mapping.length > 0) {
			newType= createPlaceholderForType(oldType, cu, mapping, rewrite);
		} else
			newType= createPlaceholderForType(oldType, cu, rewrite);
		newField.setType(newType);
		return newField;
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#createNewMethodDeclarationNode(MemberActionInfo, TypeVariableMaplet[], CompilationUnitRewrite, MethodDeclaration)}
	 */
	private MethodDeclaration createNewMethodDeclarationNode(MemberActionInfo info, TypeVariableMaplet[] mapping, CompilationUnitRewrite rewriter, MethodDeclaration oldMethod)
			throws JavaModelException {
		Assert.isTrue(!info.isFieldInfo());
		IMethod method= (IMethod)info.getMember();
		ASTRewrite rewrite= rewriter.getASTRewrite();
		AST ast= rewrite.getAST();
		MethodDeclaration newMethod= ast.newMethodDeclaration();
		copyBodyOfPushedDownMethod(rewrite, method, oldMethod, newMethod, mapping);
		newMethod.setConstructor(oldMethod.isConstructor());
		newMethod.setExtraDimensions(oldMethod.getExtraDimensions());
		if (info.copyJavadocToCopiesInSubclasses())
			copyJavadocNode(rewrite, oldMethod, newMethod);
		final IJavaProject project= rewriter.getCu().getJavaProject();
		if (info.isNewMethodToBeDeclaredAbstract() && JavaModelUtil.is50OrHigher(project) && JavaPreferencesSettings.getCodeGenerationSettings(project).overrideAnnotation) {
			final MarkerAnnotation annotation= ast.newMarkerAnnotation();
			annotation.setTypeName(ast.newSimpleName("Override")); //$NON-NLS-1$
			newMethod.modifiers().add(annotation);
		}
		copyAnnotations(oldMethod, newMethod);
		newMethod.modifiers().addAll(ASTNodeFactory.newModifiers(ast, info.getNewModifiersForCopyInSubclass(oldMethod.getModifiers())));
		newMethod.setName(ast.newSimpleName(oldMethod.getName().getIdentifier()));
		copyReturnType(rewrite, method.getCompilationUnit(), oldMethod, newMethod, mapping);
		copyParameters(rewrite, method.getCompilationUnit(), oldMethod, newMethod, mapping);
		copyThrownExceptions(oldMethod, newMethod);
		copyTypeParameters(oldMethod, newMethod);
		return newMethod;
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#copyBodyOfPushedDownMethod(ASTRewrite, IMethod, MethodDeclaration, MethodDeclaration, TypeVariableMaplet[])}
	 */
	private void copyBodyOfPushedDownMethod(ASTRewrite targetRewrite, IMethod method, MethodDeclaration oldMethod, MethodDeclaration newMethod, TypeVariableMaplet[] mapping) throws JavaModelException {
		Block body= oldMethod.getBody();
		if (body == null) {
			newMethod.setBody(null);
			return;
		}
		try {
			final IDocument document= new Document(method.getCompilationUnit().getBuffer().getContents());
			final ASTRewrite rewriter= ASTRewrite.create(body.getAST());
			final ITrackedNodePosition position= rewriter.track(body);
			body.accept(new TypeVariableMapper(rewriter, mapping));
			rewriter.rewriteAST(document, getDeclaringType().getCompilationUnit().getJavaProject().getOptions(true)).apply(document, TextEdit.NONE);
			String content= document.get(position.getStartPosition(), position.getLength());
			String[] lines= Strings.convertIntoLines(content);
			Strings.trimIndentation(lines, method.getJavaProject(), false);
			content= Strings.concatenate(lines, StubUtility.getLineDelimiterUsed(method));
			newMethod.setBody((Block)targetRewrite.createStringPlaceholder(content, ASTNode.BLOCK));
		} catch (MalformedTreeException exception) {
			JavaPlugin.log(exception);
		} catch (BadLocationException exception) {
			JavaPlugin.log(exception);
		}
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#getCompilationUnitRewrite(Map, ICompilationUnit)}
	 */
	private static CompilationUnitRewrite getCompilationUnitRewrite(final Map<ICompilationUnit, CompilationUnitRewrite> rewrites, final ICompilationUnit unit) {
		Assert.isNotNull(rewrites);
		Assert.isNotNull(unit);
		CompilationUnitRewrite rewrite= rewrites.get(unit);
		if (rewrite == null) {
			rewrite= new CompilationUnitRewrite(unit);
			rewrites.put(unit, rewrite);
		}
		return rewrite;
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#getAbstractMembers(IMember[])}
	 */
	private static IMember[] getAbstractMembers(IMember[] members) throws JavaModelException {
		List<IMember> result= new ArrayList<IMember>(members.length);
		for (int i= 0; i < members.length; i++) {
			IMember member= members[i];
			if (JdtFlags.isAbstract(member))
				result.add(member);
		}
		return result.toArray(new IMember[result.size()]);
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#getAbstractMemberInfos()}
	 */
	private MemberActionInfo[] getAbstractMemberInfos() throws JavaModelException {
		List<MemberActionInfo> result= new ArrayList<MemberActionInfo>(fMemberInfos.length);
		for (int index= 0; index < fMemberInfos.length; index++) {
			MemberActionInfo info= fMemberInfos[index];
			if (JdtFlags.isAbstract(info.getMember())) {
				result.add(info);
			}
		}
		return result.toArray(new MemberActionInfo[result.size()]);
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#getAffectedMemberInfos()}
	 */
	private MemberActionInfo[] getAffectedMemberInfos() throws JavaModelException {
		List<MemberActionInfo> result= new ArrayList<MemberActionInfo>(fMemberInfos.length);
		for (int i= 0; i < fMemberInfos.length; i++) {
			MemberActionInfo info= fMemberInfos[i];
			if (!JdtFlags.isAbstract(info.getMember()))
				result.add(info);
		}
		return result.toArray(new MemberActionInfo[result.size()]);
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#getHierarchyOfDeclaringClass(IProgressMonitor)}
	 */
	private ITypeHierarchy getHierarchyOfDeclaringClass(IProgressMonitor monitor) throws JavaModelException {
		try {
			if (fCachedClassHierarchy != null)
				return fCachedClassHierarchy;
			fCachedClassHierarchy= getDeclaringType().newTypeHierarchy(monitor);
			return fCachedClassHierarchy;
		} finally {
			monitor.done();
		}
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#getAbstractDestinations(IProgressMonitor)}
	 */
	private IType[] getAbstractDestinations(IProgressMonitor monitor) throws JavaModelException {
		IType[] allDirectSubclasses= getHierarchyOfDeclaringClass(monitor).getSubclasses(getDeclaringType());
		List<IType> result= new ArrayList<IType>(allDirectSubclasses.length);
		for (int index= 0; index < allDirectSubclasses.length; index++) {
			IType subclass= allDirectSubclasses[index];
			if (subclass.exists() && !subclass.isBinary() && !subclass.isReadOnly() && subclass.getCompilationUnit() != null && subclass.isStructureKnown())
				result.add(subclass);
		}
		return result.toArray(new IType[result.size()]);
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#checkPossibleSubclasses(IProgressMonitor)}
	 */
	private RefactoringStatus checkPossibleSubclasses(IProgressMonitor pm) throws JavaModelException {
		IType[] modifiableSubclasses= getAbstractDestinations(pm);
		if (!Arrays.asList(modifiableSubclasses).contains(fSubtype)) {
			String msg= Messages.format(RefactoringCoreMessages.PushDownRefactoring_no_subclasses,
					new String[] { JavaElementLabels.getTextLabel(getDeclaringType(), JavaElementLabels.ALL_FULLY_QUALIFIED) });
			return RefactoringStatus.createFatalErrorStatus(msg);
		}
		return new RefactoringStatus();
	}

	private RefactoringStatus checkMembersInDestinationClasses(IProgressMonitor monitor) throws JavaModelException {
		monitor.beginTask(RefactoringCoreMessages.PushDownRefactoring_checking, 2);
		RefactoringStatus result= new RefactoringStatus();
		IMember[] membersToPushDown= MemberActionInfo.getMembers(getInfosForMembersToBeCreatedInSubclassesOfDeclaringClass());

		IType[] destinationClassesForNonAbstract= getAbstractDestinations(new SubProgressMonitor(monitor, 1));
		result.merge(checkNonAbstractMembersInDestinationClasses(membersToPushDown, destinationClassesForNonAbstract));
		List<IMember> list= Arrays.asList(getAbstractMembers(getAbstractDestinations(new SubProgressMonitor(monitor, 1))));

		IType[] destinationClassesForAbstract= list.toArray(new IType[list.size()]);
		result.merge(checkAbstractMembersInDestinationClasses(membersToPushDown, destinationClassesForAbstract));
		monitor.done();
		return result;
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#checkNonAbstractMembersInDestinationClasses(IMember[], IType[])}
	 */
	private RefactoringStatus checkNonAbstractMembersInDestinationClasses(IMember[] membersToPushDown, IType[] destinationClassesForNonAbstract) throws JavaModelException {
		RefactoringStatus result= new RefactoringStatus();
		List<IMember> list= new ArrayList<IMember>(); // Arrays.asList does not support removing
		list.addAll(Arrays.asList(membersToPushDown));
		list.removeAll(Arrays.asList(getAbstractMembers(membersToPushDown)));
		IMember[] nonAbstractMembersToPushDown= list.toArray(new IMember[list.size()]);
		for (int i= 0; i < destinationClassesForNonAbstract.length; i++) {
			result.merge(MemberCheckUtil.checkMembersInDestinationType(nonAbstractMembersToPushDown, destinationClassesForNonAbstract[i]));
		}
		return result;
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#checkAbstractMembersInDestinationClasses(IMember[], IType[])}
	 */
	private RefactoringStatus checkAbstractMembersInDestinationClasses(IMember[] membersToPushDown, IType[] destinationClassesForAbstract) throws JavaModelException {
		RefactoringStatus result= new RefactoringStatus();
		IMember[] abstractMembersToPushDown= getAbstractMembers(membersToPushDown);
		for (int index= 0; index < destinationClassesForAbstract.length; index++) {
			result.merge(MemberCheckUtil.checkMembersInDestinationType(abstractMembersToPushDown, destinationClassesForAbstract[index]));
		}
		return result;
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#getInfosForMembersToBeCreatedInSubclassesOfDeclaringClass()}
	 */
	private MemberActionInfo[] getInfosForMembersToBeCreatedInSubclassesOfDeclaringClass() throws JavaModelException {
		MemberActionInfo[] abs= getAbstractMemberInfos();
		MemberActionInfo[] nonabs= getAffectedMemberInfos();
		List<MemberActionInfo> result= new ArrayList<MemberActionInfo>(abs.length + nonabs.length);
		result.addAll(Arrays.asList(abs));
		result.addAll(Arrays.asList(nonabs));
		return result.toArray(new MemberActionInfo[result.size()]);
	}

	/**
	 * {@link org.eclipse.jdt.internal.corext.refactoring.structure.PushDownRefactoringProcessor#initialize(JavaRefactoringArguments)}
	 */
	private RefactoringStatus initialize(JavaRefactoringArguments extended) {
		String handle= extended.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT);
		if (handle != null) {
			final IJavaElement element= JavaRefactoringDescriptorUtil.handleToElement(extended.getProject(), handle, false);
			if (element == null || !element.exists() || element.getElementType() != IJavaElement.TYPE) {
				return JavaRefactoringDescriptorUtil.createInputFatalStatus(element, getProcessorName(), IJavaRefactorings.PUSH_DOWN);
			} else {
				fCachedDeclaringType= (IType)element;
			}
		}
		final List<IJavaElement> elements= new ArrayList<IJavaElement>();
		final List<MemberActionInfo> infos= new ArrayList<MemberActionInfo>();
		String attribute= JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT + 1;
		final RefactoringStatus status= new RefactoringStatus();
		handle= extended.getAttribute(attribute);
		final IJavaElement element= JavaRefactoringDescriptorUtil.handleToElement(extended.getProject(), handle, false);
		if (element == null || !element.exists()) {
			status.merge(JavaRefactoringDescriptorUtil.createInputWarningStatus(element, getProcessorName(), IJavaRefactorings.PUSH_DOWN));
		} else {
			elements.add(element);
		}
		infos.add(MemberActionInfo.create((IMember)element, MemberActionInfo.PUSH_DOWN_ACTION));
		fMembersToMove= elements.toArray(new IMember[elements.size()]);
		fMemberInfos= infos.toArray(new MemberActionInfo[infos.size()]);
		String subtypeHandle= extended.getAttribute(CopyMemberToSubtypeDescriptor.ATTRIBUTE_SUBTYPE + 1);
		final IJavaElement subtype= JavaRefactoringDescriptorUtil.handleToElement(extended.getProject(), subtypeHandle, false);
		if (subtype == null || !subtype.exists()) {
			status.merge(JavaRefactoringDescriptorUtil.createInputWarningStatus(subtype, getProcessorName(), IJavaRefactorings.PUSH_DOWN));
		} else {
			fSubtype= (IType)subtype;
		}
		if (!status.isOK()) {
			return status;
		}
		return new RefactoringStatus();
	}

}
