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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringStatusDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.illinois.compositerefactorings.messages.CompositeRefactoringsMessages;
import edu.illinois.compositerefactorings.refactorings.createnewtoplevelsuperclass.CreateNewTopLevelSuperClassDescriptor;
import edu.illinois.compositerefactorings.refactorings.createnewtoplevelsuperclass.CreateNewTopLevelSuperClassRefactoring;

@SuppressWarnings("restriction")
public class CreateNewSuperclassCommandHandler extends AbstractHandler {

	@SuppressWarnings("serial")
	public static class InvalidSelectionException extends Exception {

		public InvalidSelectionException(String message) {
			super(message);
		}

	}

	private Set<String> fullyQualifiedName(List<IType> types) {
		Set<String> fullyQualifiedNames= new HashSet<String>();
		for (IType type : types) {
			fullyQualifiedNames.add(type.getFullyQualifiedName());
		}
		return fullyQualifiedNames;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// See http://wiki.bioclipse.net/index.php?title=How_to_add_menus_and_actions&oldid=4857
		Shell shell= HandlerUtil.getActiveShell(event);
		ISelection selection= HandlerUtil.getCurrentSelection(event);
		try {
			List<IType> selectedTypes= getSelectedTypes((IStructuredSelection)selection);
			IProgressMonitor monitor= new NullProgressMonitor();
			CreateNewTopLevelSuperClassDescriptor descriptor= createRefactoringDescriptor(selectedTypes);
			CreateNewTopLevelSuperClassRefactoring refactoring= (CreateNewTopLevelSuperClassRefactoring)descriptor.createRefactoringContext(new RefactoringStatus()).getRefactoring();
			List<IType> possibleTypes= Arrays.asList(refactoring.getCandidateTypes(monitor));
			RefactoringStatus status= new RefactoringStatus();
			status.merge(refactoring.checkInitialConditions(monitor));
			status.merge(refactoring.checkFinalConditions(monitor));
			if (!fullyQualifiedName(possibleTypes).containsAll(fullyQualifiedName(selectedTypes))) {
				status.merge(RefactoringStatus.createErrorStatus("The selected types belong to different class hierarchies."));
			}
			if (status.isOK() || choseToProceed(shell, status)) {
				PerformRefactoringOperation operation= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.INITIAL_CONDITONS);
				operation.run(monitor);
				JavaUI.openInEditor(findNewType(selectedTypes.get(0)));
			}
		} catch (JavaModelException e) {
			throw new ExecutionException("Unexpected selection", e);
		} catch (CoreException e) {
			throw new ExecutionException("Refactoring object creation failed.", e);
		} catch (InvalidSelectionException e) {
			MessageDialog.openError(shell, "Unavailable Refactoring", "The refactoring cannot be performed on the selected entities. Please select one or more types to create a new superclass for.");
		}
		return null;
	}

	private boolean choseToProceed(Shell shell, RefactoringStatus status) {
		return new RefactoringStatusDialog(status, shell, "Refactoring Problems", false).open() == Window.OK;
	}

	// See http://publib.boulder.ibm.com/infocenter/iadthelp/v6r0/index.jsp?topic=/org.eclipse.jdt.doc.isv/guide/jdt_api_open_editor.htm
	private IType findNewType(IType firstSelectedType) {
		return firstSelectedType.getPackageFragment().getCompilationUnit("Super" + firstSelectedType.getElementName() + JavaModelUtil.DEFAULT_CU_SUFFIX).findPrimaryType();
	}

	public static List<IType> getSelectedTypes(IStructuredSelection selection) throws JavaModelException, InvalidSelectionException {
		List<IType> selectedTypes= new ArrayList<IType>();
		for (Object selectionElement : selection.toList()) {
			selectedTypes.add(extractTypeFromSelection(selectionElement));
		}
		return selectedTypes;
	}

	// See org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester.getSingleSelectedType(IStructuredSelection)
	private static IType extractTypeFromSelection(Object selectionElement) throws JavaModelException, InvalidSelectionException {
		if (selectionElement instanceof IType)
			return (IType)selectionElement;
		if (selectionElement instanceof ICompilationUnit) {
			final ICompilationUnit unit= (ICompilationUnit)selectionElement;
			if (unit.exists()) {
				return JavaElementUtil.getMainType(unit);
			}
		}
		throw new InvalidSelectionException(selectionElement + " is not a type");
	}

	private String typeNames(List<IType> types) {
		StringBuilder sb= new StringBuilder();
		for (Iterator<IType> iterator= types.iterator(); iterator.hasNext();) {
			IType type= (IType)iterator.next();
			sb.append(type.getElementName());
			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	private CreateNewTopLevelSuperClassDescriptor createRefactoringDescriptor(List<IType> types) {
		IJavaProject javaProject= types.get(0).getJavaProject();
		String description= MessageFormat.format(CompositeRefactoringsMessages.CreateNewTopLevelSuperClass_description, typeNames(types));
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, JavaRefactoringDescriptorUtil.elementToHandle(javaProject.getElementName(), types.get(0)));
		org.eclipse.jdt.internal.core.refactoring.descriptors.JavaRefactoringDescriptorUtil.setJavaElementArray(arguments, CreateNewTopLevelSuperClassDescriptor.ATTRIBUTE_TYPES,
				JavaRefactoringDescriptorUtil.ATTRIBUTE_ELEMENT, javaProject.getElementName(), types.toArray(new IType[] {}), 1);
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, "Super" + types.get(0).getElementName());
		CreateNewTopLevelSuperClassDescriptor descriptor= new CreateNewTopLevelSuperClassDescriptor(javaProject.getElementName(), description, null, arguments,
				RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		return descriptor;
	}
}
