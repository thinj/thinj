package thinj;

/**
 * This class implements a reference to an int
 * 
 */
public class IntInABox {
	private int aValue;

	public IntInABox(int value) {
		aValue = value;
	}

	public int getValue() {
		return aValue;
	}

	/**
	 * This method increments the int value and return the value as it was before the increment 
	 * @return The value as it was before the increment
	 */
	public int increment() {
		return aValue++;
	}
}
