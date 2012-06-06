package thinj.linkmodel;

/**
 * This class represents a reference to any class member
 * 
 * @author hammer
 * 
 */
public class MemberReference extends ConstantPoolReference {
	// To be set during link time:
	private int aReferencedClassId;
	private final String aReferencedClassName;
	private final Signature aSignature;

	/**
	 * Constructor
	 * 
	 * @param classId The id of the referencing class
	 * @param constantPoolIndex The index into the constant pool of the referencing class
	 */
	public MemberReference(String referencedClassName, Signature signature, int classId,
			int constantPoolIndex) {
		super(classId, constantPoolIndex);
		aReferencedClassName = ClassInSuite.getGlobalName(referencedClassName);
		aSignature = signature;
	}

	public int getReferencedClassId() {
		return aReferencedClassId;
	}

	public void setReferencedClassId(int referencedClassId) {
		aReferencedClassId = referencedClassId;
	}

	public String getReferencedClassName() {
		return aReferencedClassName;
	}
	
	public Signature getSignature() {
		return aSignature;
	}
	
	public String format() {
		return aReferencedClassName +"#" + aSignature.format();
	}

	@Override
	public String toString() {
		return "MemberReference [aReferencedClassId=" + aReferencedClassId
				+ ", aReferencedClassName=" + aReferencedClassName + "]";
	}
}
