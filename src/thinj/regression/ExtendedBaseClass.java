package thinj.regression;

public class ExtendedBaseClass extends BaseClass {
	@Override
	public int foobar() {
		return 2;
	}

//	private static void test1() {
//		ExtendedBaseClass x = new ExtendedBaseClass();
//		Regression.verify(x.foobar() == 2);
//	}
//
//	private static void test2() {
//		BaseClass x = new ExtendedBaseClass();
//		Regression.verify(x.foobar() == 2);
//	}
//
//	private static void test3() {
//		BaseClass x = new BaseClass();
//		Regression.verify(x.foobar() == 1);
//	}
//
//	private static void test4() {
//		BaseClass x = new BaseClass();
//		Regression.verify(x.barfoo() == 3);
//	}
//
//	private static void test5() {
//		ExtendedBaseClass x = new ExtendedBaseClass();
//		Regression.verify(x.barfoo() == 3);
//	}
//
//	private static void test6() {
//		BaseClass x = new ExtendedBaseClass();
//		Regression.verify(x.barfoo() == 3);
//	}

//	private void snustobak() {
//		TinySystem.outInt(456);
//	}
	private static void test7() {
		ExtendedBaseClass x = new ExtendedBaseClass();
		Regression.verify(x.adder(1) == 2);
		Regression.verify(x.adder(1) == 2);
//		ExtendedBaseClass x = new ExtendedBaseClass();
//		x.snustobak();
	}


	public static void main() {
//		test1();
//		test2();
//		test3();
//		test4();
//		test5();
//		test6();
		test7();
	}

}
