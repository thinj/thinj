package thinj.regression.checkcast;

import thinj.regression.Regression;

public class CheckCast {

	public interface IA {
		void a();
	}

	public static void test1() {
		Object o = new IA() {
			@Override
			public void a() {
			}
		};

		Regression.verify(o instanceof Object);
		Regression.verify(o instanceof IA);
	}

	public static class ClassA {
	}

	public static class ClassB extends ClassA {
	}

	public static void test2() {
		Regression.verify(!(new Object() instanceof ClassA));

		ClassA ca = new ClassA();
		Regression.verify(ca instanceof ClassA);
		Regression.verify(ca instanceof Object);

		ClassB cb = new ClassB();
		Regression.verify(cb instanceof ClassA);
		Regression.verify(cb instanceof Object);

		Regression.verify(!(ca instanceof ClassB));
	}

	public static void test3() {
		Object[] oa = new Object[2];
		Regression.verify(oa instanceof Object);
		Regression.verify(oa instanceof Object[]);

		CheckCast[] cca = new CheckCast[2];
		Regression.verify(cca instanceof CheckCast[]);

		int[] ia = new int[2];
		Regression.verify(ia instanceof int[]);

	}

	private static void test4() {
		Regression.verify(!(null instanceof Object));

		int[][] iaa = new int[][] { { 1, 2 }, { 3, 4 } };
		Regression.verify(iaa instanceof int[][]);

		Object[][] oaa = new Object[8][];
		Regression.verify(oaa instanceof Object[][]);
		multitest(oaa);
		multitest(new CheckCast[6][]);
	}

	/**
	 * Shall be called with an Object[][] as argument otherwise test fails
	 * 
	 * @param o An Object[][]
	 */
	private static void multitest(Object o) {
		Regression.verify(o instanceof Object[][]);
	}		

	public static void main() {
		test1();
		test2();
		test3();
		test4();
		String s = "hej" + 45;
	}

}
