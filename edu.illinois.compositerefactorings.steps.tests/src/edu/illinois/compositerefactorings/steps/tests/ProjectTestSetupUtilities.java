/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.steps.tests;

import org.eclipse.jdt.ui.tests.core.ProjectTestSetup;

/**
 * 
 * This class makes some of the protected members of {@link ProjectTestSetup} accessible via
 * composition.
 * 
 */
@SuppressWarnings("restriction")
public class ProjectTestSetupUtilities extends ProjectTestSetup {

	public ProjectTestSetupUtilities() {
		super(null);
	}

	public void setUp() throws Exception {
		super.setUp();
	}

}
