package thinj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.bcel.classfile.ClassFormatException;

/**
 * This class is responsible for finding a class file within a java class path.
 */
public class ClassReader {
	// The class path to search in:
	private final File[] aClassPath;

	/**
	 * Constructor
	 * 
	 * @param classPath The ':' - separated list of directories, -zip or -jar files
	 *            wherein a class file shall be found.
	 * 
	 */
	public ClassReader(String classPath) {
		StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
		aClassPath = new File[st.countTokens()];
		for (int i = 0; i < aClassPath.length; i++) {
			String s = st.nextToken();
			aClassPath[i] = new File(s);
		}
	}

	/**
	 * This method will create an InputStream from which the requested class file can be
	 * read. If unable to lookup the file in the class path (supplied in constructor),
	 * this method will call System.exit.
	 * 
	 * @param fileName The file name in the form 'org/example/Foobar'
	 * @return The stream from which the class can be read.
	 */
	public InputStream getClassFileReader(String fileName) {
		// Search through the class path list:
		InputStream fis = null;

		for (int i = 0; i < aClassPath.length && fis == null; i++) {
			if (aClassPath[i].isFile()) {
				// Jar / Zip
				try {
					JarFile jf = new JarFile(aClassPath[i]);
					ZipEntry entry = jf.getJarEntry(fileName + ".class");
					if (entry != null) {
						fis = jf.getInputStream(entry);
					}
					// else: Entry does not exist in this jar; continue search
				} catch (IOException e) {
					System.err.println("Failed reading class '" + fileName + "' from "
							+ aClassPath[i].getAbsolutePath());
					System.exit(1);
				}
			} else if (aClassPath[i].isDirectory()) {
				String s = aClassPath[i].getPath() + File.separator + fileName + ".class";
				File f = new File(s);
				if (f.exists() && f.isFile()) {
					try {
						fis = new FileInputStream(f);
					} catch (FileNotFoundException e) {
						System.err.println("File not found: " + s);
						System.exit(1);
					}
				}
			}
		}

		if (fis == null) {
			Exception e =  new ClassNotFoundException(fileName);
			System.err.println("Class not found: " + fileName);
			e.printStackTrace();
			System.exit(1);
		}
		return fis;
	}

	public static void main(String[] args) throws ClassFormatException, IOException,
			ClassNotFoundException {
		// Example:
		//   java -cp ~/workspace/tinyjvm/bin:$CLASSPATH -Dmycp=bin:/tools/bcel/5.2/bcel-5.2.jar \\ 
		//     tinyjvm.ClassReader tinyjvm/regression/AllTests tinyjvm/regression/gc/GC \\
		//     org/apache/bcel/classfile/ClassParser
		String usage = "usage: java -Dmycp=<class path> ClassReader <class files>+";
		if (args.length < 1) {
			System.err.println(usage);
			System.exit(1);
		}
		String classPath = System.getProperty("mycp");
		if (classPath == null) {
			System.err.println(usage);
			System.exit(1);
		}
		ClassReader cr = new ClassReader(classPath);

		for (String s : args) {
			InputStream is = cr.getClassFileReader(s);
			System.out.println("Found: " + s);
		}
	}
}
