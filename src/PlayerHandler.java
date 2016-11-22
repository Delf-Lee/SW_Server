import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PlayerHandler extends Thread {
	private Socket socket;
	private int id;
	// private String nickname;

	private PlayerHandler[] players;// = new PlayerHandler[Server.MAX_PLAYER];
	private DataInputStream dis;
	private DataOutputStream dos;

	@Override
	public void run() {
		try {
			readMeg();
		} catch (IOException e) {
			System.out.println("error occured in read message");
			e.printStackTrace();
		}
	}

	public PlayerHandler(Socket socket, int id) {
		this.socket = socket;
		this.id = id;
		// this.nickname = nickname;
		setHandler();
	}

	public void setPlayers(PlayerHandler[] players) {
		this.players = players;
	}

	/** 스트림 생성 */
	private void setHandler() {
		try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			String err = "stream setting error";
			System.out.println(err);
			e.printStackTrace();
		}
	}

	private String readMeg() throws IOException {
		byte[] buffer = new byte[128];
		String msg;
		String splitMsg[];
		dis.read(buffer); // 대기
		msg = new String(buffer);
		System.out.println("p" + id + " 받은 메세지: " + msg);

		return msg;
		// msg = msg.trim();
		// splitMsg = msg.split(" ");
		// return null;
	}

	private void broadcastingMsg(String str) {
		for (int i = 0; i < players.length; i++) {
			try {
	            byte[] bb;
	            bb = str.getBytes();
	            dos.write(bb);
	        } catch (IOException e) {
	        	System.out.println("error: send mesage");
	        	e.printStackTrace();
	        }
		}
	}

}