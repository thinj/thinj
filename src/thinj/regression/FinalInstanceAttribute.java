package thinj.regression;

public class FinalInstanceAttribute {
	//private final int aClassId = 45;
	private int aClassId = 45;


	public static void main() {
		FinalInstanceAttribute fia = new FinalInstanceAttribute();
		Regression.verify(fia.aClassId == 45);
	}
}
