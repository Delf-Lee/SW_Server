import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
/** @author delf */
public class PlayerHandler extends Thread {
	private Socket socket;
	private int id;
	private String test = "test" + id;

	// private String nickname;
	private PlayerHandler[] players = new PlayerHandler[Server.MAX_PLAYER];
	private DataInputStream dis;
	private DataOutputStream dos;

	@Override
	public void run() {
		while (true) {
			try {
				readMeg();
			} catch (IOException e) { // 클라이언트와 연결이 끊기면 예외 발생
				try {
					exitPlayer();
					return;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.out.println("error occured in read message");
				// e.printStackTrace();
			}
		}
	}

	/** 서버가 관리하는 클라이언트의 배열에서 종료된 클라이언트를 삭제한다. */
	public void exitPlayer() throws IOException {
		dos.close();
		dis.close();
		socket.close();
		// delf: 클라이언트 삭제루틴
		if (id == Server.P2) {
			players[Server.P2] = players[Server.P1];
			players[Server.P2] = null;
		}
		players[Server.P1] = null;
		Server.playersInServer--;
	}

	/** 생성자
	 * @param socket 클라이언트와 연결된 소켓
	 * @param id 클라이언트의 id. 1p는 0, 2p는 1로 한다. */
	public PlayerHandler(Socket socket, int id, PlayerHandler[] players) {
		this.socket = socket;
		this.id = id;
		// this.nickname = nickname;
		setHandler();
		start(); // delf: 서버 스레드 시작
		sendMsg(G.MYID + " " + id); // delf: 배정된 아이디 전송
	}

	/** 플레이어가 모두 갖춰지면, 모든 플레이어에게 플레이어 핸들러를 알려준다.
	 * @param player 플레이어 핸들러의 배열 */
	public void setPlayers(PlayerHandler[] players) {
		this.players = players; // delf: 다른 클라이언트에게 broadcasting하기 위한 플레이어 array
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

	/** 클라이언트로 부터 메시지를 읽어들이고 그 값을 반환한다.
	 * @return 클라이어언트로 부터 수신한 메시지 문자열
	 * @exception IOException 메시지 수신에 실패하거나 클라이언트와 접속이 끊기면 예외 발생 */
	private String readMeg() throws IOException {
		byte[] buffer = new byte[128];
		String msg;
		// String splitMsg[];
		dis.read(buffer); // 대기
		msg = new String(buffer);
		broadcastingMsg(msg); // delf: 임시
		return msg;
	}

	/** 모든 클라이언트들에게 메시지를 전송한다.
	 * @param str 전송할 메시지 문자열 */
	private void broadcastingMsg(String str) {
		for (int i = 0; i < Server.playersInServer; i++) {
			players[i].sendMsg(str);
		}
		System.out.println("[" + str   .trim() + "]보냄");
	}

	/** 해당 클라이언트에게만 메시지 전송
	 * @param str 전송할 메시지 문자열 */
	public void sendMsg(String str) {
		try {
			byte[] bb;
			bb = str.getBytes();
			dos.write(bb);
		} catch (IOException e) {
			System.out.println("error: send mesage");
			System.out.println(test);
			e.printStackTrace();
		}
	}
}