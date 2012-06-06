package thinj.regression;

public class CharTest {


	public static char dims(char c) {
		return c == '@' ? 'A' : 'B';
	}

	private static void test1() {
		char cc = '?';
		char cc2 = dims(cc);
		Regression.verify('B' == cc2);
	}

	public static char dimsa(char[] arg) {
		return arg[0];
	}
	

	private static void test2() {
		char[] c1 = new char[10];
		c1[0] = 'K';
		Regression.verify(c1[0] == 'K');

		c1[1] = dimsa(c1);
		Regression.verify(c1[1] == 'K');
}

	public static void main() {
		test1();
		test2();
	}
}
