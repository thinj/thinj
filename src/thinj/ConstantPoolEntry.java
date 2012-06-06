package thinj;

/**
 * This class collects the bits and pieces to be used by the {@link CodeGenerator} when collecting
 * constant pool info for each class
 */
public class ConstantPoolEntry {
	private final int aClassId;
	private int aMemberReferenceCount;
	private int aNumberOfMethods;

	/**
	 * Constructor
	 * 
	 * @param classId The class containing the constant pool
	 */
	public ConstantPoolEntry(int classId) {
		aClassId = classId;
	}

	/**
	 * @return the classId
	 */
	public int getClassId() {
		return aClassId;
	}

	/**
	 * @return The name of the C- array containing method references
	 */
	public String getMethodReferences() {
		return "allMethodReferences" + aClassId;
	}

	/**
	 * @param memberReferenceCount
	 */
	public void setMethodReferencesLength(int memberReferenceCount) {
		aMemberReferenceCount = memberReferenceCount;
	}

	/**
	 * @return The numbers of method references
	 */
	public int getMethodReferencesLength() {
		return aMemberReferenceCount;
	}

	/**
	 * Sets the number of methods declared by this class
	 * 
	 * @param numberOfMethods The number of methods declared by this class
	 */
	public void setNumberOfMethods(int numberOfMethods) {
		aNumberOfMethods = numberOfMethods;
	}

	/**
	 * @return the number of methods declared by this class
	 */
	public int getNumberOfMethods() {
		return aNumberOfMethods;
	}

	/**
	 * @return the name of the C - array containing the method info
	 */
	public String getMethodsInClass() {
		return "allMethods" + aClassId;
	}

}
