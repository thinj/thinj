package thinj.regression;

public class Ineg {
	private static void test1() {
		int i;
		i = 45;
		Regression.verify(i == 45);
		i = -i;
		Regression.verify(i == -45);
	}

	private static void test2() {
		int i = 101;
		Regression.verify((i << 2) == 404);
		i = 500;
		Regression.verify((i >> 2) == 125);
		
		i = 0xff;
		Regression.verify((i & 0xf) == 0x0f);
		i = 0xf0;
		Regression.verify((i | 0xf) == 0xff);
	}
	
	private static void test3() {
		int i = 0; 
		i = ~i;
		Regression.verify((i & 0xff) == 0xff);
		
		i = 0x5a;
		i = i^0;		
		Regression.verify((i & 0xff) == 0x5a);
		
		i = i^0xff;
		Regression.verify((i & 0xff) == 0xa5);
	}

	public static void main() {
		test1();
		test2();
		test3();
	}
}
