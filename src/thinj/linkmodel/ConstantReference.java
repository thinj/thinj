package thinj.linkmodel;

/**
 * This class represents a reference to any class constant
 * 
 * @author hammer
 * 
 */
public class ConstantReference<T> extends ConstantPoolReference {
	private final T aValue;

	/**
	 * Constructor
	 * 
	 * @param classId The id of the referencing class
	 * @param constantPoolIndex The index into the constant pool of the referencing class
	 * @param value The constant value
	 */
	ConstantReference(int classId, int constantPoolIndex, T value) {
		super(classId, constantPoolIndex);
		aValue = value;
	}

	/**
	 * @return The constant value
	 */
	public T getValue() {
		return aValue;
	}
}
