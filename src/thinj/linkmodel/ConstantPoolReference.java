package thinj.linkmodel;

/**
 * This class contains a reference to any item from any constant pool.
 * 
 * @author hammer
 * 
 */
public abstract class ConstantPoolReference extends Linkable {
	private int aClassId;
	private final int aConstantPoolIndex;

	/**
	 * Constructor
	 * 
	 * @param classId The id of the referencing class
	 * @param constantPoolIndex The index into the constant pool of the referencing class
	 */
	protected ConstantPoolReference(int classId, int constantPoolIndex) {
		aClassId = classId;
		aConstantPoolIndex = constantPoolIndex;
	}

	/**
	 * This method returns the id of the referencing class
	 * 
	 * @return The id of the referencing class
	 */
	public int getClassId() {
		return aClassId;
	}

	/**
	 * This method sets the class id
	 * 
	 * @param classId The new class id
	 */
	public void renumberClassId(int classId) {
		aClassId = classId;
	}

	/**
	 * @return The index into the constant pool of the referencing class
	 */
	public int getConstantPoolIndex() {
		return aConstantPoolIndex;
	}

	@Override
	public String toString() {
		return "ConstantPoolReference [aClassId=" + aClassId + ", aConstantPoolIndex="
				+ aConstantPoolIndex + "]";
	}
}
