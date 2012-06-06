package thinj.regression;

public class ByteTest {
	public static byte dims(byte b) {
		return (byte) (b == 15 ? 50 : -50);
	}

	private static void test1() {
		byte bb = 15;
		byte bb2 = dims(bb);
		Regression.verify(50 == bb2);
	}

	public static byte dimsa(byte[] arg) {
		return arg[0];
	}

	private static void test2() {
		byte[] c1 = new byte[10];
		c1[0] = 0x7f;
		Regression.verify(c1[0] == 0x7f);

		c1[1] = dimsa(c1);
		Regression.verify(c1[1] == 0x7f);
	}

	private static void test3() {
		for (int i = 0; i < 128; i++) {
			byte b = (byte) i;
			Regression.verify(b >= 0);
		}
		for (int i = 128; i < 256; i++) {
			byte b = (byte) i;
			Regression.verify(b < 0);
		}
	}
	
	public static void main() {
		test1();
		test2();
		test3();
	}
}
