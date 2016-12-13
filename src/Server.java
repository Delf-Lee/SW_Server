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
	public static int playersInServer = 0; // ���� �� Ŭ���̾�Ʈ ��

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
	private boolean ready[] = { false, false }; // �� Ŭ���̾�Ʈ�� �غ� ����
	private int testCnt[] = { 0, 0 }; // �ۼ��� �ð� �׽�Ʈ Ƚ�� (��� ���ϱ� ����)
	private long testTotalSec[] = { 0, 0 }; // �ۼ��� �ð� ��

	/** ������ */
	public Server() {
		try {
			rcvSocket = new DatagramSocket(RECEIVEPORT); // ���ſ� ����
		} catch (SocketException e) {
			e.printStackTrace();
		}
		start(); // ���Ž����� ����
		System.out.println("���� ���Ž����� ���� (port: " + RECEIVEPORT + ")");
	}

	@Override
	public void run() { // delf: ������ ������ ���� ����
		bb = new byte[BUFSIZE];
		DatagramPacket rcvPacket = new DatagramPacket(bb, bb.length);
		while (true) {
			try {
				rcvSocket.receive(rcvPacket); // ������ ���� ��
				handlingMsg(rcvPacket); // ���� �޽��� ó��
				initByte(bb);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** ������ �÷��̾� �߰�.
	 * @param sndSocket �÷��̾��� ���� ��ȣ */
	private boolean addPlayer(InetAddress ipAddr, int port) throws IOException {
		if (playersInServer < MAX_PLAYER) { // �ִ� �ο����� ���ٸ� �߰����� ����
			System.out.print("���ο� palyer�� �߰���, count = " + playersInServer);
			player[playersInServer] = new PlayerHandler(ipAddr, playersInServer, port);
			playersInServer++;
			System.out.println(" -> " + playersInServer);
			return true;
		}
		sndSocket.close(); // �� �ȹ޾�
		return false;
	}

	private void broadcasting(byte[] sb) {
		broadcasting(sb, "");
	}

	/** �÷��̾�鿡�� packet ����
	 * @param sb sendByte - ������ ����Ʈ */
	private void broadcasting(byte[] sb, String tail) {
		try {
			System.out.println("tail = " + tail);
			String test = (new String(sb)).trim() + " " + tail; // ���ڿ� ����, + ��� �ۼ��� �ð� ÷��
			sb = test.getBytes(); // byte ����
			System.out.print("player[");
			for (int i = 0; i < Server.playersInServer; i++) {
				// ��Ŷ ����
				DatagramPacket sendPacket = new DatagramPacket(sb, sb.length, player[i].getIpAddr(), player[i].getPort());
				rcvSocket.send(sendPacket); // ����
				System.out.print(i + " ");
			}
			System.out.println("] ���� �޽���: " + test);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Ŭ���̾�Ʈ�� ���� ���� �����͸� ó����
	 * @param packet ���� ���� packet
	 * @throws IOException */
	private void handlingMsg(DatagramPacket packet) throws IOException {
		String msg = new String(bb).trim();
		String splitMsg[];
		int id;
		splitMsg = msg.split(G.BLANK);
		try {
			id = Integer.parseInt(splitMsg[ID]);
		} catch (ArrayIndexOutOfBoundsException e) {
			id = 0; // ���� ����
		}
		if (!splitMsg[CMD].equals("/Test")) {
			System.out.println("receive msg from client = " + msg);
		}

		switch (splitMsg[CMD]) {

		case G.ACCESS:
			int idNow = playersInServer;
			addPlayer(packet.getAddress(), packet.getPort());
			System.out.println("�÷��̾� �߰�. ip = " + packet.getAddress());
			String reply = createMsg(G.ACCESS, idNow + "");
			sendMsg(player[idNow], reply);

			if (idNow == G.P2) { // ��� �����ϸ�
				System.out.println("�ð���������");
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
				System.out.println("�Ѵ� �غ��");
				byte[] bb = new byte[BUFSIZE];
				bb = (G.READY + " 0").getBytes();
				broadcasting(bb);
			}
			break;
		case "/Test":
			int target = Integer.parseInt(splitMsg[ID]); // �÷��̾� id
			long interval = Long.parseLong(splitMsg[2]); // �ð�

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

	/** �Է� ���� �Ķ���͵�� �������� �������� �����.
	 * @param par ���������� ����� ��� ���ڿ�
	 * @author delf */
	public static String createMsg(String... par) {
		String msg = "";
		for (int i = 0; i < par.length - 1; i++) {
			msg += par[i] + G.BLANK;
		}
		msg += par[par.length - 1];
		return msg;
	}

	/** Ŭ���̾�Ʈ���� �޽����� �����Ѵ�(��Ŷ).
	 * @param id ������ Ŭ���̾�Ʈ�� id.
	 * @param sendPacket ������ �޽��� ��Ŷ */
	private void sendMsg(int id, DatagramPacket sendPacket) {
		try {
			sendPacket.setAddress(player[id].getIpAddr());
			sendPacket.setPort(SENDPORT);
			sndSocket.send(sendPacket); // ����
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Ŭ���̾�Ʈ���� �޽����� �����Ѵ�(���ڿ�).
	 * @param id ������ Ŭ���̾�Ʈ�� id.
	 * @param msg ������ �޽��� ���ڿ�. */
	private void sendMsg(PlayerHandler player, String msg) {
		// �����, �α�
		System.out.println("TO[" + player.getPlayerId() + "]" + "send message(port: " + player.getPort() + "): " + msg);
		// ����
		try {
			byte[] msgByte = new byte[128];
			msgByte = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(msgByte, msgByte.length, player.getIpAddr(), player.getPort());
			rcvSocket.send(packet); // ����
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** ������ ��Ʈ�� �����Ѵ�.
	 * @param port ���ۼ��Ͽ� �Ҵ�� ����Ʈ��ȣ ���ż����� ��Ʈ��ȣ�� port+1�� �Ҵ�ȴ�. */
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
							tmp = sendMsg.getBytes(); // ����
							// ��Ŷ ����
							DatagramPacket sendPacket = new DatagramPacket(tmp, tmp.length, player[id].getIpAddr(), player[id].getPort());
							rcvSocket.send(sendPacket); // ����
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