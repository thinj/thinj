package thinj.regression;

public class EqualsTest {
	public static void main() {
		EqualsTest o1 = new EqualsTest();
		EqualsTest o2 = new EqualsTest();
		
		Regression.verify(o1.equals(o1));
		Regression.verify(!o1.equals(o2));
		Regression.verify(!o1.equals(null));
	}
}
