package thinj.regression;

public class InstanceMethodsSpecial extends InstanceMethods {
	@Override
	protected void testargs(int i, boolean b) {
		Regression.verify(i == 145);
		Regression.verify(b);
	}

	public void basePrint2() {
		super.basePrint2();
		//TinySystem.outInt(222);
	}


	public static void main() {
		InstanceMethods.main();
		InstanceMethods ims = new InstanceMethodsSpecial();
		ims.basePrint(200);
		ims.basePrint2();
	}

}
