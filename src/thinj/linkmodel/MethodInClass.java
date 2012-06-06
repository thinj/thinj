package thinj.linkmodel;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import thinj.IntInABox;
import thinj.BuildinDependency;

/**
 * This class represents a method in a class
 * 
 * @author hammer
 * 
 */
public class MethodInClass extends MethodOrField {
	private final int aNumberofLocalVariables;
	private final int aNumberOfArguments;
	private final byte[] aCode;
	private final Type aType;
	private int aCodeOffset;
	private final LinkedList<LineNumber> aLineNumbers;
	private final LinkedList<MemberReference> aReferences;
	private int aNativeIndex;
	private final String[] aArgTypes;
	private final String aReturnType;
	private final HashSet<ClassReference> aClassDepencies;
	private final HashSet<ClassTypeEnum> aSimpleArrayDependencies;
	private final LinkedList<ExceptionHandler> aExceptionHandlers;
	private final LinkedList<BuildinDependency> aBuildinDependencies;
	private LinkedList<ConstantReference<?>> aConstantReferences;

	/**
	 * Constructor.
	 * 
	 * @param member The identification of the method
	 * @param code The method code
	 * @param numberOfLocalVariables The number of local variables including arguments
	 * @param numberOfArguments The number of method arguments
	 * @param isStatic true, if the method is static, false otherwise
	 */
	MethodInClass(Member member, byte[] code, int numberOfLocalVariables, int numberOfArguments,
			boolean isStatic) {
		this(
				member,
				isStatic,
				member.getSignature().getName().equals("<clinit>") ? Type.ClassInitCode : member
						.getSignature().getName().equals("<init>") ? Type.Constructor : Type.Method,
				numberOfArguments, numberOfLocalVariables, code, null, null);
	}

	/**
	 * Constructor for an abstract method
	 * 
	 * @param member The identification of the method
	 * @param numberOfArguments The number of method arguments
	 * @param isStatic true, if the method is static, false otherwise
	 */
	MethodInClass(Member member, int numberOfArguments) {
		this(member, false, Type.AbstractMethod, numberOfArguments, 0, new byte[0], null, null);
	}

	/**
	 * Constructor for a native method
	 * 
	 * @param member The identification of the method
	 * @param numberOfArguments The number of method arguments
	 * @param isStatic true, if the method is static, false otherwise
	 * @param returnType The return type of the method
	 */
	MethodInClass(Member member, String[] argTypes, boolean isStatic, String returnType) {
		this(member, isStatic, Type.NativeMethod, argTypes.length + (isStatic ? 0 : 1), 0,
				new byte[0], argTypes, returnType);
	}

	private MethodInClass(Member member, boolean isStatic, Type type, int numberOfArguments,
			int numberOfLocalVariables, byte[] code, String[] argTypes, String returnType) {
		super(member, isStatic);
		aType = type;
		aNumberOfArguments = numberOfArguments;
		aNumberofLocalVariables = numberOfLocalVariables;
		aCode = code;
		aReturnType = returnType;
		aLineNumbers = new LinkedList<LineNumber>();
		aReferences = new LinkedList<MemberReference>();
		aNativeIndex = 0;
		aArgTypes = argTypes;
		aClassDepencies = new HashSet<ClassReference>();
		aSimpleArrayDependencies = new HashSet<ClassTypeEnum>();
		aExceptionHandlers = new LinkedList<ExceptionHandler>();
		aBuildinDependencies = new LinkedList<BuildinDependency>();
		aConstantReferences = new LinkedList<ConstantReference<?>>();
	}

	/**
	 * This method returns the type of the method.
	 * 
	 * @return The type of the method.
	 */
	public Type getType() {
		return aType;
	}

	public byte[] getCode() {
		return aCode;
	}

	public int getNumberOfArguments() {
		return aNumberOfArguments;
	}

	public int getNumberOfLocalVariables() {
		return aNumberofLocalVariables;
	}

	/**
	 * This method sets the address of the code. The line number table will be relocated as well.
	 * 
	 * @param codeOffset The address of the code
	 */
	public void setCodeOffset(int codeOffset) {
		aCodeOffset = codeOffset;

		// Relocate line number table:
		for (LineNumber ln : aLineNumbers) {
			ln.setStartPC(ln.getStartPC() + aCodeOffset);
		}
	}

	/**
	 * This method gets the address of the code
	 * 
	 * @return The address of the code. If not set, 0 is returned
	 */
	public int getCodeOffset() {
		return aCodeOffset;
	}

	/**
	 * This method appends line number info for the method code
	 * 
	 * @param lineNumber The source line number
	 * @param startPC The lowest possible code address corresponding to the source line
	 */
	public void appendLineNumber(int lineNumber, int startPC) {
		aLineNumbers.addLast(new LineNumber(lineNumber, startPC));
	}

	/**
	 * This method returns all line number info
	 * 
	 * @return A non-null array containing line number info
	 */
	public LineNumber[] getLineNumberTable() {
		return aLineNumbers.toArray(new LineNumber[aLineNumbers.size()]);
	}

	/**
	 * This class pairs a source file line number with a program counter value
	 * 
	 * @author hammer
	 * 
	 */
	public static class LineNumber {
		private final int aLineNumber;
		private int aStartPC;

		/**
		 * Constructor
		 * 
		 * @param lineNumber The source line number
		 * @param startPC The lowest possible code address corresponding to the source line
		 */
		public LineNumber(int lineNumber, int startPC) {
			aLineNumber = lineNumber;
			aStartPC = startPC;
		}

		public int getLineNumber() {
			return aLineNumber;
		}

		public int getStartPC() {
			return aStartPC;
		}

		public void setStartPC(int startPC) {
			aStartPC = startPC;
		}
	}

	/**
	 * This method appends a MemberReference to this method. This signals that this method
	 * references some other members. It is legal to append the same reference more than once; this
	 * will be suppressed when the references are processed.
	 * 
	 * @param ref The reference to add.
	 */
	public void addReference(MemberReference ref) {
		aReferences.add(ref);
	}

	/**
	 * This method returns an array of all other members that this method references. Note that the
	 * same reference might occur multiple times.
	 * 
	 * @return An array of MemberRerences
	 */
	public MemberReference[] getMemberReferences() {
		return aReferences.toArray(new MemberReference[aReferences.size()]);
	}

	/**
	 * This method registers the dependency of a ConstantReference
	 * 
	 * @param constantReference The ConstantReference that this method depends on
	 */
	public void addConstantReference(ConstantReference<?> constantReference) {
		aConstantReferences.add(constantReference);
	}

	/**
	 * This enumerates the different types of methods
	 * 
	 * @author hammer
	 * 
	 */
	public enum Type {
		// The <clinit> - code:
		ClassInitCode,
		// The <init> - code:
		Constructor,
		// An abstract method:
		AbstractMethod,
		// A native method:
		NativeMethod,
		// A normal method:
		Method
	}

	/**
	 * This method returns the native index, if this method instance is a native method.
	 * 
	 * @return The native index, if this method instance is a native method. For other methods than
	 *         Native, the return value is undefined.
	 */
	public int getNativeIndex() {
		return aNativeIndex;
	}

	/**
	 * This method sets the native index, if this method instance is a native method.
	 * 
	 * @param nativeIndex The native index, if this method instance is a native method. For other
	 *            methods than Native, this is ignored.
	 */
	public void setNativeIndex(int nativeIndex) {
		aNativeIndex = nativeIndex;
	}

	/**
	 * This method returns the types of the arguments, if these are known. native methods only.
	 * 
	 * @return The types of the arguments, if these are known. If not known, null is returned.
	 */
	public String[] getArgTypes() {
		return aArgTypes;
	}

	/**
	 * This method returns the return type of the method, if this are known. Native methods only.
	 * 
	 * @return The return type of the method, if this are known. If not known, null is returned.
	 */
	public String getReturnType() {
		return aReturnType;
	}

	/**
	 * This method adds a class name to the collection of other classes that this member depends on
	 * 
	 * @param classReference The reference to the class that this member depends on
	 */
	public void addClassDependency(ClassReference classReference) {
		aClassDepencies.add(classReference);
	}

	/**
	 * This method returns all class names that this member depends on
	 * 
	 * @return All class names that this member depends on
	 */
	public ClassReference[] getAllClassDependencies() {
		return aClassDepencies.toArray(new ClassReference[aClassDepencies.size()]);
	}

	/**
	 * This method adds a simple array type dependency to this method
	 * 
	 * @param type The simple array that this method depends on
	 */
	public void addSimpleArrayDependency(ClassTypeEnum type) {
		aSimpleArrayDependencies.add(type);
	}

	/**
	 * This method returns all the simple array types that this method depends on
	 * 
	 * @return All the simple array types that this method depends on
	 */
	public ClassTypeEnum[] getAllSimpleArrayDependencies() {
		return aSimpleArrayDependencies.toArray(new ClassTypeEnum[aSimpleArrayDependencies.size()]);
	}

	/**
	 * This method adds an ExceptionHandler to the collection of these.
	 * 
	 * @param handler The handler to add
	 */
	public void addExceptionHandler(ExceptionHandler handler) {
		aExceptionHandlers.add(handler);
	}

	/**
	 * This method returns the collection of Exception handlers
	 * 
	 * @return The collection of Exception handlers
	 */
	public List<ExceptionHandler> getExceptionHandlers() {
		return aExceptionHandlers;
	}

	/**
	 * This method registers a build in dependency
	 * 
	 * @param dependency The dependency
	 * @param linkModel Reference is created and registered in the link model
	 * @param referencingClassId The class referencing the dependency
	 * @param constantPoolLength Reference to constant pool length. Is incremented by one.
	 */
	public void addDependency(BuildinDependency dependency, LinkModel linkModel,
			int referencingClassId, IntInABox constantPoolLength) {
		aBuildinDependencies.add(dependency);
		ConstantPoolReference ref = dependency.createDependency(linkModel, referencingClassId,
				constantPoolLength);
		if (ref instanceof MemberReference) {
			addReference((MemberReference) ref);
		} else if (ref instanceof ClassReference) {
			addClassDependency((ClassReference) ref);
		} else {
			System.err.println("Unknown class: " + ref.getClass().getName());
			System.exit(1);
		}
	}

	@Override
	public void referenced() {
		super.referenced();
		for (BuildinDependency dep : aBuildinDependencies) {
			dep.referenced();
		}
		for (ClassReference cref : aClassDepencies) {
			// if (cref.getClassName().contains("java/lang/Exce")) {
			// System.err.println("juhu: " + cref);
			// }
			cref.referenced();
		}
		
		for (ConstantReference<?> conRef : aConstantReferences) {
			conRef.referenced();
		}
		// for (ExceptionHandler exh : aExceptionHandlers) {
		// exh.getExceptionConstantPoolIndex();
		// int classId = aLinkModel.getClassIdByName(mic.getMember().getClassName());
		//
		// }
	}

}
