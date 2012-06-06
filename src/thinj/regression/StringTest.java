package thinj.regression;

public class StringTest {

	public StringTest(int x) {
	}

	public static void test1() {
		String s1 = "Hello ";
		Regression.verify(s1.length() == 6);

		String s2 = new String(new char[] { 'A', 'B', 'E' });
		Regression.verify(s2.length() == 3);
	}

	private static void test2() {
		// String s = new String(new char[] {'A', 'B', 'E'});
		String s1 = "Hello ";
		String s2 = "world";
		String s3 = s1 + s2;
		TinySystem.outInt(s3.length());
		TinySystem.outString(s3);
		TinySystem.outString(new Object().toString());
	}

	public static void main() {
		test2();
		test1();
	}

}
