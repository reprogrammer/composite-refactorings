/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;

public class ChangeUtils {

	// See http://stackoverflow.com/a/880400/130224
	private static void setParentToNull(Change change) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method= Change.class.getDeclaredMethod("setParent", Change.class);
		method.setAccessible(true);
		method.invoke(change, new Object[] { null });
	}

	public static Change[] createChangesWithNullParents(Change[] changes) throws CoreException {
		for (Change change : changes) {
			try {
				setParentToNull(change);
			} catch (Exception e) {
				throw new CoreException(new Status(Status.ERROR, null, "Failed to make a reflective call.", e));
			}
		}
		return changes;
	}

	// See org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring.createCheckConditionsContext()
	public static CheckConditionsContext createCheckConditionsContext(Object validationContext) throws CoreException {
		CheckConditionsContext result= new CheckConditionsContext();
		result.add(new ValidateEditChecker(validationContext));
		result.add(new ResourceChangeChecker());
		return result;
	}

}
