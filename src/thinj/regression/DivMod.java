package thinj.regression;


public class DivMod {
	private static int tusind = 1000;

	private static void helper(int product, int divisor) {
		if (divisor == 1 || divisor == -1) {
			Regression.verify((product + divisor - 1) % divisor == 0);
		} else if (product + divisor - 1 < 0) {
			Regression.verify((product + divisor - 1) % divisor == -1);
		} else {
			if (divisor - 1 < 0) {
				Regression.verify((product + divisor - 1) % divisor == -divisor - 1);
			} else {
				Regression.verify((product + divisor - 1) % divisor == divisor - 1);
			}
		}
	}

	private static void test1() {
		Regression.verify(0x7fffffff / 0x7fffffff == 1);
		Regression.verify(0x7ffffffe / 0x7fffffff == 0);
		Regression.verify(0x80000000 / 0x80000000 == 1);
		Regression.verify(0x7fffffff / 0x80000000 == 0);
		Regression.verify(0xffffffff / 0xffffffff == 1);
		Regression.verify(0xfffffffe / 0xffffffff == 2);
		Regression.verify(0xffffffff / 0xfffffffe == 0);

		int count = 0;
		Regression.verify((-tusind % -7) == -6);
		Regression.verify((tusind % -7) == 6);
		Regression.verify((-tusind % 7) == -6);
		Regression.verify((tusind % 7) == 6);

		for (int ui = -1000; ui < 1000; ui += 9) {
			for (int uj = -1000; uj < 1000; uj += 7) {
				if (ui != 0 && uj != 0) {
					int prod = ui * uj;
					Regression.verify(prod / ui == uj);
					Regression.verify(prod / uj == ui);

					Regression.verify(prod % ui == 0);
					Regression.verify(prod % uj == 0);

					helper(prod, ui);
					helper(prod, uj);

					if (++count >= 63778 / 80) {
						System.out.print(".");
						count = 0;
					}
				}
			}
		}
		System.out.println();
	}

	public static void main() {
		test1();
	}
}
