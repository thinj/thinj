package thinj.linkmodel;

/**
 * This class represents a field in a class
 * @author hammer
 *
 */
public class FieldInClass extends MethodOrField {
	private final int aSize;
	private int aAddress;

	/**
	 * @param member The identification of the field
	 * @param size The size of the field
	 * @param isStatic true, if the field is static, false otherwise
	 */
	FieldInClass(Member member, int size, boolean isStatic) {
		super(member, isStatic);
		aSize = size;
	}

	/**
	 * This method returns the size of the field
	 * 
	 * @return The size of the field
	 */
	public int getSize() {
		return aSize;
	}

	public void setAddress(int address) {
		aAddress = address;
	}

	public int getAddress() {
		return aAddress;
	}
}
