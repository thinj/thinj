package thinj.regression.interfaces;

import thinj.regression.Regression;

public class AnInterfaceImpl implements AnInterface {
	@Override
	public int getLost() {
		return 99;
	}

	private int y;

	public void foobar() {
		y = x;
	}

	@Override
	public int get() {
		return y;
	}

	public static void test1() {
		AnInterface inf = new AnInterfaceImpl();
		inf.foobar();

		Regression.verify(inf.get() == AnInterface.x);
		Regression.verify(inf.getLost() == 99);
	}

	public static void main() {
		test1();
	}
}
