package thinj;

import java.util.LinkedList;

import thinj.linkmodel.ClassInSuite;
import thinj.linkmodel.ConstantPoolReference;
import thinj.linkmodel.LinkModel;
import thinj.linkmodel.Member;
import thinj.linkmodel.Signature;

/**
 * This enumeration contains the dependencies that hides in the JVM. All classes etc. are generated
 * in the output file => it is impossible to link directly in C to these dependencies. This
 * enumeration supports the building of a link table to be used at run time.
 * 
 * @author hammer
 * 
 */
public enum BuildinDependency {					
	// @formatter:off
	NULL_POINTER_EXCEPTION_CLASS(NullPointerException.class), 
    NULL_POINTER_EXCEPTION_CONSTRUCTOR(NullPointerException.class, "<init>", "()V"),
    
    ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_CLASS(ArrayIndexOutOfBoundsException.class),
    ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_CONSTRUCTOR(ArrayIndexOutOfBoundsException.class,
    		                                        "<init>", "(I)V"),
    		                                        
    ARITHMETIC_EXCEPTION_CLASS(ArithmeticException.class),
    ARITHMETIC_EXCEPTION_CONSTRUCTOR(ArithmeticException.class,
                                                    "<init>", "(Ljava/lang/String;)V"),
                                                    
    OUT_OF_MEMORY_ERROR_CLASS(OutOfMemoryError.class),
    OUT_OF_MEMORY_ERROR_CONSTRUCTOR(OutOfMemoryError.class, "<init>", "()V"),
    OUT_OF_MEMORY_ERROR_GET_INSTANCE(OutOfMemoryError.class, "getInstance", 
	 	   "()Ljava/lang/OutOfMemoryError;"),
   
    NEGATIVE_ARRAY_SIZE_EXCEPTION_CLASS(NegativeArraySizeException.class),
    NEGATIVE_ARRAY_SIZE_EXCEPTION_CONSTRUCTOR(NegativeArraySizeException.class,
           "<init>", "()V"),
           
    CLASS_CAST_EXCEPTION_CLASS(ClassCastException.class),
    CLASS_CAST_EXCEPTION_CONSTRUCTOR(ClassCastException.class, "<init>", "()V"),

    ;
	// @formatter:on

	private final String aReferencedClassName;
	private final String aReferencedMember;
	private final String aSignature;
	private boolean aReferenced;
	private final String aMacro;

	/**
	 * Class Member dependency constructor
	 * 
	 * @param referencedClass
	 * @param referencedMember
	 * @param signature
	 */
	private BuildinDependency(Class<?> referencedClass, String referencedMember, String signature) {
		aReferencedClassName = ClassInSuite.getGlobalName(referencedClass.getName());
		aReferencedMember = referencedMember;
		aSignature = signature;
		if (aReferencedMember != null) {
			aMacro = CodeGenerator.generateMemberLinkIdMacro(new Member(aReferencedClassName,
					referencedMember, signature));
		} else {
			aMacro = CodeGenerator.generateClassIdMacro(aReferencedClassName);
		}
	}

	/**
	 * Class dependency constructor
	 * 
	 * @param referencedClass
	 */
	private BuildinDependency(Class<?> referencedClass) {
		this(referencedClass, null, null);
	}

	/**
	 * This method create a ConstantPoolReference for this dependency.
	 * 
	 * @param linkModel Used for creation of a MemberReference
	 * @param referencingClassId The class containing the method (mic)
	 * @param constantPoolLength The constant pool length reference. Is incremented by one.
	 * @return The created ConstantpoolReference. Note that it can be casted to MemberReference or
	 *         ClassReference depending on which constructor used for this instance
	 */
	public ConstantPoolReference createDependency(LinkModel linkModel, int referencingClassId,
			IntInABox constantPoolLength) {
		ConstantPoolReference ref;
		if (aReferencedMember == null) {
			ref = linkModel.createClassReference(referencingClassId,
					constantPoolLength.increment(), aReferencedClassName);

		} else {
			ref = linkModel.createMemberReference(aReferencedClassName, new Signature(
					aReferencedMember, aSignature), referencingClassId, constantPoolLength
					.increment());
		}
		return ref;
	}

	/**
	 * This method marks this dependency as referenced => it shall be included in suite. If this
	 * dependency is a member reference, the containing class is searched within these enums, and if
	 * found, it is marked as referenced as well. This ensures that the class will figure in the
	 * generated link tables.
	 */
	public void referenced() {
		aReferenced = true;
		if (isMemberReference()) {
			// Reference also the class dependency:
			for (BuildinDependency dep : values()) {
				if (dep.aReferencedClassName.equals(aReferencedClassName) && !dep.isReferenced()) {
					dep.referenced();
				}
			}
		}
	}

	/**
	 * This method return true if this dependency is referenced => it shall be included in suite
	 * 
	 * @return true if this dependency is referenced => it shall be included in suite
	 */
	public boolean isReferenced() {
		return aReferenced;
	}

	/**
	 * This method returns the C-style macro identifying the dependency
	 * 
	 * @return The C-style macro identifying the dependency
	 */
	public String getMacro() {
		return aMacro;
	}

	/**
	 * This method returns all the referenced StandardDependencies
	 * 
	 * @return All the referenced StandardDependencies
	 */
	public static BuildinDependency[] getReferencedDependencies() {
		LinkedList<BuildinDependency> l = new LinkedList<BuildinDependency>();
		for (BuildinDependency dep : BuildinDependency.values()) {
			if (dep.isReferenced()) {
				l.add(dep);
			}
		}

		return l.toArray(new BuildinDependency[l.size()]);
	}

	/**
	 * This method returns the symbol that c-code shall use when looking up the dependency in the
	 * standard dependencies list in the suite
	 * 
	 * @return The symbol that c-code shall use when looking up the dependency in the standard
	 *         dependencies list in the suite
	 */
	public String getCCodeReference() {
		return name();
	}

	/**
	 * This method returns true if the reference is a member reference (a 'link id') or false, if
	 * the reference is a class (a 'class id')
	 * 
	 * @return true if the reference is a member reference (a 'link id') or false, if the reference
	 *         is a class (a 'class id')
	 */
	public boolean isMemberReference() {
		return aReferencedMember != null;
	}
}
