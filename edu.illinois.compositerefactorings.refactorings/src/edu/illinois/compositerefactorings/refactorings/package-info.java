/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * To add a new refactoring called R, follow the steps below:
 * <ul>
 * <li>Create a new package called r inside this package.</li>
 * <li>Implement three classes {@code r.RDescriptor}, {@code r.RRefactoring}, and {@code r.RRefactoringContribution}.</li>
 * <li>Add the refactoring contribution to plugin.xml.</li>
 * </ul>
 */
package edu.illinois.compositerefactorings.refactorings;