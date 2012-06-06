package thinj.linkmodel;

/**
 * This class contains the definition of a single exception handler
 */
public class ExceptionHandler {
	private int aStartPC;
	private int aEndPC;
	private int aExceptionHandlerPC;
	private final int aExceptionConstantPoolIndex;
	private final int aClassId;

	/**
	 * Constructor. Note that the program counters below is relative to the address of the first
	 * instruction in the method, so relocation is necessary.
	 * 
	 * @param classId Id of the enclosing / referencing class
	 * @param startPC The start-of-scope of the exception handler
	 * @param endPC The end-of-scope of the exception handler
	 * @param exceptionHandlerPC The address of the exception handler
	 * @param exceptionConstantPoolIndex The constant pool index of the exception handled by this
	 *            handler
	 * @param i
	 */
	public ExceptionHandler(int classId, int startPC, int endPC, int exceptionHandlerPC,
			int exceptionConstantPoolIndex) {
		aClassId = classId;
		aStartPC = startPC;
		aEndPC = endPC;
		aExceptionHandlerPC = exceptionHandlerPC;
		aExceptionConstantPoolIndex = exceptionConstantPoolIndex;
	}

	/**
	 * This method returns the start-of-scope of the exception handler
	 * 
	 * @return The start-of-scope of the exception handler
	 */
	public int getStartPC() {
		return aStartPC;
	}

	/**
	 * This method returns the end-of-scope of the exception handler
	 * 
	 * @return The end-of-scope of the exception handler (this value is exclusive)
	 */
	public int getEndPC() {
		return aEndPC;
	}

	/**
	 * This method returns the address of the exception handler
	 * 
	 * @return The address of the exception handler
	 */
	public int getExceptionHandlerPC() {
		return aExceptionHandlerPC;
	}

	/**
	 * This method returns the constant pool index of the exception handled by this handler
	 * 
	 * @return The constant pool index of the exception handled by this handler
	 */
	public int getExceptionConstantPoolIndex() {
		return aExceptionConstantPoolIndex;
	}

	@Override
	public String toString() {
		return "ExceptionHandler [aEndPC=" + aEndPC + ", aExceptionConstantPoolIndex="
				+ aExceptionConstantPoolIndex + ", aExceptionHandlerPC=" + aExceptionHandlerPC
				+ ", aStartPC=" + aStartPC + "]";
	}

	/**
	 * This method will offset all addresses with the supplied offset
	 * 
	 * @param codeOffset The offset to add to all addresses
	 */
	public void setCodeOffset(int codeOffset) {
		aStartPC += codeOffset;
		aEndPC += codeOffset;
		aExceptionHandlerPC += codeOffset;
	}

	/**
	 * This method returns the classId of the enclosing / referencing class
	 * 
	 * @return The classId of the enclosing / referencing class
	 */
	public int getClassId() {
		return aClassId;
	}
}
