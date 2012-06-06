package thinj.linkmodel;

public enum ClassTypeEnum {
	InterfaceType("CT_INTERFACE", 0), 
	ClassType("CT_CLASS", 0), 
	ReferenceArray("CT_OBJECT_ARRAY", 3, "sizeof(jobject)", "?"), 
	ArrayBoolean("CT_BOOLEAN_ARRAY", 4,"1", "Z"),
	CharArray("CT_CHAR_ARRAY", 5,"sizeof(jchar)", "C"),
	FloatArray("CT_FLOAT_ARRAY", 6,"sizeof(jfloat)", "!"),
	DoubleArray("CT_DOUBLE_ARRAY", 7,"sizeof(jdouble)", "!"),
	ByteArray("CT_BYTE_ARRAY", 8,"1", "B"),
	ShortArray("CT_SHORT_ARRAY", 9,"sizeof(jshort)", "!"),
	IntArray("CT_INT_ARRAY", 10, "sizeof(jint)", "I"),
	LongArray("CT_LONG_ARRAY", 11,"sizeof(jlong)", "!");

	/**
	 * Type codes as specified in JVM Spec
	 */
	public static final int T_REFERENCE = 3;
	public static final int T_BOOLEAN = 4;
	public static final int T_CHAR = 5;
	public static final int T_FLOAT = 6;
	public static final int T_DOUBLE = 7;
	public static final int T_BYTE = 8;
	public static final int T_SHORT = 9;
	public static final int T_INT = 10;
	public static final int T_LONG = 11;


	private final String aClassTypeName;
	private final int aArrayType;
	private final String aSize;
	private final String aArraySuffix;

	private ClassTypeEnum(String classTypeName, int arrayType) {
		aClassTypeName = classTypeName;
		aArrayType = arrayType;
		aSize = null;
		aArraySuffix = null;
	}

	private ClassTypeEnum(String classTypeName, int arrayType, String size, String arraySuffix) {
		aClassTypeName = classTypeName;
		aArrayType = arrayType;
		aSize = size;
		aArraySuffix = arraySuffix;
	}

	/**
	 * This method returns the class type name as corresponds to the CLASS_TYPE in c-files 
	 * @return The class type name as corresponds to the CLASS_TYPE in c-files
	 */
	public String getClassTypeName() {
		return aClassTypeName;
	}

	/**
	 * This method returns the ARRAY_TYPE of the class type. Note that this is only defined for 
	 * array-class types
	 * @return The ARRAY_TYPE of the class type
	 */
	public int getArrayType() {
		return aArrayType;
	}

	/**
	 * This method resolves a ClassTypeEnum based on the array_type
	 * @param arrayType The array type to resolve
	 * @return null is returned if no match
	 */
	public static ClassTypeEnum resolveByArrayType(int arrayType) {
		ClassTypeEnum cte = null;
		for (int i = 0; i < ClassTypeEnum.values().length && cte == null; i++) {
			if (ClassTypeEnum.values()[i].getArrayType() == arrayType) {
				cte = ClassTypeEnum.values()[i];
			}
		}

		return cte;
	}

	/**
	 * For array types this method returns the size of an array element; for other types null is returned.
	 * @return The size of an array element as a well formed C compatible string
	 */
	public String getSize() {
		return aSize;
	}
	
	/**
	 * This resolves the supplied class name into a ClassType under the assumption that 'className' is 
	 * of the form '[a', where 'a' is to be replaced by one of I, ???  
	 * @param className
	 * @return The resolved class or null, if no match
	 */
	public static ClassTypeEnum resolveArrayType(String className) {
		ClassTypeEnum cte = null;
		for (int i = 0; i < ClassTypeEnum.values().length && cte == null; i++) {
			if (("[" + ClassTypeEnum.values()[i].aArraySuffix).equals(className)) {
				cte = ClassTypeEnum.values()[i];
			}
		}
		
		return cte;
	}

	/**
	 * This method returns the signature of an array identified by this type
	 * @return The signature of an array identified by this type
	 */
	public String getSignature() {
		return "[" + aArraySuffix;
	}
}
