package thinj.regression;

public class InstanceSubclassing extends InstanceAttributes {

	private int aAttr;

	public InstanceSubclassing() {
		super();
		aAttr = 999;
	}

	public static void test1() {
		InstanceSubclassing sub = new InstanceSubclassing();
		Regression.verify(sub.aValue == 47);
		Regression.verify(sub.aAttr == 999);
		Regression.verify(aStaticDims == 56);

		sub.aValue = 100;
		Regression.verify(sub.aValue == 100);
		Regression.verify(sub.aAttr == 999);
		Regression.verify(aStaticDims == 56);
		
		sub.aAttr = 200;
		Regression.verify(sub.aValue == 100);
		Regression.verify(sub.aAttr == 200);
		Regression.verify(aStaticDims == 56);
		
		aStaticDims = 12345;
		Regression.verify(sub.aValue == 100);
		Regression.verify(sub.aAttr == 200);
		Regression.verify(aStaticDims == 12345);
	}

	public static void main() {
		test1();
	}
}
