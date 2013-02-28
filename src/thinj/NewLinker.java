package thinj;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.Type;

import thinj.instructions.AbstractInstruction;
import thinj.instructions.InstructionHandler;
import thinj.linkmodel.ClassInSuite;
import thinj.linkmodel.ClassReference;
import thinj.linkmodel.ClassTypeEnum;
import thinj.linkmodel.ExceptionHandler;
import thinj.linkmodel.LinkModel;
import thinj.linkmodel.Member;
import thinj.linkmodel.MemberReference;
import thinj.linkmodel.MethodInClass;
import thinj.linkmodel.MethodOrField;
import thinj.linkmodel.Signature;

// // For JVM instructions, see:
// http://www.daimi.au.dk/dOvs/jvmspec/ref-ifne.html

public class NewLinker {
	private LinkModel aLinkModel;

	// The one and only method executing our program:
	private MethodInClass aInitMethod;

	private ClassReader aClassReader;
	private int aClassId;
	private final String aOutputBaseName;

	/**
	 * Constructor. When constructor returns the suite has been generated.
	 * 
	 * @param classPath The class path in which the references classes, methods etc. are loaded.
	 *            Only the referenced items will be included in the final suite.
	 * @param requiredReferences An array of required methods and field.
	 * @param mainClassName The class containing the main - method. Note that the main-method shall
	 *            have no args.
	 * @param vmClassReferences Classes referenced by the VM. This will lead to generation of link
	 *            constants (link ids)
	 * @param vmMemberReferences Class member referenced by the VM. This will lead to generation of
	 *            link constants (link ids)
	 * @throws IOException If any common I/O errors occur
	 * @throws ClassNotFoundException If unable to load a referenced class
	 */
	public NewLinker(String classPath, String outputBaseName, String[] requiredReferences,
			String[] vmClassReferences, String[] vmMemberReferences, String mainClassName)
			throws IOException, ClassNotFoundException {
		aOutputBaseName = outputBaseName;
		aLinkModel = LinkModel.getInstance();
		aClassReader = new ClassReader(classPath);
		aClassId = 0;

		// Create synthetic classes as the first:
		createSyntheticClasses();

		// Build a list of the classes referenced from the VM:
		HashSet<String> vmClasses = new HashSet<String>();
		// Load mandatory classes referenced by VM:
		for (String className : vmClassReferences) {
			ClassInSuite cl = loadClass(ClassInSuite.getGlobalName(className));
			vmClasses.add(cl.getClassName());
		}

		// 'Main' is our starting point:
		handleReference(mainClassName, "main", "([Ljava/lang/String;)V");

		// Reference the required references:
		includeReferences(requiredReferences);
		List<Member> vmRefList = includeReferences(vmMemberReferences);

		handleDecendants();

		StaticClassLoader classLoader = new StaticClassLoader(aLinkModel);
		aInitMethod = classLoader.createInitCode(ClassInSuite.getGlobalName(mainClassName));

		aLinkModel.link();
		aLinkModel.optimize();

		CodeGenerator cg = new CodeGenerator(aLinkModel);
		cg.generateCode(mainClassName, aOutputBaseName, aInitMethod.getCodeOffset(), vmClasses,
				vmRefList);
	}

	/**
	 * This method iterates through the references and ensures that the target of each reference is
	 * included
	 * 
	 * @param references All the references to include
	 * @return A list of {@link Member}s
	 */
	private List<Member> includeReferences(String[] references) {
		LinkedList<Member> members = new LinkedList<Member>();
		for (String ref : references) {
			Member m = toMember(ref);
			members.add(m);
			handleReference(m.getClassName(), m.getSignature().getName(), m.getSignature()
					.getDescriptor());
		}

		return members;
	}

	/**
	 * This method converts the supplied member reference 'ref' to a {@link Member} instance by
	 * splitting 'ref' on white spaces
	 * 
	 * @param ref The member reference
	 * @return The corresponding Member instance
	 */
	public static Member toMember(String ref) {
		StringTokenizer st = new StringTokenizer(ref, " ");
		if (st.countTokens() != 3) {
			NewLinker.exit("Wrong format of dependency: " + ref, 1);
		}
		String clName = ClassInSuite.getGlobalName(st.nextToken());
		String memberName = st.nextToken();
		String signature = st.nextToken();
		return new Member(clName, memberName, signature);
	}

	/**
	 * This method will handle the following problem: If a class 'B' extends 'A', and 'A' defines a
	 * non-final method 'm', which then is overload by another method 'f' in 'B', then we cannot
	 * rely on an explicit reference to B.f. However, if 'A.f' is referenced, 'B.f' shall be handled
	 * as a referenced method as well. Example: If 'Object.equals(Object)' is overloaded, then the
	 * overloaded method shall be included in the final suite as well.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws ClassFormatException
	 */
	private void handleDecendants() throws ClassFormatException, IOException,
			ClassNotFoundException {
		// Repeat until no references has been added
		boolean referenceAdded;
		do {
			referenceAdded = false;
			// Only interested in referenced classes:
			List<ClassInSuite> referencedClasses = getReferencedClasses();
			for (ClassInSuite cis : referencedClasses) {
				List<MethodInClass> referencedMethods = getReferencedMethods(cis);

				// Get all classes extending 'cis' (only one level down):
				List<ClassInSuite> extendingClasses = aLinkModel.getAllClassesExtending(cis
						.getClassId());
				// Get all classes implementing 'cis':
				List<ClassInSuite> implementingClasses = aLinkModel.getAllClassesImplementing(cis
						.getClassId());
				List<ClassInSuite> subClasses = new LinkedList<ClassInSuite>(extendingClasses);
				subClasses.addAll(implementingClasses);

				// Iterate over all combinations of (sub classes, referencedMethods):
				for (MethodInClass mic : referencedMethods) {
					for (ClassInSuite extCis : subClasses) {
						MethodInClass extMic = (MethodInClass) aLinkModel.getMethodOrFieldInClass(
								extCis.getClassId(), mic.getMember().getSignature().getName(), mic
										.getMember().getSignature().getDescriptor());
						if (extMic != null && !extMic.isReferenced()
								&& extMic.getType() != MethodInClass.Type.Constructor) {
							handleReference(extCis.getClassName(), extMic.getMember()
									.getSignature().getName(), extMic.getMember().getSignature()
									.getDescriptor());
							referenceAdded = true;
						}
					}
				}
			}
		} while (referenceAdded);
	}

	/**
	 * This method builds and returns a list containing all referenced classes
	 * 
	 * @return A list containing all referenced classes
	 */
	private List<ClassInSuite> getReferencedClasses() {
		LinkedList<ClassInSuite> l = new LinkedList<ClassInSuite>();

		ClassInSuite[] allClasses = aLinkModel.getAllClasses();
		for (ClassInSuite cis : allClasses) {
			// Only interested in referenced classes:
			if (cis.isReferenced()) {
				l.add(cis);
			}
		}

		return l;
	}

	/**
	 * This method builds a list of all referenced methods from a class. This includes directly
	 * referenced methods from the class and indirectly referenced methods referenced via
	 * implemented interfaces.
	 * 
	 * @param cis The class for which a list of referenced methods shall be build
	 * @return The list of referenced methods.
	 */
	private List<MethodInClass> getReferencedMethods(ClassInSuite cis) {
		// Get all methods from class:
		MethodInClass[] allMethods = aLinkModel.getMethods(cis.getClassId());

		// Build a list of referenced methods only:
		LinkedList<MethodInClass> referencedMethods = new LinkedList<MethodInClass>();
		for (MethodInClass mic : allMethods) {
			if (mic.isReferenced()) {
				referencedMethods.add(mic);
			}
		}

		// Get names of all implemented interfaces:
		String[] allInterfaces = cis.getImplementedInterfaces();
		for (String interfaceName : allInterfaces) {
			ClassInSuite intf = aLinkModel.getClassByName(interfaceName);
			// Get all methods from interface:
			MethodInClass[] intfMethods = aLinkModel.getMethods(intf.getClassId());
			for (MethodInClass mic : intfMethods) {
				if (mic.isReferenced()) {
					referencedMethods.add(mic);
				}
			}
		}

		return referencedMethods;
	}

	/**
	 * This method handles a reference to a member. 'handles' means that when this method returns
	 * the class has been added to the link model; the referenced member has been marked as a
	 * dependency; and if the class or method hasn't been referenced before its dependencies are
	 * handled in the same manner (using recursion).
	 * 
	 * @param referencedClass The class to reference to
	 * @param memberName The name of the member to reference
	 * @param signature The signature of the referenced member
	 */
	private void handleReference(String referencedClass, String memberName, String signature) {
		// System.out.println("handleRef: " + referencedClass + ", " + memberName + ", " +
		// signature);
		referencedClass = ClassInSuite.getGlobalName(referencedClass);
		ClassInSuite cl = aLinkModel.getClassByName(referencedClass);
		if (cl == null) {
			// Class not loaded; load it:
			cl = loadClass(referencedClass);
		}
		// Mark class as referenced (only necessary in order to eliminate non-referenced
		// synthetic classes):
		cl.referenced();

		// Establish reference to target and scan instructions, if target is a method:
		MethodOrField mof = aLinkModel.getMethodOrField(cl.getClassId(), memberName, signature);
		if (mof == null) {
			System.err.println("Unresolved reference: " + cl.getClassName() + "#" + memberName
					+ signature);
			System.exit(1);
		}
		if (!mof.isReferenced()) {
			// Avoid endless recursion:
			mof.referenced();
			if (mof instanceof MethodInClass) {
				MethodInClass mic = (MethodInClass) mof;
				// Handle all class references in method code (method=mic):
				for (ClassReference cref : mic.getAllClassDependencies()) {
					cref.referenced();
					loadClass(cref.getClassName());
				}

				// Handle all simple array references in method code (method=mic):
				for (ClassTypeEnum type : mic.getAllSimpleArrayDependencies()) {
					loadClass(type.getSignature());
				}

				// Handle all member references in method code (method=mic):
				MemberReference[] refs = mic.getMemberReferences();
				for (MemberReference ref : refs) {
					// Mark reference as necessary:
					ref.referenced();
					handleReference(ref.getReferencedClassName(), ref.getSignature().getName(), ref
							.getSignature().getDescriptor());
				}
			}
			// else field: no code etc. to traverse
		}
		// else: Ignore; already ref'ed
	}

	/**
	 * This method loads a class into the link model. If unable to load, this method will call
	 * System.exit(1); so any return is a successful return.
	 * 
	 * @param referencedClass The name of the class to load. Shall be in '/' - format.
	 * @return The loaded class
	 */
	private ClassInSuite loadClass(String referencedClass) {
		ClassInSuite classInSuite = aLinkModel.getClassByName(referencedClass);

		if (classInSuite == null) {
			// Check for array:
			if (referencedClass.startsWith("[")) {
				classInSuite = loadArray(referencedClass);
			} else {
				// InputStream is = aClassReader.getClassFileReader(referencedClass);
				// JavaClass jc = null;
				// JavaClass superClass = null;
				// JavaClass[] allInterfaces = null;
				// try {
				// jc = new ClassParser(is, referencedClass).parse();
				// superClass = jc.getSuperClass();
				// allInterfaces = jc.getAllInterfaces();
				// } catch (Exception e) {
				// System.err.println("Failed loading class: " + referencedClass);
				// e.printStackTrace();
				// System.exit(1);
				// }
				// NYT
				JavaClass jc = readClassFromFile(referencedClass);
				JavaClass superClass = readClassFromFile(jc.getSuperclassName());
				JavaClass[] allInterfaces = readClassesFromFiles(jc.getInterfaceNames());
				// NYT

				boolean isJavaLangObject = referencedClass.equals("java/lang/Object");
				// Register the class in our model:
				classInSuite = aLinkModel.createClassInSuite(jc.getClassName(), aClassId++,
						isJavaLangObject ? null : superClass.getClassName(),
						jc.isInterface() ? ClassTypeEnum.InterfaceType : ClassTypeEnum.ClassType);

				// Avoid endless recursion:
				classInSuite.referenced();

				// Load all implemented interfaces:
				for (JavaClass interfaceClass : allInterfaces) {
					String intfName = ClassInSuite.getGlobalName(interfaceClass.getClassName());
					// Strange... it seems that getAllInterfaces() also return the name of the
					// interface it self... Avoid self-referencing:
					if (!intfName.equals(referencedClass)) {
						classInSuite.addImplementedInterface(intfName);
						loadClass(intfName);
					}
				}

				// NB!!! ConstantPool contains constants for entire class !!!
				loadConstantPool(jc.getConstantPool(), classInSuite.getClassId());

				// Collect fields:
				for (int i = 0; i < jc.getFields().length; i++) {
					Field field = jc.getFields()[i];
					aLinkModel.createFieldInClass(jc.getClassName(), field.getName(),
							field.getSignature(), field.getType().getSize(), field.isStatic());
				}

				loadMethods(classInSuite.getClassId(), isJavaLangObject, jc);

				// Make sure references to class-init method are referenced:
				for (MethodInClass mic : aLinkModel.getMethods(classInSuite.getClassId())) {
					if (mic.getType() == MethodInClass.Type.ClassInitCode && !mic.isReferenced()) {
						// Refer to static all <clinit> - methods:
						// System.out.println("clinit ref: " + referencedClass + "." +
						// mic.getMember().getSignature().getName() + "#" +
						// mic.getMember().getSignature().getDescriptor());
						handleReference(referencedClass, mic.getMember().getSignature().getName(),
								mic.getMember().getSignature().getDescriptor());
					}
				}
			}
		}
		return classInSuite;
	}

	/**
	 * This method reads an array JavaClass from some .class files
	 * 
	 * @param interfaceNames The names of the classes
	 * @return The read JavaClass'es. If unable to read, this method exits.
	 */
	private JavaClass[] readClassesFromFiles(String[] interfaceNames) {
		LinkedList<JavaClass> l = new LinkedList<JavaClass>();
		for (String name : interfaceNames) {
			l.add(readClassFromFile(name));
		}

		return l.toArray(new JavaClass[l.size()]);
	}

	/**
	 * This method reads a JavaClass from a .class file
	 * 
	 * @param referencedClass The name of the class
	 * @return The read JavaClass. If unable to read, this method exits.
	 */
	private JavaClass readClassFromFile(String referencedClass) {
		InputStream is = aClassReader.getClassFileReader(ClassInSuite
				.getGlobalName(referencedClass));
		JavaClass jc = null;
		try {
			jc = new ClassParser(is, referencedClass).parse();
		} catch (Exception e) {
			exit("Failed loading class: " + referencedClass, 1);
		}
		return jc;
	}

	/**
	 * This method loads an array: First the array element class is loaded; then the array class is
	 * created
	 * 
	 * @param referencedClass The name of the array
	 * @return The array class corresponding to 'referencedClass'
	 * @throws ClassFormatException See {@link #loadClass(String)}
	 * @throws IOException See {@link #loadClass(String)}
	 * @throws ClassNotFoundException See {@link #loadClass(String)}
	 */
	private ClassInSuite loadArray(String referencedClass) {
		ClassInSuite cis;
		ClassTypeEnum classType = ClassTypeEnum.resolveArrayType(referencedClass);
		if (classType != null) {
			// Simple type array:
			cis = aLinkModel.createArrayClassInSuite(classType);
		} else {
			// Object array:
			// Skip leading '[':
			String elementClassName = referencedClass.substring(1);
			ClassInSuite elementClass = null;
			if (elementClassName.startsWith("[")) {
				// It's an array of array!
				elementClass = loadArray(elementClassName);
			} else if (elementClassName.startsWith("L")) {
				// Skip leading 'L':
				elementClass = loadClass(elementClassName.substring(1));
			} else {
				System.err.println("Unexpected array name: " + referencedClass + "--"
						+ elementClassName);
				System.exit(1);
			}

			cis = aLinkModel.createObjectArrayClassInSuite(elementClass.getClassName(), aClassId++);
		}

		cis.referenced();
		return cis;
	}

	/**
	 * This method loads all methods in the class. They are *not* noted as referenced. The
	 * instruction list in each method is scanned and any referenced are handled (recursion).
	 * 
	 * @param classId The id of the embracing class
	 * @param isJavaLangObject true if the embracing class is java.lang.Object
	 * @param jc The bcel - representation of the loaded class
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void loadMethods(int classId, boolean isJavaLangObject, JavaClass jc) {
		int constantPoolLength = jc.getConstantPool().getLength();
		for (int i = 0; i < jc.getMethods().length; i++) {
			Method m = jc.getMethods()[i];

			if (isJavaLangObject && m.getName().equals("<init>")) {
				// Patch java.lang.Object default constructor - no call to any super class:
				// m.getCode().setCode(toByteArray(new int[] { 0xB1 }));
				m.getCode().setCode(new byte[] { (byte) 0xB1 });
			}
			// Determine number of arguments to method - 'this' counts also as an
			// argument:
			int numberOfArguments = (m.isStatic() ? 0 : 1);
			for (Type mx : m.getArgumentTypes()) {
				// Count long / double as 2 arguments:
				numberOfArguments += mx.getSize();
			}

			// Find number of local variables in method:
			if (!m.isAbstract() && !m.isNative()) {
				// System.err.println(new InstructionList(m.getCode().getCode()));
				int maxIndex = m.getCode().getMaxLocals();

				MethodInClass mic = aLinkModel.createMethodInClass(jc.getClassName(), m.getName(),
						m.getSignature(), m.getCode().getCode(), maxIndex, numberOfArguments,
						m.isStatic());
				// System.err.println("micmic: " + jc.getClassName() + "." + m.getName() + "(("
				// + m.getSignature());
				// Add line number information:
				LineNumberTable lnt = m.getCode().getLineNumberTable();
				for (LineNumber ln : lnt.getLineNumberTable()) {
					mic.appendLineNumber(ln.getLineNumber(), ln.getStartPC());
				}

				// Load exceptions:
				loadExceptions(m, classId, mic);

				// Scan instructions and register any references in mic:
				// InstructionList il = new InstructionList(m.getCode().getCode());
				// scanInstructions(jc.getConstantPool(), il, mic, classId);
				constantPoolLength = scanCode(mic, classId, constantPoolLength);
			} else if (m.isAbstract()) {
				// Create abstract method entry in model:
				aLinkModel.createAbstractMethodInClass(jc.getClassName(), m.getName(),
						m.getSignature(), numberOfArguments);
			} else if (m.isNative()) {
				String[] argTypes = Utility.methodSignatureArgumentTypes(m.getSignature());
				String returnType = Utility.methodSignatureReturnType(m.getSignature());
				aLinkModel.createNativeMethodInClass(jc.getClassName(), m.getName(),
						m.getSignature(), m.isStatic(), argTypes, returnType);
			} else {
				System.err.println("?? unknown message type..." + jc.getClassName() + "#"
						+ m.getName());
				System.exit(1);
			}
		}
	}

	/**
	 * This method loads exceptions and handlers for the supplied method. Nothing is marked as
	 * 'referenced'.
	 * 
	 * @param m The method
	 * @param classId The id of the class defining the method
	 * @param mic thinJ representation of the method
	 */
	private void loadExceptions(Method m, int classId, MethodInClass mic) {
		// Get the exceptions thrown by the method:
		ExceptionTable table = m.getExceptionTable();
		if (table != null) {
			// Register the dependencies of the thrown exceptions:
			// System.out.println("classId: " + classId + "; method = " + m);
			for (int i = 0; i < table.getExceptionIndexTable().length; i++) {
				// Register dependency:
				String name = ClassInSuite.getGlobalName(table.getExceptionNames()[i]);
				ClassReference classReference = aLinkModel.createClassReference(classId,
						table.getExceptionIndexTable()[i], name);

				mic.addClassDependency(classReference);
			}

			// System.out.println("#excep: " + table.getNumberOfExceptions());
			// System.exit(1);
		}

		if (m.getCode() != null && m.getCode().getExceptionTable() != null
				&& m.getCode().getExceptionTable().length > 0) {
			// System.out.println("classId: " + classId + "; method = " + m);
			// Register exception handlers:
			for (int i = 0; i < m.getCode().getExceptionTable().length; i++) {
				CodeException codex = m.getCode().getExceptionTable()[i];
				// System.err.println("classId = " + classId + ", cpIx = " + codex.getCatchType());
				if (codex.getCatchType() != 0) {
					ClassReference cref = aLinkModel.getClassReference(classId,
							codex.getCatchType());
					mic.addClassDependency(cref);
				}
				ExceptionHandler handler = new ExceptionHandler(classId, codex.getStartPC(),
						codex.getEndPC(), codex.getHandlerPC(), codex.getCatchType());
				mic.addExceptionHandler(handler);
				// System.out.println("my codex: " + handler + "; cpIx = " + codex.getCatchType());
			}
		}
	}

	/**
	 * This method scans an instruction list looking for references. If any found; these are handled
	 * using the {@link #handleReference(String, String, String)} method. So we are indeed
	 * recursing. <br/>
	 * <br/>
	 * Note! {@link #loadClass(String)} shall under no circumstances be called from this method!
	 * 
	 * @param mic The method in which the references shall be registered
	 * @param classId The classId of the class containing the instructions being scanned
	 * @param constantPoolLength The number of elements in the constant pool
	 * @return The new constant pool length after scanning the code. Some extra elements might be
	 *         added during the scan
	 */
	private int scanCode(final MethodInClass mic, final int classId, int constantPoolLength) {
		final IntInABox cpl = new IntInABox(constantPoolLength);
		AbstractInstruction.disassemble(0, mic.getCode(), new InstructionHandler() {
			@Override
			public void handle(int address, AbstractInstruction instruction) {
				instruction.registerDependencies(aLinkModel, classId, mic, cpl);
			}
		});

		return cpl.getValue();
	}

	/**
	 * This method loads all classes that are not explicitly referenced.
	 * 
	 * @throws ClassFormatException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void createSyntheticClasses() throws ClassFormatException, IOException,
			ClassNotFoundException {
		// Always java.lang.Object as the first class:
		loadClass(ClassInSuite.getGlobalName(Object.class.getName()));
		aClassId = ClassTypeEnum.values().length + 4;
		// loadClass("java/lang/Class");
		// loadClass(ClassInSuite.getGlobalName(Class.class.getName()));
		handleReference(ClassInSuite.getGlobalName(Class.class.getName()), "aClassId", "I");

		// If the simple type arrays are not referenced from java - classes, they are not included.
		// However, if native methods use these types, they cannot be found at runtime => it is
		// necessary to reference all simple array types. An example might be native code as this:
		// jarray ba = NewByteArray(sizeof(contextDef));
		// (Example from Java_java_lang_Thread.c)

		loadArray("[C");
		loadArray("[B");
		loadArray("[I");
		loadArray("[J");
		// As long as our own java.lang.Class does not allocate it's own Class[] we will have to
		// help referencing Object[]:
		// TODO Rework java.lang.Class
		loadClass("[L" + ClassInSuite.getGlobalName(Object.class.getName()));
		// More assistance to Class[]:
		loadClass("[L" + ClassInSuite.getGlobalName(Class.class.getName()));
	}

	/**
	 * This method loads the constant pool for the entire class
	 * 
	 * @param cp The constant pool to load
	 * @param classId The id of the class containing the constant pool
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws ClassFormatException
	 */
	private void loadConstantPool(ConstantPool cp, int classId) {
		Constant[] constantPool = cp.getConstantPool();
		for (int i = 0; i < constantPool.length; i++) {
			Constant cons = constantPool[i];

			if (cons == null) {
				// Ignore
			} else if (cons instanceof ConstantClass) {
				ConstantClass cc = (ConstantClass) cons;
				String className = getName(constantPool, cc.getNameIndex());
				if (className.endsWith(";")) {
					className = className.substring(0, className.length() - 1);
				}
				aLinkModel.createClassReference(classId, i, className);
			} else if (cons instanceof ConstantUtf8) {
				// ConstantUtf8 c8 = (ConstantUtf8) cons;
				// System.out.println("8utf: " + c8);
			} else if (cons instanceof ConstantMethodref) {
				// Is added during scan of instructions
				ConstantMethodref ref = (ConstantMethodref) cons;
				addMethodReference(ref.getClassIndex(), ref.getNameAndTypeIndex(), i, constantPool,
						classId);
			} else if (cons instanceof ConstantNameAndType) {
			} else if (cons instanceof ConstantInterfaceMethodref) {
				// Is added during scan of instructions
				ConstantInterfaceMethodref ref = (ConstantInterfaceMethodref) cons;
				addMethodReference(ref.getClassIndex(), ref.getNameAndTypeIndex(), i, constantPool,
						classId);
			} else if (cons instanceof ConstantFieldref) {
				// Is added during scan of instructions
				addFieldReference((ConstantFieldref) cons, i, constantPool, classId);
			} else if (cons instanceof ConstantInteger) {
				aLinkModel.createIntegerConstantReference(classId, i,
						((ConstantInteger) cons).getBytes());
			} else if (cons instanceof ConstantString) {
				ConstantString cs = (ConstantString) cons;
				aLinkModel.createStringConstantReference(classId, i, cs.getBytes(cp));
				// A string constant requires that java/lang/string is present:
				loadClass("java/lang/String");
			} else if (cons instanceof ConstantLong) {
				ConstantLong cs = (ConstantLong) cons;
				aLinkModel.createLongConstantReference(classId, i, cs.getBytes());
			} else if (cons instanceof ConstantFloat) {
				aLinkModel.createFloatConstantReference(classId, i,
						((ConstantFloat) cons).getBytes());
			} else if (cons instanceof ConstantDouble) {
				aLinkModel.createDoubleConstantReference(classId, i,
						((ConstantDouble) cons).getBytes());
			} else {
				System.err.println("Unsupported constant type: " + cons.getClass().getName());
				System.exit(1);
			}
		}
	}

	// ----------------------------------------------//
	/**
	 * This method adds a reference to a method to the link model.
	 * 
	 * @param classIndex The index into the constant pool where the containing class is defined
	 * @param nameAndTypeIndex The name and signature of the referenced field
	 * @param index The index into the constant pool of class identified by classId
	 * @param constantPool The constant pool to extract the textual reference from
	 * @param classId The id identifying the class
	 */
	private void addMethodReference(int classIndex, int nameAndTypeIndex, int index,
			Constant[] constantPool, int classId) {
		addMethodOrFieldReference(classIndex, nameAndTypeIndex, index, constantPool, classId);
	}

	/**
	 * This method adds a reference to a field to the link model.
	 * 
	 * @param ref The constant pool containing the reference
	 * @param index The index into the constant pool of class identified by classId
	 * @param constantPool The constant pool to extract the textual reference from
	 * @param classId The id identifying the class
	 */
	private void addFieldReference(ConstantFieldref ref, int index, Constant[] constantPool,
			int classId) {
		addMethodOrFieldReference(ref.getClassIndex(), ref.getNameAndTypeIndex(), index,
				constantPool, classId);
	}

	/**
	 * This method adds a reference to a field or method to the link model.
	 * 
	 * @param cpClassIndex The index into the constant pool where the containing class is defined
	 * @param cpNameAndTypeIndex The name and signature of the referenced field
	 * @param index The index into the constant pool of class identified by classId
	 * @param constantPool The constant pool to extract the textual reference from
	 * @param classId The id identifying the class
	 */
	private void addMethodOrFieldReference(int cpClassIndex, int cpNameAndTypeIndex, int index,
			Constant[] constantPool, int classId) {
		ConstantClass cpClass = (ConstantClass) constantPool[cpClassIndex];
		ConstantNameAndType cpNameAndType = (ConstantNameAndType) constantPool[cpNameAndTypeIndex];
		String className = getName(constantPool, cpClass.getNameIndex());

		ConstantUtf8 cpMethodName = (ConstantUtf8) constantPool[cpNameAndType.getNameIndex()];
		String memberName = cpMethodName.getBytes();

		ConstantUtf8 cpSignature = (ConstantUtf8) constantPool[cpNameAndType.getSignatureIndex()];
		String memberSignature = cpSignature.getBytes();

		aLinkModel.createMemberReference(className, new Signature(memberName, memberSignature),
				classId, index);
	}

	// ----------------------------------------------//

	/**
	 * This method looks up a name Constant from the constant pool
	 * 
	 * @param constantPool The pool to lookup in
	 * @param nameIndex The index into the constant pool
	 * @return The resolved name
	 */
	private String getName(Constant[] constantPool, int nameIndex) {
		ConstantUtf8 utfName = (ConstantUtf8) constantPool[nameIndex];
		return utfName.getBytes();
	}

	public static void main(String[] args) {
		// Example:
		// java -cp ~/workspace/thinj/bin:$CLASSPATH -Dmycp=bin:/tools/bcel/5.2/bcel-5.2.jar \\
		// thinj.ClassReader thinj/regression/AllTests thinj/regression/gc/GC \\
		// org/apache/bcel/classfile/ClassParser

		String usage = "usage: java -Dmycp=<class path> -Dout=<file> NewLinker <main class>";
		if (args.length != 1) {
			System.err.println(usage);
			System.exit(1);
		}
		String mainClass = args[0];

		// Example:
		// -Dmycp=/home/hammer/workspace/thinjrt/bin/thinjrt.jar:/home/hammer/workspace/thinj/bin:/tools/j2sdk/1.6.0_20/jre/lib/rt.jar
		// Mandatory; contains class path to search in when resolving class names into class files
		String classPath = System.getProperty("mycp");
		if (classPath == null) {
			System.err.println(usage);
			System.exit(1);
		}

		// Example:
		// -Dout=/home/hammer/workspace/thinjvm/davsdu
		// Mandatory; defines the name of the auto generated .c - file and .h file. The .c - file
		// contains
		// the program, and the .h -file contains macros to be used when native c- code needs to
		// resolve
		// classes, methods and fields in the java code (to be used when calling from native c code
		// to java)
		String outFile = System.getProperty("out");
		if (outFile == null) {
			System.err.println(usage);
			System.exit(1);
		}

		// Example:
		// -Ddependencies=/home/hammer/workspace/thinj/src/thinj/regression/regression.dep
		// Optional; lists the java methods and fields that are mandatory as seen from native c
		// code.
		// If e.g. a java - method is referenced from native code only; this file will provide means
		// for ensuring inclusion of the referenced item in the resulting suite (otherwise the
		// native
		// code won't link)
		// The format of each line is :
		// <class name>#<member name>#<signature>
		// - example:
		// thinj.regression.nativetest.ReverseNativeInstanceTest#bar#()V
		LinkedList<String> requiredReferences = new LinkedList<String>();
		String dependsFile = System.getProperty("dependencies");
		if (dependsFile != null && dependsFile.length() > 0) {
			try {
				BufferedReader bis = new BufferedReader(new FileReader(dependsFile));
				String line;
				do {
					line = bis.readLine();
					if (line != null) {
						// Strip comments:
						line = line.replaceAll("#.*$", "").replaceAll("^[ \t]*", "");
						if (line.length() > 0) {
							requiredReferences.add(line);
						}
					}
				} while (line != null);
			} catch (FileNotFoundException e) {
				System.err.println("failed to open dependency file: " + dependsFile);
				System.exit(1);
			} catch (IOException e) {
				System.err.println("Error while reading dependency file: " + dependsFile);
				e.printStackTrace();
				System.exit(1);
			}
		}
		// else: Ignore

		// requiredReferences.add("java/lang.Thread runFromNative ()V");
		// requiredReferences.add("java.lang.Thread getNextReadyThread ()Ljava/lang/Thread;");
		try {
			new NewLinker(classPath, outFile, requiredReferences.toArray(new String[0]),
// @formatter:off
					new String[] { 
						ArrayIndexOutOfBoundsException.class.getName(),
						ArithmeticException.class.getName(),
						Class.class.getName(), 
						ClassCastException.class.getName(),
						NegativeArraySizeException.class.getName(),
						NullPointerException.class.getName(), 
						OutOfMemoryError.class.getName(),
//						IllegalArgumentException.class.getName(),
//						IllegalThreadStateException.class.getName(),
						String.class.getName(), 
						Thread.class.getName(), 
				    },
					new String[] { 
				        "java.lang.ArithmeticException            <init>         (Ljava/lang/String;)V",
				        "java.lang.ArrayIndexOutOfBoundsException <init>         (I)V",
				        "java.lang.Class                          aClassId       I",
				        "java.lang.Class                          aAllClasses    [Ljava/lang/Class;",
				        "java.lang.ClassCastException             <init>         ()V",				        
//				        "java.lang.IllegalArgumentException       <init>         (Ljava/lang/String;)V",
//				        "java.lang.IllegalThreadStateException    <init>         (Ljava/lang/String;)V",
				        "java.lang.NullPointerException           <init>         ()V",
				        "java.lang.OutOfMemoryError               <init>         ()V",
				        "java.lang.OutOfMemoryError               getInstance    ()Ljava/lang/OutOfMemoryError;",
				        "java.lang.NegativeArraySizeException     <init>         ()V",
				        "java.lang.String                         value          [C",

				        "java.lang.Thread                         aAllThreads    Ljava/lang/Thread;",
						"java.lang.Thread                         aContext       [B",
				        "java.lang.Thread                         aCurrentThread Ljava/lang/Thread;",
				        "java.lang.Thread                         aNextThread    Ljava/lang/Thread;",
						"java.lang.Thread                         aStack         [B",
				        "java.lang.Thread                         aState         I",
				        "java.lang.Thread                         runFromNative  ()V",
//				        "java.lang.Throwable                      aCause         Ljava/lang/String;",
//				        "java.lang.Throwable                      aStackTrace    [I",				        
			        }, 
					// @formatter:on
					mainClass);
		} catch (Exception e) {
			System.err.println("Failed linking for " + args[0]);
			e.printStackTrace();
		}
		System.out.println("Done.");
	}

	/**
	 * This method makes an error - exit from thinj
	 * 
	 * @param message The message to display
	 * @param exitValue The exit - value
	 */
	public static void exit(String message, int exitValue) {
		System.err.println(message);
		new Exception().printStackTrace(System.err);
		System.exit(exitValue);

	}
}
