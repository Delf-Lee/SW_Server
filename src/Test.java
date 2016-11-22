
public class Test {
public static void main(String[] args) {
	String msg = "123 234";
	String splitMsg[];
	msg = msg.trim();
	splitMsg = msg.split(" ");
	System.out.println(splitMsg[1]);
}
}
