package thinj;

import java.util.HashMap;

/**
 * This class contains a repo of code generators to be used when generating native interface code.
 * 
 * @author hammer
 * 
 */
public class NativeTypeRepository {
	private static HashMap<String, NativeTypeHandler> aMap = new HashMap<String, NativeTypeHandler>();
	private static DefaultHandler aDefaultHandler;
	static {
		register(new NativeBoolean());
		register(new NativeInt());
		register(new NativeLong());
		register(new NativeChar());
		register(new NativeVoid());
		register(new NativeString());
		aDefaultHandler = new DefaultHandler();
	}

	/**
	 * Public no-constructor
	 */
	private NativeTypeRepository() {
	}

	/**
	 * This method registers a type handler
	 * 
	 * @param handler The handler to register
	 */
	private static void register(NativeTypeHandler handler) {
		aMap.put(handler.getKey(), handler);
	}

	/**
	 * This method returns the type handler for the supplied java type
	 * 
	 * @param javaType (e.g. 'int', 'boolean', etc)
	 * @return The type handler for the supplied java type
	 */
	public static NativeTypeHandler getHandler(String javaType) {
		NativeTypeHandler handler = aMap.get(javaType);
		if (handler == null) {
			handler = aDefaultHandler;
		}

		return handler;
	}

	public static abstract class NativeTypeHandler {
		/**
		 * This method returns code for popping this type of the stack and assigning the value to
		 * the supplied parameter
		 * 
		 * @param argName The name of the parameter to assign to
		 * @return The generated code
		 */
		public abstract String getParameterAssignment(String argName);

		/**
		 * This method returns the native type
		 * 
		 * @return The native type
		 */
		public abstract String getParameterType();

		/**
		 * This method generates code for pushing the return value
		 * 
		 * @param returnVarName The name of the variable containing the return value
		 * @return The generated code
		 */
		public abstract String getReturnTypePushCode(String returnVarName);

		/**
		 * This method generates code for assigning the return value
		 * 
		 * @param returnVarName The name of the variable containing the return value
		 * @return The generated code
		 */
		public String getAssignReturnTypeCode(String returnVarName) {
			return getParameterType() + " " + returnVarName + " = ";
		}

		/**
		 * This method returns the java representation of the type
		 * 
		 * @return The java representation of the type
		 */
		public abstract String getKey();
	}

	/**
	 * Handler for 'char'
	 * 
	 * @author hammer
	 */
	private static class NativeChar extends NativeTypeHandler {
		@Override
		public String getParameterAssignment(String argName) {
			return "    " + getParameterType() + " " + argName + " = operandStackPopJavaInt();";
		}

		@Override
		public String getParameterType() {
			return "jchar";
		}

		@Override
		public String getKey() {
			return "char";
		}

		@Override
		public String getReturnTypePushCode(String returnVarName) {
			return "operandStackPushJavaInt(" + returnVarName + ");";
		}
	}

	/**
	 * Handler for 'int'
	 * 
	 * @author hammer
	 */
	private static class NativeInt extends NativeTypeHandler {
		@Override
		public String getParameterAssignment(String argName) {
			return "    " + getParameterType() + " " + argName + " = operandStackPopJavaInt();";
		}

		@Override
		public String getParameterType() {
			return "jint";
		}

		@Override
		public String getKey() {
			return "int";
		}

		@Override
		public String getReturnTypePushCode(String returnVarName) {
			return "operandStackPushJavaInt(" + returnVarName + ");";
		}
	}

	/**
	 * Handler for 'long'
	 */
	private static class NativeLong extends NativeTypeHandler {
		@Override
		public String getParameterAssignment(String argName) {
			return "    " + getParameterType() + " " + argName + " = operandStackPopJavaLong();";
		}

		@Override
		public String getParameterType() {
			return "jlong";
		}

		@Override
		public String getKey() {
			return "long";
		}

		@Override
		public String getReturnTypePushCode(String returnVarName) {
			return "operandStackPushJavaLong(" + returnVarName + ");";
		}
	}

	/**
	 * Handler for 'boolean'
	 * 
	 * @author hammer
	 */
	private static class NativeBoolean extends NativeTypeHandler {
		@Override
		public String getParameterAssignment(String argName) {
			return "    " + getParameterType() + " " + argName + " = operandStackPopJavaInt() != 0;";
		}

		@Override
		public String getParameterType() {
			return "BOOL";
		}

		@Override
		public String getKey() {
			return "boolean";
		}

		@Override
		public String getReturnTypePushCode(String returnVarName) {
			return "stackPushSomeBooleanfætter";
		}
	}

	/**
	 * Handler for any kind of 'Object'
	 * 
	 * @author hammer
	 */
	private static class DefaultHandler extends NativeTypeHandler {
		@Override
		public String getParameterAssignment(String argName) {
			return "    " + getParameterType() + " " + argName + " = operandStackPopObjectRef();";
		}

		@Override
		public String getParameterType() {
			return "jobject";
		}

		@Override
		public String getKey() {
			return null;
		}

		@Override
		public String getReturnTypePushCode(String returnVarName) {
			return "operandStackPushObjectRef(" + returnVarName + ");";
		}
	}

	/**
	 * Handler for 'java.lang.String'
	 * 
	 * @author hammer
	 */
	private static class NativeString extends NativeTypeHandler {
		@Override
		public String getParameterAssignment(String argName) {
			return "    " + getParameterType() + " " + argName + " = operandStackPopObjectRef();";
		}

		@Override
		public String getParameterType() {
			return "jstring";
		}

		@Override
		public String getKey() {
			return "String";
		}

		@Override
		public String getReturnTypePushCode(String returnVarName) {
			return "operandStackPushObjectRef(" + returnVarName + ");";
		}
	}

	/**
	 * Handler for 'void'
	 * 
	 * @author hammer
	 */
	private static class NativeVoid extends NativeTypeHandler {
		@Override
		public String getParameterAssignment(String argName) {
			return ""; // No assignment!
		}

		@Override
		public String getParameterType() {
			return "void";
		}

		@Override
		public String getKey() {
			return "void";
		}

		@Override
		public String getReturnTypePushCode(String returnVarName) {
			return "";
		}

		@Override
		public String getAssignReturnTypeCode(String returnVarName) {
			return "";
		}
	}
}
