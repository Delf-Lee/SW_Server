import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.format.ResolverStyle;

/** @author delf */
public class Server extends Thread {

	public final static int P1 = 0;
	public final static int P2 = 1;
	public final static int MAX_PLAYER = 2;
	public static int playersInServer = 0; // 서버 내 클라이언트 수

	private final static int BUFSIZE = 128;
	public final static int CMD = 0;
	public final static int ID = 1;

	public static int SENDPORT;
	public static int RECEIVEPORT = 13131;

	private byte[] bb;
	private DatagramSocket sndSocket;
	private DatagramSocket rcvSocket;
	// private DatagramPacket packet;

	private PlayerHandler[] player = new PlayerHandler[MAX_PLAYER];

	/** 생성자 */
	public Server() {
		// setSocketPort(13131);
		try {
			// sndSocket = new DatagramSocket(SENDPORT); // 전송용 소켓
			rcvSocket = new DatagramSocket(RECEIVEPORT); // 수신용 소켓
		} catch (SocketException e) {
			e.printStackTrace();
		}
		start(); // 수신스레드 시작
		System.out.println("서버 수신스레드 시작 (port: " + RECEIVEPORT + ")");
	}

	/** 서버 스레드를 시작한다.
	 * @param port 서버를 실행할 포트번호 */
	public void serverStart(int port) {

	}

	@Override
	public void run() { // delf: 데이터 수신을 위한 루프
		bb = new byte[BUFSIZE];
		DatagramPacket rcvPacket = new DatagramPacket(bb, bb.length);
		while (true) {
			try {
				rcvSocket.receive(rcvPacket); // 데이터 수신 부
				handlingMsg(rcvPacket); // 받은 메시지 처리
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** 서버에 플레이어 추가.
	 * @param sndSocket 플레이어의 소켓 번호 */
	private boolean addPlayer(InetAddress ipAddr) throws IOException {
		if (playersInServer < MAX_PLAYER) { // 최대 인원보다 많다면 추가하지 않음
			System.out.print("새로운 palyer가 추가됨, count = " + playersInServer);
			player[playersInServer] = new PlayerHandler(ipAddr, playersInServer, player);
			playersInServer++;
			System.out.println(" -> " + playersInServer);
			// for (int i = 0; i < playersInServer; i++) {
			// player[i].setPlayers(player);
			// }
			return true;
		}
		sndSocket.close(); // 응 안받아
		return false;
	}

	/** 플레이어들에게 packet 전송
	 * @param sb sendByte - 전송할 바이트 */
	private void broadcasting(byte[] sb) {
		try {
			String test = new String(sb);
			System.out.print("player[");
			for (int i = 0; i < Server.playersInServer; i++) {
				// 패킷 생성
				DatagramPacket sendPacket = new DatagramPacket(sb, sb.length, player[i].getIpAddr(), SENDPORT);
				rcvSocket.send(sendPacket); // 전송
				System.out.print(i + " ");
			}
			System.out.println("] 보낸 메시지: " + test);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 클라이언트로 부터 받은 데이터를 처리함
	 * @param packet 수신 받은 packet
	 * @throws IOException */
	private void handlingMsg(DatagramPacket packet) throws IOException { // TODO: 나중에 프로토콜 더생기면 안에 try/catch 생성
		String msg = new String(bb).trim();
		String splitMsg[];
		splitMsg = msg.split(G.BLANK);

		System.out.println("handlingMsg = " + msg);

		switch (splitMsg[CMD]) {

		case G.ACCESS:
			int idNow = playersInServer;
			addPlayer(packet.getAddress());
			String reply = createMsg(G.ACCESS, " " + idNow);
			sendMsg(idNow, reply);
			break;

		case G.KEY:
			// int target = Integer.parseInt(splitMsg[ID]); // delf: 메시지에 대한 타겟을 저장
			broadcasting(packet.getData());
			break;
		}
	}

	// public void notifyId() {
	// }

	// private DatagramPacket createPacket(String msg) {
	// bb = msg.getBytes();
	// return (new DatagramPacket(bb, bb.length));
	// }

	/** 입력 받은 파라미터들로 프로토콜 형식으로 만든다.
	 * @param par 프로토콜을 만드는 요소 문자열
	 * @author delf */
	public static String createMsg(String... par) {
		String msg = "";
		for (int i = 0; i < par.length - 1; i++) {
			msg += par[i] + G.BLANK;
		}
		msg += par[par.length - 1];
		return msg;
	}

	/** 클라이언트에게 메시지를 전송한다(패킷).
	 * @param id 전송할 클라이언트의 id.
	 * @param sendPacket 전송할 메시지 패킷 */
	private void sendMsg(int id, DatagramPacket sendPacket) {
		try {
			sendPacket.setAddress(player[id].getIpAddr());
			sendPacket.setPort(SENDPORT);
			sndSocket.send(sendPacket); // 전송
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 클라이언트에게 메시지를 전송한다(문자열).
	 * @param id 전송할 클라이언트의 id.
	 * @param msg 전송할 메시지 문자열. */
	private void sendMsg(int id, String msg) {
		// 디버깅, 로깅
		System.out.println("TO[" + id + "]" + "send message(port: " + SENDPORT + "): " + msg);
		if (player[id] == null) {
			System.out.println("접속되어 있지 않은 id: " + id);
			return;
		}
		// 전송
		try {
			byte[] msgByte = new byte[128];
			msgByte = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(msgByte, msgByte.length, player[id].getIpAddr(), SENDPORT);
			rcvSocket.send(packet); // 전송
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 소켓의 포트를 설정한다.
	 * @param port 전송소켓에 할당될 의포트번호 수신소켓의 포트번호는 port+1로 할당된다. */
	private void setSocketPort(int port) {
		SENDPORT = port;
		RECEIVEPORT = SENDPORT + 1;
	}

	public static void main(String[] args) {
		new Server();
	}
}
