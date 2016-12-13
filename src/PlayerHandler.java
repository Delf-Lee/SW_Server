import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/** @author delf */
public class PlayerHandler extends Thread {

	private InetAddress ipAddr = InetAddress.getLoopbackAddress();
	private int port;
	private int id;

	// delf: udp 소켓, 생성은 생성자에서
	//private DatagramSocket socket = null;

	private PlayerHandler[] players = new PlayerHandler[Server.MAX_PLAYER];
	public final static int BUFSIZE = 128;

	// delf: udp를 사용하기 위한 맴버
	private byte[] bb = new byte[BUFSIZE]; // byte 전송
	private DatagramPacket udp_packet = new DatagramPacket(bb, bb.length); // 전송할 패킷

	/** 생성자
	 * @param socket 클라이언트와 연결된 소켓
	 * @param id 클라이언트의 id. 1p는 0, 2p는 1로 한다. */
	public PlayerHandler(InetAddress ipAddr, int id, PlayerHandler[] players) { // 소켓 안씀 this.port = port;
		this.ipAddr = ipAddr;
		//this.port = port;
//		try {
//			socket = new DatagramSocket(port);
//		} catch (SocketException e) {
//			e.printStackTrace();
//		}
		this.id = id;
		//start(); // delf: 서버 스레드 시작
		// sendMsg(G.MYID + " " + id); // delf: 배정된 아이디 전송
	}
	
	public PlayerHandler(InetAddress ipAddr, int id, int port) { // 소켓 안씀 this.port = port;
		this.ipAddr = ipAddr;
		this.id = id;
		this.port = port;
	}

	/** {@link PlayerHandler}가 수행하는 핵심 로직
	 * @author delf */
	@Override
	public void run() {
//		while (true) {
//			try {
//				socket.receive(udp_packet); // delf :메시지 수신
//				broadcastingMsg(udp_packet); // delf: 자신을 포함한 접속 된 클라이언트에게 broadcasting
//			} catch (IOException e) {
//				// TODO: 예외발생...? UDP에서는 언제 예외 던지지? // 교수님께 질문
//				try {
//					exitPlayer();
//					return;
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
//				System.out.println("error occured in read message");
//				// e.printStackTrace();
//			}
//		}
	}

	/** 서버가 관리하는 클라이언트의 배열에서 종료된 클라이언트를 삭제한다. */
	public void exitPlayer() throws IOException {
		// delf: 클라이언트 삭제루틴
		if (id == Server.P2) {
			players[Server.P2] = players[Server.P1];
			players[Server.P2] = null;
		}
		players[Server.P1] = null;
		Server.playersInServer--;
	}

	// /** 생성자
	// * @param socket 클라이언트와 연결된 소켓
	// * @param id 클라이언트의 id. 1p는 0, 2p는 1로 한다. */
	// public PlayerHandler(Socket socket, int id, PlayerHandler[] players) {
	// this.socket = socket;
	// this.id = id;
	// // this.nickname = nickname;
	// setHandler();
	// start(); // delf: 서버 스레드 시작
	// sendMsg(G.MYID + " " + id); // delf: 배정된 아이디 전송
	// }

	/** 플레이어가 모두 갖춰지면, 모든 플레이어에게 플레이어 핸들러를 알려준다.
	 * @param player 플레이어 핸들러의 배열 */
	public void setPlayers(PlayerHandler[] players) {
		this.players = players; // delf: 다른 클라이언트에게 broadcasting하기 위한 플레이어 array
	}

	/** 클라이언트로 부터 메시지를 읽어들이고 그 값을 반환한다.
	 * @return 클라이어언트로 부터 수신한 메시지 문자열
	 * @exception IOException 메시지 수신에 실패하거나 클라이언트와 접속이 끊기면 예외 발생 */
	// private String readMeg() throws IOException {
	// byte[] buffer = new byte[128];
	// String msg;
	// // String splitMsg[];
	// dis.read(buffer); // 대기
	// msg = new String(buffer);
	// broadcastingMsg(msg); // delf: 임시
	// return msg;
	// }

	/** 모든 클라이언트들에게 메시지를 전송한다.
	 * @param str 전송할 메시지 문자열 */
	// private void broadcastingMsg(String str) {
	// for (int i = 0; i < Server.playersInServer; i++) {
	// players[i].sendMsg(str);
	// }
	// System.out.println("[" + str.trim() + "]보냄");
	// }

//	private void broadcastingMsg(DatagramPacket packet) { // delf: 뿌리기
//		for (int i = 0; i < Server.playersInServer; i++) {
//			players[i].sendMsg(packet);
//		}
//	}

	/** 해당 클라이언트에게만 메시지 전송
	 * @param str 전송할 메시지 문자열
	 * @author delf */
//	public void sendMsg(String str) {
//		try {
//			byte[] msgByte = new byte[128];
//			msgByte = str.getBytes();
//			DatagramPacket packet = new DatagramPacket(msgByte, msgByte.length, ipAddr, port);
//			socket.send(packet); // 전송
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	/** @author delf 테스트 메소드 DatagramPacket 전송 */
//	public void sendMsg(DatagramPacket packet) {
//		try {
//			packet.setAddress(ipAddr);
//			packet.setPort(port);
//			socket.send(packet); // 전송
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	public void handlingMsg(DatagramPacket packet) {
		String msg = bb.toString();
		msg = msg.trim(); // delf: 메시지에서 공백 제거
		String splitMsg[];
		splitMsg = msg.split(G.BLANK); // delf: 빈칸을 기준으로 나누어 담는다.
	}

	public void setIpAddr(InetAddress ipAddr) {
		this.ipAddr = ipAddr;
	}

	public InetAddress getIpAddr() {
		return ipAddr;
	}
	
	public int getPort() {
		return port;
	}
	
	public int getPlayerId() {
		return id;
	}
}