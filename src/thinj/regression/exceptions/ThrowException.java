package thinj.regression.exceptions;

import thinj.regression.Regression;

public class ThrowException {

	// ////////////////////////////////////////////////////////////
	private static void test1() {
		boolean caught = false;
		try {
			throw new Exception("just testing 1");
		} catch (Exception e) {
			//e.printStackTrace();
			caught = true;
		}
		Regression.verify(caught);
	}

	// ////////////////////////////////////////////////////////////
	static void testHelper() throws Throwable {
		throw new Throwable("just testing");
	}

	private static void test2() {
		boolean caught = false;
		try {
			testHelper();
		} catch (Throwable e) {
			//e.printStackTrace();
			caught = true;
		}
		Regression.verify(caught);
	}

	// ////////////////////////////////////////////////////////////

	private static void test3() {
		boolean caught = false;
		try {
			ExceptionProxying.testExceptionProxy();
		} catch (Throwable e) {
			//e.printStackTrace();
			caught = true;
		}
		Regression.verify(caught);
	}

	// ////////////////////////////////////////////////////////////
	private static void test4() {
		// test exception sub classing:
		boolean caught = false;
		try {
			throw new Exception("just testing 4");
		} catch (Throwable e) {
			//e.printStackTrace();
			caught = true;
		}
		Regression.verify(caught);
	}

	// //////////////////////////////////////////////////////////
	private static void test5() {
		// Simple finally test
		boolean caught = false;
		boolean finallyVisited = false;
		try {
			throw new Exception("just testing 1");
		} catch (Exception e) {
			//e.printStackTrace();
			caught = true;
		} finally {
			finallyVisited = true;
		}
		Regression.verify(caught);
		Regression.verify(finallyVisited);
	}

	// ////////////////////////////////////////////////////////////
	private static boolean aFinallyTest6;

	private static void test6Helper() throws Exception {
		aFinallyTest6 = false;
		try {
			throw new Exception("just testing 1");
		} finally {
			aFinallyTest6 = true;
		}
	}

	//
	// Test of finally in two levels
	//
	private static void test6() {
		boolean caught = false;
		boolean localFinally = false;
		try {
			test6Helper();
		} catch (Throwable e) {
			//e.printStackTrace();
			caught = true;
		} finally {
			localFinally = true;
		}
		Regression.verify(aFinallyTest6);
		Regression.verify(caught);
		Regression.verify(localFinally);
	}

	// ////////////////////////////////////////////////////////////
	private static boolean aFinallyTest7_1;
	private static boolean aFinallyTest7_2;

	private static void test7Helper2() throws Exception {
		aFinallyTest7_2 = false;
		try {
			throw new Exception("just testing 1");
		} finally {
			aFinallyTest7_2 = true;
		}
	}

	private static void test7Helper1() throws Exception {
		aFinallyTest7_1 = false;
		try {
			test7Helper2();
		} finally {
			aFinallyTest7_1 = true;
		}
	}

	//
	// Test of finally in two levels
	//
	private static void test7() {
		boolean caught = false;
		boolean localFinally = false;
		try {
			test7Helper1();
		} catch (Throwable e) {
			//e.printStackTrace();
			caught = true;
		} finally {
			localFinally = true;
		}
		Regression.verify(aFinallyTest7_1);
		Regression.verify(aFinallyTest7_2);
		Regression.verify(caught);
		Regression.verify(localFinally);
	}


	// ////////////////////////////////////////////////////////////

	public static void main() {
		 test1();
		 test2();
		 test3();
		 test4();
		 test5();
		 test6();
		 test7();
	}
}
