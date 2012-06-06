package thinj.regression.arrays;

import thinj.regression.Regression;

public class MultiArrayTest {
	static int dims = 56;
	static {
		dims = 87;
		DummyClass.cyclicDependency();
	}

	private static void test1() {
		int[][] iaa = new int[][] { { 1, 2 }, { 3, 4 } };
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				Regression.verify(iaa[i][j] == i * 2 + j + 1);
			}
		}
	}

	private static void test2() {
		Object[] oa = new MultiArrayTest[2];
		for (int i = 0; i < oa.length; i++) {
			Regression.verify(oa[i] == null);
		}
	}

	public static void test3() {
		// System.out.println("test3");
		Object[] oa = new DummyClass[2];
		Regression.verify(oa.length == 2);
		// new DummyClass();
	}

	// public static void main(String[] a) {
	// test3();
	// }

	public static void main() {
		Regression.verify(dims == 87);
		test1();
		test2();
		test3();
	}

}
