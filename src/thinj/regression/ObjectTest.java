package thinj.regression;

public class ObjectTest {

	public static void main() {
		test1();
	}

	private static void test1() {
		Object o = new Object();
		Regression.verify(o != null);
		o = null;
		Regression.verify(o == null);
	}
}
