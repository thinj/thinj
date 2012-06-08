package thinj.linkmodel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import thinj.NewLinker;
import thinj.instructions.AbstractInstruction;
import thinj.instructions.InstructionHandler;

public class LinkModel {
	// Singleton instance:
	private static LinkModel aInstance;

	// All class members:
	private final LinkedList<MethodOrField> aMembers;

	// Collections of all references:
	private final LinkedList<MemberReference> aMemberReferences;

	// Collection of integer constant references:
	private final LinkedList<ConstantReference<Integer>> aIntegerConstantReferences;

	// Collection of float constant references:
	private final LinkedList<ConstantReference<Float>> aFloatConstantReferences;

	// Collection of double constant references:
	private final LinkedList<ConstantReference<Double>> aDoubleConstantReferences;

	// Collection of String constant references:
	private final LinkedList<ConstantReference<String>> aStringConstantReferences;

	// Collection of long constant references:
	private final LinkedList<ConstantReference<Long>> aLongConstantReferences;

	// Collection of class references:
	private final LinkedList<ClassReference> aClassReferences;

	// Collection of class - id mappings for all classes:
	private final TreeMap<String, ClassInSuite> aClasses;

	// Collection of all signatures - to be used when building inheritance tables. The signatures
	// are not related to specific classes, this means that e.g. any <init>#()V has the
	// same signature no matter the containing class:
	private final HashMap<Signature, Integer> aSignatureMap;

	// Id generator for instance methods:
	private int aInstanceMethodIdGenerator = 0;

	// This maps from un-optimized member reference to optimized member reference. Is allocated and
	// populated in the optimize - method:
	private TreeMap<MemberReference, MemberReference> aMemberReferenceTranslationMap;

	private LinkModel() {
		aMembers = new LinkedList<MethodOrField>();
		aMemberReferences = new LinkedList<MemberReference>();
		aClassReferences = new LinkedList<ClassReference>();
		aIntegerConstantReferences = new LinkedList<ConstantReference<Integer>>();
		aFloatConstantReferences = new LinkedList<ConstantReference<Float>>();
		aDoubleConstantReferences = new LinkedList<ConstantReference<Double>>();
		aStringConstantReferences = new LinkedList<ConstantReference<String>>();
		aLongConstantReferences = new LinkedList<ConstantReference<Long>>();
		aClasses = new TreeMap<String, ClassInSuite>();
		aSignatureMap = new HashMap<Signature, Integer>();
	}

	/**
	 * @return The one and only instance
	 */
	public static LinkModel getInstance() {
		if (aInstance == null) {
			aInstance = new LinkModel();
		}
		return aInstance;
	}

	/**
	 * This method creates an entry in the model containing a method.
	 * 
	 * @param className The name of the containing class
	 * @param name The name of the method
	 * @param descriptor The descriptor of the method
	 * @param code The method code
	 * @param numberOfLocalVariables The number local variables including arguments
	 * @param numberOfArguments The number of method arguments
	 * @param isStatic true, if the method is static, false otherwise
	 * @return The created model
	 */
	public MethodInClass createMethodInClass(String className, String name, String descriptor,
			byte[] code, int numberOfLocalVariables, int numberOfArguments, boolean isStatic) {
		MethodInClass mic = new MethodInClass(new Member(className, name, descriptor), code,
				numberOfLocalVariables, numberOfArguments, isStatic);
		aMembers.add(mic);

		addSignature(mic.getMember().getSignature());

		return mic;
	}

	/**
	 * This method creates an entry in the model containing an abstract method.
	 * 
	 * @param className The name of the containing class
	 * @param name The name of the method
	 * @param descriptor The descriptor of the method
	 * @param numberOfArguments The number of method arguments
	 * @return The created method
	 */
	public MethodInClass createAbstractMethodInClass(String className, String name,
			String descriptor, int numberOfArguments) {
		MethodInClass mic = new MethodInClass(new Member(className, name, descriptor),
				numberOfArguments);
		aMembers.add(mic);

		addSignature(mic.getMember().getSignature());

		return mic;
	}

	/**
	 * This method creates an entry in the model containing a native method.
	 * 
	 * @param className The name of the containing class
	 * @param name The name of the method
	 * @param descriptor The descriptor of the method
	 * @param isStatic true, if the method is static, false otherwise
	 * @param argTypes The type of the method arguments
	 * @param returnType The return type of the method
	 */
	public void createNativeMethodInClass(String className, String name, String descriptor,
			boolean isStatic, String[] argTypes, String returnType) {
		MethodInClass mic = new MethodInClass(new Member(className, name, descriptor), argTypes,
				isStatic, returnType);
		aMembers.add(mic);

		addSignature(mic.getMember().getSignature());
	}

	/**
	 * This method registers a signature (with no class) to the collection of signatures
	 * 
	 * @param signature The signature to register
	 */
	private void addSignature(Signature signature) {
		// Build inheritance tables:
		if (aSignatureMap.get(signature) == null) {
			aSignatureMap.put(signature, aInstanceMethodIdGenerator++);
		}
		// else: Already there; avoid holes in the ids.
	}

	/**
	 * This method creates an entry in the model containing a field.
	 * 
	 * @param className The name of the containing class
	 * @param name The name of the field
	 * @param descriptor The descriptor of the field
	 * @param isStatic true, if the field is static, false otherwise
	 * @return The created entry
	 */
	public FieldInClass createFieldInClass(String className, String name, String descriptor,
			int size, boolean isStatic) {
		FieldInClass fic = new FieldInClass(new Member(className, name, descriptor), size, isStatic);
		aMembers.add(fic);

		addSignature(fic.getMember().getSignature());

		return fic;
	}

	/**
	 * This method creates an array class within the collection of classes in suite
	 * 
	 * @param classType The type of the class to add
	 * @return The created entry
	 */
	public ArrayClassInSuite createArrayClassInSuite(ClassTypeEnum classType) {
		ArrayClassInSuite ac = new ArrayClassInSuite(classType);
		if (aClasses.get(ac.getClassName()) == null) {
			aClasses.put(ac.getClassName(), ac);
		}

		return ac;
	}

	/**
	 * This method creates an object array class within the collection of classes in suite
	 * 
	 * @param classType The type of the class to add
	 * @return The created entry
	 */
	public ClassInSuite createObjectArrayClassInSuite(String elementClassName, int classId) {
		ClassInSuite cis = new ClassInSuite("[L" + elementClassName, classId,
				Object.class.getName(), ClassTypeEnum.ReferenceArray);
		aClasses.put(cis.getClassName(), cis);
		return cis;
	}

	/**
	 * This method creates an entry in the model containing a class-in-suite.
	 * 
	 * @param className The name of the containing class
	 * @param classId The id of the containing class
	 * @param superClassName The name of the super class - or null, if the class added is
	 *            'java.lang.Object'
	 * @param classType The type of the class to add
	 * @return The created entry
	 */
	public ClassInSuite createClassInSuite(String className, int classId, String superClassName,
			ClassTypeEnum classType) {
		ClassInSuite cis = new ClassInSuite(className, classId, superClassName, classType);
		// Note! cis.getClassName() might be != from className
		aClasses.put(cis.getClassName(), cis);

		return cis;
	}

	/**
	 * This method creates a reference to a member in the model.
	 * 
	 * @param referencedClassName The name of the class referenced by this reference
	 * @param memberName The name of the referenced member
	 * @param referencingClassId The class id of the referencing class
	 * @param constantPoolIndex The index into to constant pool of the referencing class
	 * @return The created reference
	 */
	public MemberReference createMemberReference(String referencedClassName, Signature signature,
			int referencingClassId, int constantPoolIndex) {
		MemberReference ref = new MemberReference(referencedClassName, signature,
				referencingClassId, constantPoolIndex);

		addSignature(signature);

		aMemberReferences.add(ref);
		return ref;
	}

	/**
	 * This method creates a reference to a double constant in the model
	 * 
	 * @param classId The class id of the referencing class
	 * @param constantPoolIndex The index into to constant pool of the referencing class
	 * @param value The value of the constant
	 */
	public void createDoubleConstantReference(int classId, int constantPoolIndex, double value) {
		ConstantReference<Double> ref = new ConstantReference<Double>(classId, constantPoolIndex,
				value);

		aDoubleConstantReferences.add(ref);
	}

	/**
	 * This method creates a reference to a float constant in the model
	 * 
	 * @param classId The class id of the referencing class
	 * @param constantPoolIndex The index into to constant pool of the referencing class
	 * @param value The value of the constant
	 */
	public void createFloatConstantReference(int classId, int constantPoolIndex, float value) {
		ConstantReference<Float> ref = new ConstantReference<Float>(classId, constantPoolIndex,
				value);

		aFloatConstantReferences.add(ref);
	}

	/**
	 * This method creates a reference to an integer constant in the model
	 * 
	 * @param classId The class id of the referencing class
	 * @param constantPoolIndex The index into to constant pool of the referencing class
	 * @param value The value of the constant
	 */
	public void createIntegerConstantReference(int classId, int constantPoolIndex, int value) {
		ConstantReference<Integer> ref = new ConstantReference<Integer>(classId, constantPoolIndex,
				value);

		aIntegerConstantReferences.add(ref);
	}

	/**
	 * This method creates a reference to a long constant in the model
	 * 
	 * @param classId The class id of the referencing class
	 * @param constantPoolIndex The index into to constant pool of the referencing class
	 * @param value The value of the constant
	 * @return The created reference
	 */
	public void createLongConstantReference(int classId, int constantPoolIndex, long value) {
		ConstantReference<Long> ref = new ConstantReference<Long>(classId, constantPoolIndex, value);

		aLongConstantReferences.add(ref);
	}

	/**
	 * This method creates a reference to a string constant in the model
	 * 
	 * @param classId The class id of the referencing class
	 * @param constantPoolIndex The index into to constant pool of the referencing class
	 * @param value The value of the constant
	 */
	public void createStringConstantReference(int classId, int constantPoolIndex, String value) {
		ConstantReference<String> ref = new ConstantReference<String>(classId, constantPoolIndex,
				value);

		aStringConstantReferences.add(ref);
	}

	/**
	 * This method creates a reference to an class and add it to the model
	 * 
	 * @param classId The class id of the referencing class
	 * @param constantPoolIndex The index into to constant pool of the referencing class
	 * @param className The name of the referenced class
	 * @return The created reference
	 */
	public ClassReference createClassReference(int classId, int constantPoolIndex, String className) {
		ClassReference ref = new ClassReference(className, classId, constantPoolIndex);
		aClassReferences.add(ref);
		// System.err.println("ref: " + classId + "; " + className + "; ");
		// new Exception().printStackTrace();
		return ref;
	}

	/**
	 * This method returns all array classes in the model
	 * 
	 * @return All array classes in the model
	 */
	public ArrayClassInSuite[] getAllArrayClasses() {
		LinkedList<ArrayClassInSuite> l = new LinkedList<ArrayClassInSuite>();
		for (ClassInSuite cis : aClasses.values()) {
			if (cis instanceof ArrayClassInSuite) {
				l.add((ArrayClassInSuite) cis);
			}
		}

		return l.toArray(new ArrayClassInSuite[l.size()]);
	}

	// /**
	// * This method looks up an array class based on type.
	// *
	// * @param type The type identifying the array class, e.g. T_CHAR
	// * @return The resolved class or null, of no match
	// */
	// public ArrayClassInSuite getArrayClassByType(int type) {
	// ArrayClassInSuite res = null;
	// ArrayClassInSuite[] allArrayClasses = getAllArrayClasses();
	// for (ArrayClassInSuite acis : allArrayClasses) {
	// if (acis.getType() == type) {
	// res = acis;
	// break;
	// }
	// }
	//
	// return res;
	// }

	/**
	 * This method returns all constant references of double type
	 * 
	 * @return all constant references of type double
	 */
	public List<ConstantReference<Double>> getAllDoubleConstantReferences() {
		return aDoubleConstantReferences;
	}

	/**
	 * This method returns all constant references of float type
	 * 
	 * @return all constant references of type float
	 */
	public List<ConstantReference<Float>> getAllFloatConstantReferences() {
		return aFloatConstantReferences;
	}

	/**
	 * This method returns all constant references of Integer type
	 * 
	 * @return all constant references of type Integer
	 */
	public List<ConstantReference<Integer>> getAllIntegerConstantReferences() {
		return aIntegerConstantReferences;
	}

	/**
	 * This method returns all constant references of String type
	 * 
	 * @return all constant references of type String
	 */
	public List<ConstantReference<String>> getAllStringConstantReferences() {
		return aStringConstantReferences;
	}

	/**
	 * This method returns all constant references of long type
	 * 
	 * @return all constant references of type long
	 */
	public List<ConstantReference<Long>> getAllLongConstantReferences() {
		return aLongConstantReferences;
	}

	/**
	 * This method returns all classes added to model. The classes will be sorted, so that classId=0
	 * is at index=0 and so on.
	 * 
	 * @return All classes added to model
	 */
	public ClassInSuite[] getAllClasses() {
		TreeMap<Integer, ClassInSuite> idMap = new TreeMap<Integer, ClassInSuite>();
		for (ClassInSuite cis : aClasses.values()) {
			idMap.put(cis.getClassId(), cis);
		}
		return idMap.values().toArray(new ClassInSuite[idMap.size()]);
	}

	/**
	 * This method returns all class references
	 * 
	 * @return All class references
	 */
	public ClassReference[] getAllClassReferences() {
		return aClassReferences.toArray(new ClassReference[aClassReferences.size()]);
	}

	/**
	 * This method returns all references to methods or fields
	 * 
	 * @return MemberReference[] all references to methods or fields
	 */
	public MemberReference[] getAllMethodOrFieldReferences() {
		LinkedList<MemberReference> l = new LinkedList<MemberReference>();
		for (MemberReference ref : aMemberReferences) {
			l.add(ref);
		}
		return l.toArray(new MemberReference[l.size()]);
	}

	/**
	 * This method returns all unique references to methods. The return value will be sorted in
	 * ascending order of (referencing classId, constant pool index)
	 * 
	 * @return MemberReference[] all unique references to methods
	 */
	public MemberReference[] getAllMethodReferences() {
		TreeSet<MemberReference> ts = new TreeSet<MemberReference>(
				new Comparator<MemberReference>() {
					@Override
					public int compare(MemberReference o1, MemberReference o2) {
						int diff = o1.getClassId() - o2.getClassId();
						if (diff == 0) {
							diff = o1.getConstantPoolIndex() - o2.getConstantPoolIndex();
						}
						return diff;
					}
				});

		for (MemberReference ref : aMemberReferences) {
			// Do only add method references:
			if (ref.getSignature().isMethod()) {
				ts.add(ref);
			}
		}
		return ts.toArray(new MemberReference[ts.size()]);
	}

	/**
	 * This method returns all unique references to methods referenced by the class identified by
	 * 'referencingClassId'. The return value will be sorted in ascending order of (constant pool
	 * index)
	 * 
	 * @param referencingClassId
	 * @return MemberReference[] all unique references to methods for class identified by
	 *         'referencingClassId'
	 */
	public MemberReference[] getAllMethodReferences(int referencingClassId) {
		TreeSet<MemberReference> ts = new TreeSet<MemberReference>(
				new Comparator<MemberReference>() {
					@Override
					public int compare(MemberReference o1, MemberReference o2) {
						int diff = o1.getClassId() - o2.getClassId();
						if (diff == 0) {
							diff = o1.getConstantPoolIndex() - o2.getConstantPoolIndex();
						}
						return diff;
					}
				});

		for (MemberReference ref : aMemberReferences) {
			// Do only add method references:
			if (ref.getSignature().isMethod() && ref.getClassId() == referencingClassId) {
				ts.add(ref);
			}
		}

		return ts.toArray(new MemberReference[ts.size()]);
	}

	/**
	 * This method returns all references to static or instance fields
	 * 
	 * @return MemberReference[] all references to static or instance fields
	 */
	public MemberReference[] getAllFieldReferences() {
		TreeSet<MemberReference> ts = new TreeSet<MemberReference>(
				new Comparator<MemberReference>() {
					@Override
					public int compare(MemberReference o1, MemberReference o2) {
						int diff = o1.getClassId() - o2.getClassId();
						if (diff == 0) {
							diff = o1.getConstantPoolIndex() - o2.getConstantPoolIndex();
						}
						return diff;
					}
				});
		for (MemberReference ref : aMemberReferences) {
			// Do only add selected field references:
			if (!ref.getSignature().isMethod()) {
				ts.add(ref);
			}
		}
		return ts.toArray(new MemberReference[ts.size()]);
	}

	/**
	 * This method returns all methods in classes.
	 * 
	 * @return all methods in classes.
	 */
	public MethodInClass[] getAllMethods() {
		LinkedList<MethodInClass> l = new LinkedList<MethodInClass>();

		for (MethodOrField mof : aMembers) {
			if (mof instanceof MethodInClass) {
				l.add((MethodInClass) mof);
			}
		}
		return l.toArray(new MethodInClass[l.size()]);
	}

	/**
	 * This method returns all methods implemented by the class identified by 'classId'
	 * 
	 * @param classId identifies the class
	 * @return All methods sorted by link id
	 */
	public MethodInClass[] getClassMethods(int classId) {
		TreeSet<MethodInClass> ts = new TreeSet<MethodInClass>(new Comparator<MethodInClass>() {
			@Override
			public int compare(MethodInClass o1, MethodInClass o2) {
				return o1.getLinkId() - o2.getLinkId();
			}
		});
		for (MethodOrField mof : aMembers) {
			if (mof instanceof MethodInClass) {
				MethodInClass mic = (MethodInClass) mof;
				int methodClassId = getClassIdByName(mic.getMember().getClassName());
				if (methodClassId == classId) {
					ts.add(mic);
				}
			}
		}
		return ts.toArray(new MethodInClass[ts.size()]);
	}

	/**
	 * This method returns the referenced member from a class. If not found in the indicated class,
	 * the search will continue up in the super class(es).
	 * 
	 * @param signature The signature of the member
	 * @param memberName The name of the member
	 * @param classId The id of the class wherein the member shall be looked up
	 * @return The referenced method or field or null, if no match
	 */
	public MethodOrField getMethodOrField(int classId, String memberName, String signature) {
		ClassInSuite cis = getClassById(classId);
		MethodOrField retval = getMethodOrFieldInClass(classId, memberName, signature);

		if (retval == null && classId != 0) {
			// If no hit, try in super class:
			// System.out.println(classId + "super: " + getSuperClassById(classId) + "; " +
			// memberName
			// + ";" + signature);
			retval = getMethodOrField(getSuperClassById(classId).getClassId(), memberName,
					signature);
		}

		if (retval == null && classId != 0) {
			// Try interfaces:
			String[] interfaceNames = cis.getImplementedInterfaces();
			for (int i = 0; i < interfaceNames.length && retval == null; i++) {
				retval = getMethodOrField(getClassIdByName(interfaceNames[i]), memberName,
						signature);
			}
		}

		return retval;
	}

	/**
	 * This method returns the referenced member from a class. If not found in the indicated class,
	 * null is returned.
	 * 
	 * @param signature The signature of the member
	 * @param memberName The name of the member
	 * @param classId The id of the class wherein the member shall be looked up
	 * 
	 * @return The referenced method or field or null, if no match
	 */
	public MethodOrField getMethodOrFieldInClass(int classId, String memberName, String signature) {
		MethodOrField retval = null;
		for (MethodOrField mof : aMembers) {
			if (memberName.equals(mof.getMember().getSignature().getName())
					&& getClassIdByName(mof.getMember().getClassName()) == classId
					&& signature.equals(mof.getMember().getSignature().getDescriptor())) {
				retval = mof;
				break;
			}
		}

		return retval;
	}

	/**
	 * This method returns all fields from all classes.
	 * 
	 * @return all fields in all classes.
	 */
	public FieldInClass[] getAllFields() {
		LinkedList<FieldInClass> l = new LinkedList<FieldInClass>();

		for (MethodOrField mof : aMembers) {
			if (mof instanceof FieldInClass) {
				FieldInClass fic = (FieldInClass) mof;
				l.add(fic);
			}
		}
		return l.toArray(new FieldInClass[l.size()]);
	}

	/**
	 * This method returns all instance fields from a single class.
	 * 
	 * @param classId The identification of the requested class
	 * @return all instance fields in a the specified class.
	 */
	public FieldInClass[] getInstanceFields(int classId) {
		LinkedList<FieldInClass> l = new LinkedList<FieldInClass>();

		for (MethodOrField mof : aMembers) {
			if (mof instanceof FieldInClass) {
				FieldInClass fic = (FieldInClass) mof;
				if (!fic.isStatic() && getClassIdByName(fic.getMember().getClassName()) == classId) {
					l.add(fic);
				}
			}
		}
		return l.toArray(new FieldInClass[l.size()]);
	}

	/**
	 * This method returns all methods from a single class.
	 * 
	 * @param classId The identification of the requested class
	 * @return all methods in a the specified class.
	 */
	public MethodInClass[] getMethods(int classId) {
		LinkedList<MethodInClass> l = new LinkedList<MethodInClass>();

		for (MethodOrField mof : aMembers) {
			if (mof instanceof MethodInClass) {
				MethodInClass mic = (MethodInClass) mof;
				if (getClassIdByName(mic.getMember().getClassName()) == classId) {
					l.add(mic);
				}
			}
		}
		return l.toArray(new MethodInClass[l.size()]);
	}

	/**
	 * This method finds the number of arguments corresponding to the given signature
	 * 
	 * @param signature
	 * @return The number of arguments corresponding to the given signature
	 */
	public int getArgumentCount(Signature signature) {
		MethodInClass matchMic = null;
		for (MethodOrField mof : aMembers) {
			if (mof instanceof MethodInClass) {
				MethodInClass mic = (MethodInClass) mof;
				if (mic.getMember().getSignature().equals(signature)) {
					matchMic = mic;
					break;
				}
			}
		}
		if (matchMic == null) {
			System.err.println("Internal error: Failed to find matching method for signature: "
					+ signature);
			new Exception().printStackTrace();
			System.exit(1);
		}

		return matchMic.getNumberOfArguments();
	}

	/**
	 * This method will link all items together
	 */
	public void link() {
		// The address of methods:
		int codeOffset = 0;

		// The address for static fields:
		int staticAddress = 0;

		// Index into native jump table:
		int nativeIndex = 0;

		// Set addresses of all code and all fields:
		for (MethodOrField mof : aMembers) {
			if (mof.isReferenced()) {
				int linkId = aSignatureMap.get(mof.getMember().getSignature());
				mof.setLinkId(linkId);
				if (mof instanceof MethodInClass) {
					MethodInClass mic = (MethodInClass) mof;
					mic.setCodeOffset(codeOffset);

					// Fix offsets in exception handlers:
					List<ExceptionHandler> ehl = mic.getExceptionHandlers();
					for (ExceptionHandler handler : ehl) {
						handler.setCodeOffset(codeOffset);
					}
					codeOffset += mic.getCode().length;
					if (mic.getType() == MethodInClass.Type.NativeMethod) {
						mic.setNativeIndex(nativeIndex++);
					}
				} else {
					// Field in class:
					FieldInClass fic = (FieldInClass) mof;
					if (fic.isStatic()) {
						fic.setAddress(staticAddress);
						staticAddress += fic.getSize();
					}
					// else: Address is calculated elsewhere
				}
			}

		}

		// Resolve all class inheritances:
		for (ClassInSuite cis : aClasses.values()) {
			if (cis.isReferenced()) {
				if (cis.getSuperClassName() != null) {
					ClassInSuite superClass = aClasses.get(cis.getSuperClassName());
					if (superClass == null) {
						System.err.println("Link error: Class " + cis.getSuperClassName()
								+ " not defined");
					}
				}
				// else: It's java.lang.Object!

				// Calculate size of class instances (excluding super classes):
				int instanceSize = 0;
				FieldInClass[] fica = getInstanceFields(cis.getClassId());
				for (FieldInClass fic : fica) {
					// Cannot yet deduce size of super classes, hence instance field //
					// addresses cannot be resolve yet
					instanceSize += fic.getSize();
				}
				cis.setInstanceSize(instanceSize);
			}
		}

		/*
		 * Instance ref: (classId, constantPoolIndex -> globalMethodId) Instance method: (classId,
		 * globalMethodId -> miscMethodInfo ('methodInClass'))
		 */
		for (MemberReference ref : aMemberReferences) {
			if (ref.isReferenced()) {
				// Only for instance methods, not static method nor instance fields:
				ref.setReferencedClassId(getClassIdByName(ref.getReferencedClassName()));
				int linkId = aSignatureMap.get(ref.getSignature());
				ref.setLinkId(linkId);
			}
		}

		// Resolve all class references:
		for (ClassReference ref : aClassReferences) {
			if (ref.isReferenced()) {
				ClassInSuite cis = aClasses.get(ref.getClassName());
				if (cis != null) {
					// Set target class id:
					ref.setTargetClassId(cis.getClassId());
				} else {
					System.out.println("Warning: Unresolved reference (is this an error?): "
							+ ref.getClassName() + "; cid=" + ref.getClassId() + "; cpIx = "
							+ ref.getConstantPoolIndex());
				}
			}
		}

		// Resolve addresses of all instance fields in all classes:
		for (ClassInSuite cis : aClasses.values()) {
			if (cis.isReferenced()) {
				// Calculate size of class instances (excluding super classes):
				int instanceOffset;
				if (cis.getSuperClassName() == null) {
					// cis is java.lang.Object
					instanceOffset = 0;
				} else {
					instanceOffset = aClasses.get(cis.getSuperClassName())
							.getInstanceSizeWithSuper(this);
				}
				FieldInClass[] fica = getInstanceFields(cis.getClassId());
				for (FieldInClass fic : fica) {
					fic.setAddress(instanceOffset);
					instanceOffset += fic.getSize();
				}
			}
		}
	}

	/**
	 * This method returns the class corresponding to the class name
	 * 
	 * @param className The name of the requested class
	 * @return The requested class or null, if no match
	 */
	public ClassInSuite getClassByName(String className) {
		return aClasses.get(className);
	}

	/**
	 * This method returns the class id for the class in the supplied member
	 * 
	 * @param className The name of the class for which the class id shall be resolved
	 * @return The class id for the class
	 */
	public int getClassIdByName(String className) {
		ClassInSuite cis = getClassByName(className);
		if (cis == null) {
			System.err.println("Internal error: Failed to look up class: " + className);
			for (String name : aClasses.keySet()) {
				System.err.println("names: " + name);
			}
			new Exception().printStackTrace();
			System.exit(1);
		}
		return cis.getClassId();
	}

	/**
	 * This method returns the class identified by the supplied id
	 * 
	 * @param classId
	 * @return The class identified by the supplied id
	 */
	public ClassInSuite getClassById(int classId) {
		ClassInSuite hit = null;
		for (ClassInSuite cis : aClasses.values()) {
			if (cis.getClassId() == classId) {
				hit = cis;
				break;
			}
		}
		if (hit == null) {
			System.err.println("Internal error: Failed to look up class: " + classId);
			System.exit(1);
		}

		return hit;
	}

	/**
	 * This method returns all classes that directly extends the class identified by 'classId'. Only
	 * one level of inheritance; so if 'A' extends 'B' and 'B' extends 'C', then
	 * getAllClassesExtending(C.classId) will only return (B).
	 * 
	 * @param superClassId Identifies the super class. If no match, this method will cause an exit
	 *            of this program.
	 * @return A list of sub classes to the class identified by 'classId'. An empty array is
	 *         returned if no sub classes are found.
	 */
	public List<ClassInSuite> getAllClassesExtending(int superClassId) {
		ClassInSuite superClass = getClassById(superClassId);

		LinkedList<ClassInSuite> l = new LinkedList<ClassInSuite>();
		for (ClassInSuite cis : aClasses.values()) {
			if (cis.isExtending(superClass)) {
				l.add(cis);
			}
		}

		return l;
	}

	/**
	 * This method returns all classes that directly implements the class (interface) identified by
	 * 'classId'.
	 * 
	 * @param interfaceClassId Identifies the interface class. If no match, this method will cause
	 *            an exit of this program.
	 * @return A list of sub classes to the class identified by 'classId'. An empty array is
	 *         returned if no sub classes are found.
	 */
	public List<ClassInSuite> getAllClassesImplementing(int interfaceClassId) {
		ClassInSuite superClass = getClassById(interfaceClassId);

		LinkedList<ClassInSuite> l = new LinkedList<ClassInSuite>();
		for (ClassInSuite cis : aClasses.values()) {
			if (cis.isImplementing(superClass)) {
				l.add(cis);
			}
		}

		return l;
	}

	/**
	 * This method returns the super class for a class identified by 'classId'.
	 * 
	 * @param classId Identifies the class for which the super class is to be returned. 0 is
	 *            illegal, since this denotes java.lang.Object, which has no super class.
	 * @return The super class for a class identified by 'classId'
	 */
	public ClassInSuite getSuperClassById(int classId) {
		String superClassName = getClassById(classId).getSuperClassName();
		return getClassByName(superClassName);
	}

	/**
	 * This method looks up the name of the referenced class.
	 * 
	 * @param referencingClassId The id of the class containing the reference
	 * @param constantPoolIndex The index into the constant pool of the referencing class
	 * @return Always the matching class name is returned. If unable to find a match, and error is
	 *         raised and System.exit() is called.
	 */
	public String getClassNameByReference(int referencingClassId, int constantPoolIndex) {
		String className = null;
		for (ClassReference cRef : aClassReferences) {
			if (cRef.getClassId() == referencingClassId
					&& cRef.getConstantPoolIndex() == constantPoolIndex) {
				className = cRef.getClassName();
				break;
			}
		}

		if (className == null) {
			System.err.println("Unable to resolve reference: " + referencingClassId + "."
					+ constantPoolIndex);
			System.exit(1);
		}
		return className;
	}

	/**
	 * This method finds the reference to a class member
	 * 
	 * @param referencingClassId The id of the class referencing the member
	 * @param constantPoolIndex The constant pool index of the referencing class
	 * @return The found reference. If unable to find, the program terminates with error.
	 */
	public MemberReference getMemberReference(int referencingClassId, int constantPoolIndex) {
		MemberReference res = null;
		for (MemberReference mref : aMemberReferences) {
			if (mref.getClassId() == referencingClassId
					&& mref.getConstantPoolIndex() == constantPoolIndex) {
				res = mref;
			}
		}

		if (res == null) {
			System.err.println("Unable to resolve member reference: " + referencingClassId + "."
					+ constantPoolIndex);
			System.exit(1);
		}

		return res;
	}

	/**
	 * This method finds the reference to a class
	 * 
	 * @param referencingClassId The id of the class referencing the target class
	 * @param constantPoolIndex The constant pool index of the referencing class
	 * @return The found reference. If unable to find, the program terminates with error.
	 */
	public ClassReference getClassReference(int referencingClassId, int constantPoolIndex) {
		ClassReference ref = null;
		for (ClassReference cref : aClassReferences) {
			if (cref.getClassId() == referencingClassId
					&& cref.getConstantPoolIndex() == constantPoolIndex) {
				ref = cref;
				break;
			}
		}

		if (ref == null) {
			System.err.println("Unable to resolve class reference: " + referencingClassId + "."
					+ constantPoolIndex);
			new Exception().printStackTrace();
			System.exit(1);
		}

		return ref;
	}

	/**
	 * This method cleans up the model and optimises the different references etc.
	 */
	public void optimize() {
		removeUnreferencedItems();
		renumberAllClassIds();

		aMemberReferenceTranslationMap = new TreeMap<MemberReference, MemberReference>(
				new Comparator<MemberReference>() {
					@Override
					public int compare(MemberReference o1, MemberReference o2) {
						// Identified by:
						// ref.getClassId()
						// ref.getConstantPoolIndex()
						int diff = o1.getClassId() - o2.getClassId();
						if (diff == 0) {
							diff = o1.getConstantPoolIndex() - o2.getConstantPoolIndex();
						}
						return diff;
					}
				});
		// For each class find set of unique method references:
		for (int referencingClassid = 0; referencingClassid < getTotalClassCount(); referencingClassid++) {
			// Set of unique references (well, use a map so we can 'get()' them again):
			TreeMap<MemberReference, MemberReference> uniqueCollection = new TreeMap<MemberReference, MemberReference>(
					new Comparator<MemberReference>() {
						@Override
						public int compare(MemberReference o1, MemberReference o2) {
							// 'Unique' references are identified by:
							// getClassId(), getReferencedClassId(), getLinkId()
							// If diff is zero, the two member references refer to the same member:
							int diff = o1.getReferencedClassId() - o2.getReferencedClassId();
							if (diff == 0) {
								diff = o1.getLinkId() - o2.getLinkId();
							}
							return diff;
						}
					});

			int newConstantpoolIndex = 0;
			MemberReference[] allRefs = getAllMethodReferences(referencingClassid);

			// Build collection of unique references:
			for (MemberReference ref : allRefs) {
				MemberReference uniqueRef = uniqueCollection.get(ref);
				if (uniqueRef == null) {
					uniqueRef = new MemberReference(ref.getReferencedClassName(),
							ref.getSignature(), referencingClassid, newConstantpoolIndex);
					uniqueRef.setLinkId(ref.getLinkId());
					uniqueRef.setReferencedClassId(ref.getReferencedClassId());
					uniqueCollection.put(uniqueRef, uniqueRef);
					newConstantpoolIndex++;
				}
				aMemberReferenceTranslationMap.put(ref, uniqueRef);
			}
		}

		// for (MemberReference mr : aMemberReferenceTranslationMap.keySet()) {
		// MemberReference un = aMemberReferenceTranslationMap.get(mr);
		// System.out.println("" + mr.getClassId() + "." + mr.getConstantPoolIndex() + ":"
		// + mr.getReferencedClassId() + "." + mr.getLinkId() + "->" + un.getClassId()
		// + "." + un.getConstantPoolIndex() + ":" + un.getReferencedClassId() + "."
		// + un.getLinkId());
		// }

		for (MethodInClass mic : getAllMethods()) {
			if (mic.isReferenced() && mic.getType() != MethodInClass.Type.AbstractMethod) {
				renumberMethodReferences(mic);
			}
		}
	}

	/**
	 * This method renumbers method references in code for a single method.
	 * 
	 * @param mic The method for which the code shall be renumbered
	 */
	private void renumberMethodReferences(final MethodInClass mic) {
		byte[] ba = mic.getCode();
		if (ba.length > 0) {
			ClassInSuite cis = getClassByName(mic.getMember().getClassName());
			ba = AbstractInstruction.renumberMemberReferences(cis.getClassId(), ba);
			mic.setCode(ba);
		}
	}

	/**
	 * This method returns the optimised set of member references that is used by the class
	 * identified by 'referencingClassId'
	 * 
	 * @param referencingClassId The class using the references
	 * @return A sorted array of member references. At position 'n' the MemberReference with
	 *         constant pool index 'n' is situated.
	 */
	public MemberReference[] getOptimizedMethodReferences(int referencingClassId) {
		TreeSet<MemberReference> set = new TreeSet<MemberReference>(
				new Comparator<MemberReference>() {
					@Override
					public int compare(MemberReference o1, MemberReference o2) {
						int diff = o1.getConstantPoolIndex() - o2.getConstantPoolIndex();
						return diff;
					}
				});
		for (MemberReference mr : aMemberReferenceTranslationMap.values()) {
			if (mr.getClassId() == referencingClassId) {
				set.add(mr);
			}
		}

		return set.toArray(new MemberReference[set.size()]);
	}

	/**
	 * This method renumbers all class ids so they starts with 0 and continues without any 'wholes'
	 */
	private void renumberAllClassIds() {
		// Map from old to new ClassId:
		HashMap<Integer, Integer> classIdMap = new HashMap<Integer, Integer>();
		// java.lang.Object is always 0:
		classIdMap.put(0, 0);
		int classId = 1;
		for (ClassInSuite cis : aClasses.values()) {
			if (cis.getClassId() != 0) {
				classIdMap.put(cis.getClassId(), classId);
				cis.renumberClassId(classId);
				classId++;
			}
			// else: java.lang.Object is in map
		}

		// -------------------------------------------------
		// aClassReferences
		// -------------------------------------------------
		for (ClassReference cref : aClassReferences) {
			cref.renumberClassId(classIdMap.get(cref.getClassId()));
			cref.setTargetClassId(classIdMap.get(cref.getTargetClassId()));
		}

		// -------------------------------------------------
		// aXXXConstantReferences
		// -------------------------------------------------
		for (ConstantReference<?> cref : aLongConstantReferences) {
			cref.renumberClassId(classIdMap.get(cref.getClassId()));
		}
		for (ConstantReference<?> cref : aStringConstantReferences) {
			cref.renumberClassId(classIdMap.get(cref.getClassId()));
		}
		for (ConstantReference<?> cref : aDoubleConstantReferences) {
			cref.renumberClassId(classIdMap.get(cref.getClassId()));
		}
		for (ConstantReference<?> cref : aFloatConstantReferences) {
			cref.renumberClassId(classIdMap.get(cref.getClassId()));
		}
		for (ConstantReference<?> cref : aIntegerConstantReferences) {
			cref.renumberClassId(classIdMap.get(cref.getClassId()));
		}

		// -------------------------------------------------
		// aClassReferences
		// -------------------------------------------------
		for (MemberReference mref : aMemberReferences) {
			mref.renumberClassId(classIdMap.get(mref.getClassId()));
			mref.setReferencedClassId(classIdMap.get(mref.getReferencedClassId()));
		}
	}

	/**
	 * This method removes all unreferenced items.
	 */
	private void removeUnreferencedItems() {
		// -------------------------------------------------
		// aMembers
		// -------------------------------------------------
		for (Iterator<MethodOrField> it = aMembers.iterator(); it.hasNext();) {
			MethodOrField next = it.next();
			if (!next.isReferenced()) {
				it.remove();
			}
		}

		// -------------------------------------------------
		// aMemberReferences
		// -------------------------------------------------
		for (Iterator<MemberReference> it = aMemberReferences.iterator(); it.hasNext();) {
			MemberReference next = it.next();
			if (!next.isReferenced()) {
				it.remove();
			}
		}

		// -------------------------------------------------
		// aXXXConstantReferences
		// -------------------------------------------------
		removeUnreferencedConstantReferences(aIntegerConstantReferences);
		removeUnreferencedConstantReferences(aFloatConstantReferences);
		removeUnreferencedConstantReferences(aDoubleConstantReferences);
		removeUnreferencedConstantReferences(aStringConstantReferences);
		removeUnreferencedConstantReferences(aLongConstantReferences);

		// -------------------------------------------------
		// aClasses
		// -------------------------------------------------
		for (Iterator<String> it = aClasses.keySet().iterator(); it.hasNext();) {
			ClassInSuite next = aClasses.get(it.next());
			if (!next.isReferenced()) {
				it.remove();
			}
		}

		// -------------------------------------------------
		// aClassReferences
		// -------------------------------------------------
		for (Iterator<ClassReference> it = aClassReferences.iterator(); it.hasNext();) {
			ClassReference next = it.next();
			if (!next.isReferenced()) {
				it.remove();
			}
		}
	}

	/**
	 * This method removes all unreferenced constant references from the supplie list
	 * 
	 * @param constantReferences The list to clean up
	 */
	private <T> void removeUnreferencedConstantReferences(
			LinkedList<ConstantReference<T>> constantReferences) {
		for (Iterator<ConstantReference<T>> it = constantReferences.iterator(); it.hasNext();) {
			ConstantReference<T> next = it.next();
			if (!next.isReferenced()) {
				it.remove();
			}
		}
	}

	/**
	 * @return the number of referenced classes (only valid after {@link #removeUnreferencedItems()}
	 *         has been called
	 */
	public int getTotalClassCount() {
		return aClasses.size();
	}

	/**
	 * This method translates a non-optimised reference to an optimised ditto
	 * 
	 * @param referencingClassId
	 * @param constantPoolIndex
	 * @return
	 */
	public MemberReference getOptimizedReference(int referencingClassId, int constantPoolIndex) {
		MemberReference retval = null;
		for (MemberReference mr : aMemberReferenceTranslationMap.keySet()) {
			if (mr.getClassId() == referencingClassId
					&& mr.getConstantPoolIndex() == constantPoolIndex) {
				retval = aMemberReferenceTranslationMap.get(mr);
			}
		}

		if (retval == null) {
			NewLinker.exit("Cannot translate reference: " + referencingClassId + "."
					+ constantPoolIndex, 1);
		}

		return retval;
	}
}
