import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Calendar;

import javax.swing.text.html.HTMLDocument.HTMLReader.SpecialAction;

/** @author delf */
public class Server extends Thread {

	public final static int P1 = 0;
	public final static int P2 = 1;
	public final static int MAX_PLAYER = 2;
	public static int playersInServer = 0; // 서버 내 클라이언트 수

	private final static int BUFSIZE = 128;

	public final static int CMD = 0;
	public final static int ID = 1;
	public final static int PORT = 1;

	public static int SENDPORT;
	public static int RECEIVEPORT = 13131;

	private byte[] bb;
	private DatagramSocket sndSocket;
	private DatagramSocket rcvSocket;
	// private DatagramPacket packet;
	private PlayerHandler[] player = new PlayerHandler[MAX_PLAYER];
	private boolean ready[] = { false, false }; // 각 클라이언트의 준비 여부
	private int testCnt[] = { 0, 0 }; // 송수신 시간 테스트 횟수 (평균 구하기 위함)
	private long testTotalSec[] = { 0, 0 }; // 송수신 시간 합

	/** 생성자 */
	public Server() {
		try {
			rcvSocket = new DatagramSocket(RECEIVEPORT); // 수신용 소켓
		} catch (SocketException e) {
			e.printStackTrace();
		}
		start(); // 수신스레드 시작
		System.out.println("서버 수신스레드 시작 (port: " + RECEIVEPORT + ")");
	}

	@Override
	public void run() { // delf: 데이터 수신을 위한 루프
		bb = new byte[BUFSIZE];
		DatagramPacket rcvPacket = new DatagramPacket(bb, bb.length);
		while (true) {
			try {
				rcvSocket.receive(rcvPacket); // 데이터 수신 부
				handlingMsg(rcvPacket); // 받은 메시지 처리
				initByte(bb);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** 서버에 플레이어 추가.
	 * @param sndSocket 플레이어의 소켓 번호 */
	private boolean addPlayer(InetAddress ipAddr, int port) throws IOException {
		if (playersInServer < MAX_PLAYER) { // 최대 인원보다 많다면 추가하지 않음
			System.out.print("새로운 palyer가 추가됨, count = " + playersInServer);
			player[playersInServer] = new PlayerHandler(ipAddr, playersInServer, port);
			playersInServer++;
			System.out.println(" -> " + playersInServer);
			return true;
		}
		sndSocket.close(); // 응 안받아
		return false;
	}

	private void broadcasting(byte[] sb) {
		broadcasting(sb, "");
	}

	/** 플레이어들에게 packet 전송
	 * @param sb sendByte - 전송할 바이트 */
	private void broadcasting(byte[] sb, String tail) {
		try {
			System.out.println("tail = " + tail);
			String test = (new String(sb)).trim() + " " + tail; // 문자열 정리, + 평균 송수신 시간 첨가
			sb = test.getBytes(); // byte 추출
			System.out.print("player[");
			for (int i = 0; i < Server.playersInServer; i++) {
				// 패킷 생성
				DatagramPacket sendPacket = new DatagramPacket(sb, sb.length, player[i].getIpAddr(), player[i].getPort());
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
	private void handlingMsg(DatagramPacket packet) throws IOException {
		String msg = new String(bb).trim();
		String splitMsg[];
		int id;
		splitMsg = msg.split(G.BLANK);
		try {
			id = Integer.parseInt(splitMsg[ID]);
		} catch (ArrayIndexOutOfBoundsException e) {
			id = 0; // 올일 없음
		}
		if (!splitMsg[CMD].equals("/Test")) {
			System.out.println("receive msg from client = " + msg);
		}

		switch (splitMsg[CMD]) {

		case G.ACCESS:
			int idNow = playersInServer;
			addPlayer(packet.getAddress(), packet.getPort());
			System.out.println("플레이어 추가. ip = " + packet.getAddress());
			String reply = createMsg(G.ACCESS, idNow + "");
			sendMsg(player[idNow], reply);

			if (idNow == G.P2) { // 모두 접속하면
				System.out.println("시간측정시작");
				checkNetworkTime();
			}

			break;

		case G.KEY:
			String rcvMsg = new String(bb);
			int delay = getInterval(id);
			broadcasting(packet.getData(), Integer.toString(delay));
			break;

		case G.READY:
			System.out.println("ready id: " + id);
			ready[id] = true;

			if (ready[G.P1] == true && ready[G.P2] == true) {
				System.out.println("둘다 준비됨");
				byte[] bb = new byte[BUFSIZE];
				bb = (G.READY + " 0").getBytes();
				broadcasting(bb);
			}
			break;
		case "/Test":
			int target = Integer.parseInt(splitMsg[ID]); // 플레이어 id
			long interval = Long.parseLong(splitMsg[2]); // 시간

			// System.out.println("delay = " + (getNow() - interval));
			testTotalSec[target] += (getNow() - interval);
			// System.out.println("getNow() = " + getNow() + ", interval = " + interval);
			term[target] = testTotalSec[target] / (++testCnt[target]);
			System.out.println(testTotalSec[target] + "/" + testCnt[target] + "=" + term[target]);

			break;
		}
	}

	public int getInterval(int id) {
		System.out.println(term[G.P1] + "  " + term[G.P2]);
		switch (id) {
		case G.P1:
			return (int) (term[G.P1] - term[G.P2]);
		case G.P2:
			return (int) (term[G.P2] - term[G.P1]);
		}
		return -1;
	}

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
	private void sendMsg(PlayerHandler player, String msg) {
		// 디버깅, 로깅
		System.out.println("TO[" + player.getPlayerId() + "]" + "send message(port: " + player.getPort() + "): " + msg);
		// 전송
		try {
			byte[] msgByte = new byte[128];
			msgByte = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(msgByte, msgByte.length, player.getIpAddr(), player.getPort());
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

	private void initByte(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			b[i] = 0;
		}
	}

	public static void main(String[] args) {
		new Server();
	}

	private long depart[] = { 0, 0 };
	private long term[] = { 0, 0 };

	public void setCriterion(int id) {
		depart[id] = System.currentTimeMillis();
	}

	public long getNow() {
		return System.currentTimeMillis();
	}

	public long getTerm(int id) {
		return term[id];
	}

	public void checkNetworkTime() {
		String test[] = { "/Test 0", "/Test 1" };
		new Thread(new Runnable() {
			String sendMsg = "";

			@Override
			public void run() {
				int cnt = 0;
				while (true) {
					for (int id = 0; id <= G.P2; id++) {
						try {
							sendMsg = test[id] + " " + getNow() + " " + cnt;
							System.out.println(sendMsg);
							byte tmp[] = new byte[128];
							tmp = sendMsg.getBytes(); // 전송
							// 패킷 생성
							DatagramPacket sendPacket = new DatagramPacket(tmp, tmp.length, player[id].getIpAddr(), player[id].getPort());
							rcvSocket.send(sendPacket); // 전송
							sleep(10);
							// sendMsg = "";
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						cnt++;
					}
				}
			}
		}).start();
	}
}