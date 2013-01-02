package edu.illinois.compositerefactorings.refactorings.tests;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class RefactoringTestUtilities {

	public static InputStream getFileInputStream(String fileName) throws IOException {
		IPath path= new Path("resources").append(fileName);
		try {
			return Activator.getDefault().getFileInPlugin(path).toURI().toURL().openStream();
		} catch (CoreException e) {
			throw new IOException(e);
		}
	}

}
