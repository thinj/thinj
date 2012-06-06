package thinj.regression.gc;

public class HeapTest {
	public static void test0() {
		int i = 0;
		String s = "Hello " + i;
//		String s = "Hello " + "world: " + i;
//		String s = "Hello " + 0;
	}

	public static void test1() {
		int i = 0;
		for (int x = 0; x < 100; x++) {
			String s = "Hello " + "world: " + i;
			i++;
			for (int j = 0; j < 100; j++) {
				new Object();
			}
		}
	}

	public static void test2() {
		int count = 0;
		for (int i = 0; i < 100; i++) {
			String s = "Hello world";
			for (int j = 0; j < 100; j++) {
				new Object();
				count++;
			}
		}
	}

	public static void main() {
		test0();
		test1();
		test2();
	}
}
