
public class Test {
	
	public static void tmp(String... t) {
		
		System.out.println("cnt = " + t.length);
		for (int i = 0; i < t.length; i++) {
			System.out.println(t[i]);
		}
	}
	
	public static void main(String[] args) {
		tmp("11", "22", "33");
	}

	
}
