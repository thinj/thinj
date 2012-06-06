package thinj.regression.arrays;

import thinj.regression.Regression;

public class ArrayTest {
	private int[] aValues;

	public ArrayTest() {
		aValues = new int[4];
		aValues[3] = 45;
		Regression.verify(aValues[3] == 45);

		for (int i = 0; i < aValues.length; i++) {
			aValues[i] = i;
			Regression.verify(aValues[i] == i);
		}
		Regression.verify(aValues.length == 4);

	}

	private static void test1() {
		new ArrayTest();
	}

	private static void test2() {
		boolean[] ba = new boolean[7];
		ba[3] = true;
		for (int i = 0; i < ba.length; i++) {
			ba[i] = (i & 1) == 1;
			Regression.verify(ba[i] == ((i & 1) == 1));
		}
		
		Regression.verify(ba.length == 7);
	}

	public static void main() {
		test1();
		test2();
	}
}
