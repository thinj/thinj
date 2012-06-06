package thinj.regression.abstractclass;

import thinj.regression.Regression;

public class ExtendingClass extends AbstractClass{

	public ExtendingClass(int value) {
		super(value);
	}

	@Override
	public void foobar() {
		Regression.verify(aValue == 45);
	}
}
