package thinj.regression.exceptions;

import thinj.regression.Regression;

public class TestNullPointerException {
	// ////////////////////////////////////////////////////////////
	private static boolean aUncaughtTest1;
	/**
	 * Test of NPE throwing
	 */
	private static void test1() {
		aUncaughtTest1 = false;
		Object[] ba = null;
		int len = ba.length;
		aUncaughtTest1 = true;
	}

	// ////////////////////////////////////////////////////////////
	private static boolean aUncaughtTest2;
	/**
	 * Test2 of NPE throwing
	 */
	private static void test2() {
		aUncaughtTest2 = false;
		int[] ba = null;
		int i = ba[0];
		aUncaughtTest2 = true;
	}

	// ////////////////////////////////////////////////////////////

	public static void main() {
		int caugthCounter = 0;
		try {
			test1();
		} catch (Exception e) {
			//e.printStackTrace();
			caugthCounter++;
		}
		Regression.verify(!aUncaughtTest1);
		try {
			test2();
		} catch (Exception e) {
			//e.printStackTrace();
			caugthCounter++;
		}
		Regression.verify(!aUncaughtTest2);
		Regression.verify(caugthCounter == 2);
	}
}
