package edu.illinois.compositerefactorings.refactorings.tests;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

@SuppressWarnings("restriction")
public abstract class RefactoringTest extends org.eclipse.jdt.ui.tests.refactoring.RefactoringTest {

	public RefactoringTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		StubUtility.setCodeTemplate(CodeTemplateContextType.NEWTYPE_ID,
				"${package_declaration}" +
						System.getProperty("line.separator", "\n") +
						"${" + CodeTemplateContextType.TYPE_COMMENT + "}" +
						System.getProperty("line.separator", "\n") +
						"${type_declaration}", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.TYPECOMMENT_ID, "", null);
	}

	@Override
	public String getFileContents(String fileName) throws IOException {
		return getContents(RefactoringTestUtilities.getFileInputStream(fileName));
	}

	protected IType getClassFromTestFile(IPackageFragment pack, String className) throws Exception {
		return getType(createCUfromTestFile(pack, className), className);
	}

	protected void createPerformCheckRefactoring(final JavaRefactoringDescriptor descriptor) throws CoreException, Exception {
		final RefactoringStatus status= new RefactoringStatus();
		final Refactoring refactoring= descriptor.createRefactoring(status);
		assertTrue("status should be ok", status.isOK());
		assertNotNull("refactoring should not be null", refactoring);
		assertEquals("was supposed to pass", null, performRefactoring(refactoring));
	}

	protected void compareCompilationUnits(final Map<String, ICompilationUnit> units) throws IOException, JavaModelException {
		for (Map.Entry<String, ICompilationUnit> entry : units.entrySet()) {
			String cuName= entry.getKey();
			ICompilationUnit unit= entry.getValue();
			String expected= getFileContents(getOutputTestFileName(cuName));
			String actual= unit.getSource();
			String message= "incorrect changes in " + unit.getElementName();
			assertEqualLines(message, expected, actual);
		}
	}

	protected Map<String, ICompilationUnit> getCompilationUnits(IPackageFragment packageFragment, List<String> classNames) throws Exception {
		final Map<String, ICompilationUnit> units= new HashMap<String, ICompilationUnit>();
		for (String className : classNames) {
			units.put(className, createCUfromTestFile(packageFragment, className));
		}
		return units;
	}

}
