package thinj.regression;

public class InstanceAttributes {
	protected int aValue;
	
	private boolean aEnabled;
	private Object aObject;
	
	public static int aStaticDims = 56;
	
	public InstanceAttributes() {
		aValue = 47;
		aEnabled = true;
		aObject = null;
	}

	private static void test1() {
		InstanceAttributes inc = new InstanceAttributes();
		Regression.verify(inc.aValue == 47);
		Regression.verify(inc.aEnabled);
	}
	
	
	public static void testNull() {
		InstanceAttributes inc = new InstanceAttributes();
		Regression.verify(inc.aObject == null);
	}
	
	public static void main() {
		test1();
		testNull();
	}
}
