package thinj.regression;

public class NativeTest {
	public static void main() {
		Object o = new Object();
		Regression.verify(o.hashCode() == o.hashCode());
	}
}
