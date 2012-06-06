package thinj.regression.nativetest;

public class ReverseNativeInstanceTest {
	private static ReverseNativeInstanceTest aInstance;
	public native void foo();

	public void bar() {
		System.out.println("bar called: " + (aInstance == this));
	}
	/*
	mangler følgende:
		3) Instruktioner skal angive hvilke exception, de afhænger af => inkluderingen af 
		   exception kode kan gøres betinget.
		4) Exception (default) constructors skal kaldes, når f.eks. en NPE kastes
		5) Native kode, der kalder interface metoder - også i andre klasser end context.classId
		   skal testes. 
		6) CodeGenerator skal kunne holde til, at NPE og andre Exceptions ikke forekommer (men
		   skal stadig sætte værdien af classId - variablen til 0, da c-koden vil linke til variablen.
		7) Teste at 'this' i 'bar' - metoden == 'this' i den kaldende instance 
		9) Prerequisite dependencies skal 'handleReferences' på samme måde 
		
	*/	   
	public static void main() {
		ReverseNativeInstanceTest x = new ReverseNativeInstanceTest();
		aInstance = x;
		//x.bar();
		x.foo();
		
		System.out.println("foobar");
	}
}
