/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.steps;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;

@SuppressWarnings("restriction")
public class JavaTypeSelectionPropertyTester extends PropertyTester {

	private static final String IS_JAVA_TYPE_SELECTED= "isJavaTypeSelected";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof CompilationUnitEditor)) {
			return false;
		}
		if (IS_JAVA_TYPE_SELECTED.equals(property)) {
			CompilationUnitEditor editor= (CompilationUnitEditor)receiver;
			try {
				IType selectedType= SelectionConverter.getTypeAtOffset(editor);
				if (selectedType != null) {
					return true;
				}
			} catch (JavaModelException e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}

}
