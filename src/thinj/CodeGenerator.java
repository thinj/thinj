package thinj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import thinj.NativeTypeRepository.NativeTypeHandler;
import thinj.instructions.AbstractInstruction;
import thinj.instructions.InstructionHandler;
import thinj.linkmodel.ClassInSuite;
import thinj.linkmodel.ClassReference;
import thinj.linkmodel.ClassTypeEnum;
import thinj.linkmodel.ConstantReference;
import thinj.linkmodel.ExceptionHandler;
import thinj.linkmodel.FieldInClass;
import thinj.linkmodel.LinkModel;
import thinj.linkmodel.Member;
import thinj.linkmodel.MemberReference;
import thinj.linkmodel.MethodInClass;

/**
 * This class is responsible for generation of C - code for the suite
 * 
 * @author hammer
 * 
 */
public class CodeGenerator {
	private final LinkModel aLinkModel;
	private PrintStream aHeader;
	private PrintStream aSuite;
	private PrintStream aTrace;
	private HashMap<Integer, ConstantPoolEntry> aConstantPools;

	public CodeGenerator(LinkModel linkModel) {
		aLinkModel = linkModel;
		aConstantPools = new HashMap<Integer, ConstantPoolEntry>();
	}

	/**
	 * This method generates all C-code based on link model and arguments
	 * 
	 * @param mainClassName The name of the main class
	 * @param outputBaseName
	 * @param startAddress The java byte code start address
	 */
	public void generateCode(String mainClassName, String outputBaseName, int startAddress) {
		String headerFileName = outputBaseName + ".h";
		try {
			aHeader = new PrintStream(headerFileName);
			aSuite = new PrintStream(outputBaseName + ".c");
			aTrace = new PrintStream(outputBaseName + ".trace");
		} catch (FileNotFoundException e) {
			System.err.println("Failed to open map file: ");
			e.printStackTrace();
			System.exit(1);
		}
		sectionHeader(aSuite, "This file is autogenerated; any modifications might be lost");
		aSuite.println();
		aSuite.println("#include \"constantpool.h\"");
		aSuite.println("#include \"operandstack.h\"");
		aSuite.println("#include \"jni.h\"");
		aSuite.println("#include \"instructions.h\"");
		aSuite.println("#include \"" + new File(headerFileName).getName() + "\"");

		// Find used instructions:
		referenceInstructions();

		sectionHeader(aSuite, "", "Declarations");
		AbstractInstruction.generateDeclarations(aSuite);

		sectionHeader(aSuite, "", "Forward Declarations");
		AbstractInstruction.generateForwardDeclarations(aSuite);

		sectionHeader(aSuite, "", "Instruction Table");
		AbstractInstruction.generateInstructionTable(aSuite);

		// Dump class info to be used during 'new' - execution:
		dumpClassInstanceInfo();

		// Dump all class references:
		dumpClassReferences();

		// Dump buildin link table:
		dumpBuildinLinkTable();

		// // Dump all array info:
		// dumpArrayInfo();

		// - mangler at frasortere de referencer, som ikke er i spil

		// Dump all fields:
		dumpFields();

		// Dump all non-native method info:
		dumpMethods();

		// Dump all native method info:
		dumpNativeMethodInfo();

		// Dump all constant references:
		dumpContantReferences();

		// // Dump all class references:
		// dumpClassReferences();

		// Dump all code:
		dumpAllCode();

		// Dump constant pools:
		dumpConstantPools();

		// Miscellaneous:
		sectionHeader(aSuite, "Startup information");
		aSuite.println("const u2 startClassIndex = "
				+ aLinkModel.getClassIdByName(ClassInSuite.getGlobalName(mainClassName)) + ";");
		aSuite.println("const codeIndex startAddress = " + String.format("0x%04x", startAddress)
				+ ";");

		aHeader.close();
		aSuite.close();
		aTrace.close();
	}

	/**
	 * This method dumps the array of constant pool
	 */
	private void dumpConstantPools() {
		sectionHeader(aSuite, "Constant Pools");
		aSuite.println("const constantPool const allConstantPools[] = {");
		for (int classId = 0; classId < aLinkModel.getTotalClassCount(); classId++) {
			ConstantPoolEntry cp = aConstantPools.get(classId);
			// Method referecens:
			String s = cp.getMethodReferencesLength() == 0 ? "NULL" : cp.getMethodReferences();
			aSuite.print("    {" + s + ", " + cp.getMethodReferencesLength());

			// Method instances:
			s = cp.getNumberOfMethods() == 0 ? "NULL" : cp.getMethodsInClass();
			aSuite.print(", " + s + ", " + cp.getNumberOfMethods());

			aSuite.println("}, // " + aLinkModel.getClassById(classId).getClassName());
		}
		aSuite.println("};");
	}

	private void dumpBuildinLinkTable() {
		sectionHeader(aSuite, "Buildin Dependencies Link Table, Class Ids");

		BuildinDependency[] spa = BuildinDependency.getReferencedDependencies();
		int count = 0;
		aSuite.println("const BuildinClassDependency_s const AllBuildinClassDependencies[] = {");
		for (BuildinDependency dep : spa) {
			if (!dep.isMemberReference()) {
				aSuite.println("    {" + dep.getCCodeReference() + ", " + dep.getMacro() + "}, ");
				count++;
			}
		}
		aSuite.println("};");

		aSuite.println("const u2 numberOfAllBuildinClassDependencies = " + count + ";");

		sectionHeader(aSuite, "Buildin Dependencies Link Table, Member References (Link Id)");
		count = 0;
		aSuite.println("const BuildinMemberDependency_s const AllBuildinMemberDependencies[] = {");
		for (BuildinDependency dep : spa) {
			if (dep.isMemberReference()) {
				aSuite.println("    {" + dep.getCCodeReference() + ", " + dep.getMacro() + "}, ");
				count++;
			}
		}
		aSuite.println("};");

		aSuite.println("const u2 numberOfAllBuildinMemberDependencies = " + count + ";");

	}

	/**
	 * This method finds all referenced instructions and mark these. Then not use instructions can
	 * be omitted in the suite
	 */
	private void referenceInstructions() {
		MethodInClass[] methods = aLinkModel.getAllMethods();
		for (MethodInClass mic : methods) {
			if (mic.isReferenced()) {
				mic.getCode();
				AbstractInstruction.disassemble(0, mic.getCode(), new InstructionHandler() {
					@Override
					public void handle(int address, AbstractInstruction instruction) {
						instruction.referenced();
					}
				});
			}
		}
	}

	/**
	 * This method appends a comments section to the output
	 * 
	 * @param s The comment to add
	 */
	private void sectionHeader(PrintStream ps, String s) {
		sectionHeaderLn(ps, "", s);
	}

	/**
	 * This method appends a comments section to the output. each line in the section is prefixed by
	 * 'prefix'. A line feed is appended
	 * 
	 * @param s The comment to add
	 * @param prefix The prefix to each line
	 */
	private void sectionHeaderLn(PrintStream ps, String prefix, String s) {
		sectionHeader(ps, prefix, s);
		aSuite.println();
	}

	/**
	 * This method appends a comments section to the output. each line in the section is prefixed by
	 * 'prefix'
	 * 
	 * @param s The comment to add
	 * @param prefix The prefix to each line
	 */
	private void sectionHeader(PrintStream ps, String prefix, String s) {
		ps.println();
		ps.println(prefix
				+ "//------------------------------------------------------------------------------");
		ps.println(prefix + "// " + s);
		ps.println(prefix
				+ "//------------------------------------------------------------------------------");
	}

	// /**
	// * This method dumps information about the arrays used in the generated suite
	// */
	// private void dumpArrayInfo() {
	// sectionHeader(aSuite, "Arrays in suite");
	// ArrayClassInSuite[] aa = aLinkModel.getAllArrayClasses();
	// int count = 0;
	// println("const arrayInfoDef arrayInfo[] = {");
	// for (ArrayClassInSuite acis : aa) {
	// if (acis.isReferenced()) {
	// count++;
	// println("    {" + acis.getClassId() + ", " + acis.getElementSize() + ", "
	// + acis.getType() + "},");
	// }
	// }
	// println("};");
	// println("const u2 numberofAllArrayInfo = " + count + ";");
	//
	// println();
	// }

	/**
	 * This method generates code for information about class instances.
	 */
	private void dumpClassInstanceInfo() {
		sectionHeader(aSuite, "Class Instance Information");
		sectionHeader(aHeader, "Class Instance Information");

		ClassInSuite[] classes = aLinkModel.getAllClasses();

		aSuite.println("const classInstanceInfoDef const allClassInstanceInfo[] = {");
		ClassInSuite javaLangString = null;
		ClassInSuite javaLangClass = null;
		int count = 0;
		LinkedList<String> implementedInterfaces = new LinkedList<String>();
		int interfaceCount = 0;
		for (int tableIndex = 0; tableIndex < classes.length; tableIndex++) {
			ClassInSuite cis = classes[tableIndex];
			if (cis.isReferenced()) {
				if (cis.getClassName().equals("java/lang/String")) {
					javaLangString = cis;
				} else if (cis.getClassName().equals("java/lang/Class")) {
					javaLangClass = cis;
				}
				count++;
				int instanceSize = cis.getInstanceSizeWithSuper(aLinkModel);
				int superClassId;
				if (cis.getSuperClassName() != null) {
					superClassId = aLinkModel.getClassByName(cis.getSuperClassName()).getClassId();
				} else {
					superClassId = cis.getClassId();
				}
				String elementSize = cis.getClassType().getSize();
				if (elementSize == null) {
					elementSize = "0";
				}
				int elementClassId = 0;
				if (cis.getClassType() == ClassTypeEnum.ReferenceArray) {
					String elementClassName;
					if (cis.getClassName().startsWith("[L")) {
						// Strip leading "[L"
						elementClassName = cis.getClassName().substring(2);
					} else {
						// Strip leading "["
						elementClassName = cis.getClassName().substring(1);
					}
					ClassInSuite elementClass = aLinkModel.getClassByName(elementClassName);
					elementClassId = elementClass.getClassId();
				}

				aHeader.println("#define " + generateClassIdMacro(cis.getClassName()) + " "
						+ cis.getClassId());

				aSuite.println(String.format("    {%4d,%4d,%4d,%4d,%4d, %-16s%4d, %-16s}, // %s",
						cis.getClassId(), superClassId, instanceSize, interfaceCount,
						cis.getImplementedInterfaces().length,
						String.format("%s,", cis.getClassType().getClassTypeName()),
						elementClassId, elementSize, cis.getClassName()));

				// ClassInstanceInfoDef def = new ClassInstanceInfoDef(cis.getClassId(),
				// superClassId,
				// instanceSize, interfaceCount, cis.getImplementedInterfaces().length,
				// cis.getClassType(), elementClassId, elementSize, cis.getClassName());
				// Tables.add(def);

				// ImplementedInterfaces implInter = new ImplementedInterfaces(cis.getClassName());
				StringBuilder sb = new StringBuilder();
				if (cis.getImplementedInterfaces().length > 0) {
					for (String s : cis.getImplementedInterfaces()) {
						int interfaceId = aLinkModel.getClassIdByName(s);
						sb.append(interfaceId).append(", ");
						// implInter.classIds.add(interfaceId);
					}
					implementedInterfaces.add("// " + cis.getClassName());
					implementedInterfaces.add(sb.toString());
				}
				interfaceCount += cis.getImplementedInterfaces().length;
				// Tables.add(implInter);
			}
		}
		aSuite.println("};");
		aSuite.println("const u2 numberOfAllClassInstanceInfo = " + count + ";");

		sectionHeader(aSuite, "Special java classes");
		if (javaLangString != null) {
			aSuite.println("const u2 javaLangStringClassIndex = " + javaLangString.getClassId()
					+ ";");
		} else {
			aSuite.println("const u2 javaLangStringClassIndex = 0; // Dummy value");
		}

		if (javaLangClass != null) {
			aSuite.println("const u2 javaLangClassClassIndex = " + javaLangClass.getClassId() + ";");
		} else {
			aSuite.println("const u2 javaLangClassClassIndex = 0; // Dummy value");
		}

		sectionHeader(aSuite, "Implemented interfaces");
		// The interfaces implemented by the misc. classes:
		aSuite.println("const u2 const implementedInterfaces[] = {");
		for (String s : implementedInterfaces) {
			aSuite.println("    " + s);
		}
		aSuite.println("};");

	}

	/**
	 * This method dumps all constant references
	 */
	private void dumpContantReferences() {
		dumpContantReferences("Integer", aLinkModel.getAllIntegerConstantReferences());
		dumpContantReferences("Float", aLinkModel.getAllFloatConstantReferences());
		dumpContantReferences("Double", aLinkModel.getAllDoubleConstantReferences());
		dumpStringContantReferences();
	}

	/**
	 * This method dumps all integer constant references
	 * 
	 * @param <T>
	 */
	private <T> void dumpContantReferences(String type,
			List<ConstantReference<T>> constantReferences) {
		sectionHeader(aSuite, type + " Constant References");

		aSuite.println("const u2 numberOfAll" + type + "ConstantReferences = "
				+ constantReferences.size() + ";");

		String lowerCaseType = type.substring(0, 1).toLowerCase() + type.substring(1);
		aSuite.println("const " + lowerCaseType + "ConstantReference const all" + type
				+ "ConstantReferences[] = {");
		for (ConstantReference<T> ref : constantReferences) {
			aSuite.println("    {" + ref.getClassId() + ", " + ref.getConstantPoolIndex() + ", "
					+ ref.getValue() + "}, ");
		}
		aSuite.println("};");
	}

	/**
	 * This method dumps all used class references
	 */
	private void dumpClassReferences() {
		sectionHeader(aSuite, "Class References");

		// Sort class references:
		ClassReference[] classReferences = aLinkModel.getAllClassReferences();
		TreeSet<ClassReference> ts = new TreeSet<ClassReference>(new Comparator<ClassReference>() {
			@Override
			public int compare(ClassReference o1, ClassReference o2) {
				int diff = o1.getClassId() - o2.getClassId();
				if (diff == 0) {
					diff = o1.getConstantPoolIndex() - o2.getConstantPoolIndex();
				}

				return diff;
			}
		});
		for (ClassReference cref : classReferences) {
			ts.add(cref);
		}

		int count = 0;
		aSuite.println("const classReference const allClassReferences[] = {");
		int prevClassId = -1;
		for (ClassReference ref : ts) {
			if (ref.isReferenced()) {
				count++;
				if (prevClassId != ref.getClassId()) {
					aSuite.println("    // Class Id: " + ref.getClassId());
					prevClassId = ref.getClassId();
				}
				aSuite.println("    {" + ref.getClassId() + ", " + ref.getConstantPoolIndex()
						+ ", " + ref.getTargetClassId() + "}, // \"" + ref.getClassName() + "\"");
			}
		}

		aSuite.println("};");

		aSuite.println("const u2 numberOfAllClassReferences = " + count + ";");
	}

	// /**
	// * This method dumps all integer constant references
	// */
	// private void dumpIntegerContantReferences() {
	// sectionHeader(aSuite, "Integer Constant References");
	//
	// List<ConstantReference<Integer>> integerConstantReferences = aLinkModel
	// .getAllIntegerConstantReferences();
	//
	// println("const u2 numberOfAllIntegerConstantReferences = "
	// + integerConstantReferences.size() + ";");
	//
	// println("integerConstantReference allIntegerConstantReferences[] = {");
	// for (ConstantReference<Integer> ref : integerConstantReferences) {
	// println("    {" + ref.getClassId() + ", " + ref.getConstantPoolIndex() + ", "
	// + ref.getValue() + "}, ");
	// }
	// println("};");
	// }

	/**
	 * This method dumps all string constant references
	 */
	private void dumpStringContantReferences() {
		sectionHeader(aSuite, "String Constant References");

		List<ConstantReference<String>> references = aLinkModel.getAllStringConstantReferences();

		aSuite.println("const u2 numberOfAllStringConstantReferences = " + references.size() + ";");

		aSuite.println("const stringConstantReference const allStringConstantReferences[] = {");
		for (ConstantReference<String> ref : references) {
			aSuite.println("    {" + ref.getClassId() + ", " + ref.getConstantPoolIndex() + ", \""
					+ ref.getValue() + "\"}, ");
		}
		aSuite.println("};");
	}

	/**
	 * This method generates code for field references and field properties
	 */
	private void dumpFields() {
		sectionHeader(aSuite, "Field references");

		MemberReference[] fieldRefs = aLinkModel.getAllFieldReferences();
		aSuite.println("const u2 numberOfAllFieldReferences = " + fieldRefs.length + ";");
		aSuite.println("const memberReference const allFieldReferences[] = {");
		int prevClassId = -1;
		for (MemberReference ref : fieldRefs) {
			if (prevClassId != ref.getClassId()) {
				aSuite.println("    // Class Id: " + ref.getClassId());
				prevClassId = ref.getClassId();
			}
			aSuite.println("    {" + ref.getClassId() + ", " + ref.getConstantPoolIndex() + ", "
					+ ref.getReferencedClassId() + ", " + ref.getLinkId() + "}, // "
					+ ref.getReferencedClassName() + "->" + ref.getSignature());
		}
		aSuite.println("};");

		sectionHeader(aSuite, "Field properties");

		int count = 0;
		FieldInClass[] fields = aLinkModel.getAllFields();
		int staticSize = 0;
		aSuite.println("const fieldInClass const allFields[] = {");
		// prevClassId = -1;
		for (FieldInClass fic : fields) {
			if (fic.isReferenced()) {
				count++;
				int classId = aLinkModel.getClassIdByName(fic.getMember().getClassName());
				// if (prevClassId != classId) {
				// aSuite.println("    // Class Id: " + classId);
				// prevClassId = classId;
				// }
				aSuite.println("   {" + classId + ", " + fic.getLinkId() + ", " + fic.getAddress()
						+ ", " + fic.getSize() + "}, // " + fic.getLinkId() + "-"
						+ fic.getMember().format());
				if (fic.isStatic()) {
					staticSize += fic.getSize();
				}
			}
		}
		aSuite.println("};");
		aSuite.println("const u2 numberOfAllFields = " + count + ";");

		sectionHeader(aSuite, "Static Memory");
		aSuite.println("const int staticMemorySize = " + staticSize + ";");
		aSuite.println("stackable staticMemory[" + staticSize + "];");
	}

	/**
	 * This method generate code for method references and method properties
	 */
	private void dumpMethods() {
		sectionHeader(aSuite, "Method References");

		for (int referencingClassId = 0; referencingClassId < aLinkModel.getTotalClassCount(); referencingClassId++) {
			// Optimized refs:
			MemberReference[] methodRefs = aLinkModel.getOptimizedMethodReferences(referencingClassId);
			int prevClassId = -1;
			ConstantPoolEntry cp = getConstantPoolEntry(referencingClassId);
			cp.setMethodReferencesLength(methodRefs.length);
			if (methodRefs.length > 0) {
				aSuite.println("const memberReference const " + cp.getMethodReferences()
						+ "[] = {");
				for (MemberReference ref : methodRefs) {
					// if (!ref.isReferenced()) {
					// NewLinker.exit("unref'ed MemberReference: " + ref, 1);
					// }
					int argCount = aLinkModel.getArgumentCount(ref.getSignature());
					if (prevClassId != ref.getClassId()) {
						prevClassId = ref.getClassId();
					}
					aSuite.println("    {" + ref.getClassId() + ", " + ref.getConstantPoolIndex()
							+ ", " + ref.getReferencedClassId() + ", " + ref.getLinkId() + ", "
							+ argCount + "}, // " + ref.getReferencedClassName() + "#"
							+ ref.getSignature().format());
				}
				aSuite.println("};");
				aSuite.println();
			}
		}

		sectionHeader(aSuite, "Method Attributes");
		sectionHeader(aHeader, "Method Attributes");
		for (int classId = 0; classId < aLinkModel.getTotalClassCount(); classId++) {
			ConstantPoolEntry cp = aConstantPools.get(classId);

			// Dump all methods for this class Id:
			MethodInClass[] methods = aLinkModel.getClassMethods(classId);
			cp.setNumberOfMethods(methods.length);
			if (methods.length > 0) {
				aSuite.println("const methodInClass const " + cp.getMethodsInClass() + "[] = {");
				for (MethodInClass mic : methods) {
					if (!mic.isReferenced()) {
						NewLinker.exit("Unreferenced method: " + mic, 1);
					}

					int nativeIndex = (mic.getType() == MethodInClass.Type.NativeMethod ? 1 + mic
							.getNativeIndex() : 0);
					// Dump method info:

					generateMemberMapping(mic);

					aSuite.println("    {" + classId + ", " + mic.getLinkId() + ", "
							+ String.format("0x%04x", mic.getCodeOffset()) + ", "
							+ mic.getCode().length + ", " + mic.getNumberOfLocalVariables() + ", "
							+ mic.getNumberOfArguments() + ", " + nativeIndex + "}, // "
							+ mic.getLinkId() + "-" + mic.getMember().format());
				}
				aSuite.println("};");
				aSuite.println();
			}
		}

		MethodInClass[] methods = aLinkModel.getAllMethods();
		dumpExceptionHandlers(methods);

		dumpTraceInfo(methods);
	}

	/**
	 * This method returns the ConstantPoolEntry for the class identified by 'classId'
	 * 
	 * @param classId Identifies the entry to return
	 * @return A new instance is returned if no entry identified by 'classId' yet has been requested
	 */
	private ConstantPoolEntry getConstantPoolEntry(int classId) {
		ConstantPoolEntry cp = aConstantPools.get(classId);
		if (cp == null) {
			cp = new ConstantPoolEntry(classId);
			aConstantPools.put(classId, cp);
		}
		return cp;
	}

	/**
	 * This method dumps stack trace info in a separate file
	 * 
	 * @param methods All methods
	 */
	private void dumpTraceInfo(MethodInClass[] methods) {
		// Extract stack trace info (line number table):
		aTrace.println(":: PC SourceLine Method");
		for (MethodInClass mic : methods) {
			if (mic.isReferenced() && mic.getType() != MethodInClass.Type.AbstractMethod) {
				for (MethodInClass.LineNumber ln : mic.getLineNumberTable()) {
					aTrace.println(ln.getStartPC() + "  " + ln.getLineNumber() + "  "
							+ mic.getMember().format());
				}
			}
		}
	}

	/**
	 * This method generates '#define's thus enabling references to class methods from native code
	 * 
	 * @param mic The method to generate references for
	 */
	private void generateMemberMapping(MethodInClass mic) {
		aHeader.println("#define " + generateMemberLinkIdMacro(mic.getMember()) + " "
				+ mic.getLinkId());
	}

	/**
	 * This method generates the C-style LINK_ID macro for a member
	 * 
	 * @param member The member to generate a LINK_ID macro for
	 * @return The generated macro
	 */
	public static String generateMemberLinkIdMacro(Member member) {
		return "LINK_ID_" + convertSpecialChars(member.format());
	}

	/**
	 * This method generates the C-style CLASS_ID macro for a class
	 * 
	 * @param member The class to generate a LINK_ID macro for
	 * @return The generated macro
	 */
	public static String generateClassIdMacro(String referencedClassName) {
		return "CLASS_ID_" + convertSpecialChars(referencedClassName);
	}

	private static String convertSpecialChars(String s) {
		return s.replaceAll("[#/<>()\\[;]", "_");
	}

	private void dumpExceptionHandlers(MethodInClass[] methods) {
		int count = 0;
		sectionHeader(aSuite, "Exception Handlers");
		aSuite.println("const exceptionHandler const allExceptionHandersInAllClasses[] = {");
		for (MethodInClass mic : methods) {
			if (mic.isReferenced() /* && mic.getType() != MethodInClass.Type.AbstractMethod */) {
				List<ExceptionHandler> ehl = mic.getExceptionHandlers();
				int classId = aLinkModel.getClassIdByName(mic.getMember().getClassName());
				// Dump handler info:
				for (ExceptionHandler handler : ehl) {
					// System.err.println("handler: " + ehl + "; containing: "
					// + aLinkModel.getClassIdByName(mic.getMember().getClassName()) +
					// "; name: " + mic.getMember().getClassName());
					count++;
					String exClassName;
					int exClassId;
					if (handler.getExceptionConstantPoolIndex() != 0) {
						exClassName = aLinkModel.getClassNameByReference(classId,
								handler.getExceptionConstantPoolIndex());
						exClassId = aLinkModel.getClassIdByName(exClassName);
					} else {
						exClassName = "<finally>";
						exClassId = 0;
					}

					aSuite.println("    {"
							+ aLinkModel.getClassIdByName(mic.getMember().getClassName()) + ", "
							+ exClassId + ", " + String.format("0x%04x", handler.getStartPC())
							+ ", " + String.format("0x%04x", handler.getEndPC()) + ", "
							+ String.format("0x%04x", handler.getExceptionHandlerPC()) + "}, // "
							+ exClassName + " @0x" + String.format("%04x", mic.getCodeOffset()));
				}
			}
		}
		aSuite.println("};");
		aSuite.println("const u2 numberOfAllExceptionHandersInAllClasses = " + count + ";");
	}

	private void dumpNativeMethodInfo() {
		sectionHeader(aSuite, "Native Method Encapsulations");
		int count = 0;
		MethodInClass[] methods = aLinkModel.getAllMethods();
		LinkedList<String> nativeTableEntries = new LinkedList<String>();
		for (MethodInClass mic : methods) {
			if (mic.isReferenced() && mic.getType() == MethodInClass.Type.NativeMethod) {
				count++;
				int nativeIndex = 1 + mic.getNativeIndex();
				String className = mic.getMember().getClassName();
				String methodName = mic.getMember().getSignature().getName();
				String nativeMethodName = "Java_" + className.replace('/', '_') + "_" + methodName;
				String[] argTypes = mic.getArgTypes();
				String returnType = mic.getReturnType();

				// JNIEXPORT jint JNICALL Java_thinj_rt_Object_hashCode(JNIEnv *, jobject);
				aSuite.println("// " + nativeIndex + ": " + nativeMethodName);
				NativeTypeHandler returntypeHandler = NativeTypeRepository.getHandler(returnType);

				StringBuilder prototypeArgs = new StringBuilder("JNIEnv *");
				if (mic.isStatic()) {
					prototypeArgs.append(", jclass");
				} else {
					prototypeArgs.append(", jobject");
				}
				String argList = "";
				StringBuilder assignments = new StringBuilder();
				for (int i = 0; i < argTypes.length; i++) {
					String s = argTypes[i];
					// function argument list:
					// String argName = "p" + (argTypes.length - 1 - i);
					String argName = "p" + i;
					argList = ", " + argName + argList;
					// argList.append(", ");
					// argList.append(argName);
					NativeTypeHandler handler = NativeTypeRepository.getHandler(s);
					// Invocation values:
					// assignments.append(handler.getParameterAssignment(argName)).append("\n");
					// prototype types:
					prototypeArgs.append(", ").append(handler.getParameterType());
				}

				for (int i = argTypes.length - 1; i >= 0; i--) {
					// for (int i = 0; i < argTypes.length; i++) {
					String s = argTypes[i];
					String argName = "p" + (argTypes.length - 1 - i);
					NativeTypeHandler handler = NativeTypeRepository.getHandler(s);
					// Invocation values:
					assignments.append(handler.getParameterAssignment(argName)).append("\n");
				}

				StringBuilder forward = new StringBuilder("JNIEXPORT "
						+ returntypeHandler.getParameterType() + " JNICALL " + nativeMethodName);
				forward.append("(").append(prototypeArgs).append(");");
				aSuite.println(forward);

				nativeTableEntries.add("native" + nativeIndex);
				aSuite.println("void native" + nativeIndex + "(void) {");
				aSuite.println(assignments);
				if (mic.isStatic()) {
					// TODO According to
					// http://java.sun.com/developer/onlineTraining/Programming/JDCBook/jniexamp.html#gen
					// this argument shall be of type 'jclass', not 'jobject'. Fix it, when we know
					// a little more about jclass...
					aSuite.println("    jclass thisOrClass = NULL;");
				} else {
					aSuite.println("    jobject thisOrClass = operandStackPopObjectRef();");
				}
				String returnVarName = "retval";
				aSuite.println("    " + returntypeHandler.getAssignReturnTypeCode(returnVarName)
						+ nativeMethodName + "(&context, thisOrClass" + argList + ");");
				// Push return value to stack:
				aSuite.println("    " + returntypeHandler.getReturnTypePushCode(returnVarName));
				aSuite.println("}");
			}
		}

		// Then the table to look up in:
		sectionHeader(aSuite, "Native Method Jump Table");
		aSuite.println("const nativeJumpTableEntry const nativeJumpTable[] = {");
		for (String tableEntry : nativeTableEntries) {
			aSuite.println("    " + tableEntry + ",");
		}
		aSuite.println("};");
		aSuite.println("const u2 nativeJumpTableSize = " + nativeTableEntries.size() + ";");

	}

	/**
	 * This method dumps all code into a single u1 array.
	 */
	private void dumpAllCode() {
		sectionHeader(aSuite, "Byte Code");

		aSuite.println("const u1 code[] = {");
		int codeSize = 0;
		for (MethodInClass mic : aLinkModel.getAllMethods()) {
			if (mic.isReferenced() && mic.getType() != MethodInClass.Type.AbstractMethod) {
				dumpCode(mic);
				codeSize += mic.getCode().length;
			}
		}
		aSuite.println("};");
		aSuite.println("const u2 codeSize = " + codeSize + ";");
	}

	/**
	 * This method dumps code for a single method.
	 * 
	 * @param mic The method for which the code shall be dumped
	 */
	private void dumpCode(final MethodInClass mic) {
		byte[] ba = mic.getCode();
		if (ba.length > 0) {
			int addressOffset = mic.getCodeOffset();

			ClassInSuite cis = aLinkModel.getClassByName(mic.getMember().getClassName());
			sectionHeader(aSuite, "    ", "(" + cis.getClassId() + ") " + mic.getMember().format());

			AbstractInstruction.disassemble(addressOffset, ba, new InstructionHandler() {
				@Override
				public void handle(int address, AbstractInstruction instruction) {
					for (MethodInClass.LineNumber ln : mic.getLineNumberTable()) {
						if (address == ln.getStartPC()) {
							aSuite.println();
							aSuite.println("    // " + mic.getMember().format() + ":"
									+ ln.getLineNumber());
						}
					}

					aSuite.print("    /* " + String.format("%04x", address) + " */      ");
					instruction.generateCode(aSuite);
				}
			});
		}
	}

	// private void println() {
	// println("");
	// }
	//
	// private void println(Object s) {
	// System.out.println(s == null ? "null" : s.toString());
	// }
	//
	// private void print(String s) {
	// System.out.print(s == null ? "null" : s.toString());
	// }
}
