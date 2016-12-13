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
	public static int playersInServer = 0; // ���� �� Ŭ���̾�Ʈ ��

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

	/** ������ */
	public Server() {
		// setSocketPort(13131);
		try {
			// sndSocket = new DatagramSocket(SENDPORT); // ���ۿ� ����
			rcvSocket = new DatagramSocket(RECEIVEPORT); // ���ſ� ����
		} catch (SocketException e) {
			e.printStackTrace();
		}
		start(); // ���Ž����� ����
		System.out.println("���� ���Ž����� ���� (port: " + RECEIVEPORT + ")");
	}

	/** ���� �����带 �����Ѵ�.
	 * @param port ������ ������ ��Ʈ��ȣ */
	public void serverStart(int port) {

	}

	@Override
	public void run() { // delf: ������ ������ ���� ����
		bb = new byte[BUFSIZE];
		DatagramPacket rcvPacket = new DatagramPacket(bb, bb.length);
		while (true) {
			try {
				rcvSocket.receive(rcvPacket); // ������ ���� ��
				handlingMsg(rcvPacket); // ���� �޽��� ó��
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** ������ �÷��̾� �߰�.
	 * @param sndSocket �÷��̾��� ���� ��ȣ */
	private boolean addPlayer(InetAddress ipAddr) throws IOException {
		if (playersInServer < MAX_PLAYER) { // �ִ� �ο����� ���ٸ� �߰����� ����
			System.out.print("���ο� palyer�� �߰���, count = " + playersInServer);
			player[playersInServer] = new PlayerHandler(ipAddr, playersInServer, player);
			playersInServer++;
			System.out.println(" -> " + playersInServer);
			// for (int i = 0; i < playersInServer; i++) {
			// player[i].setPlayers(player);
			// }
			return true;
		}
		sndSocket.close(); // �� �ȹ޾�
		return false;
	}

	/** �÷��̾�鿡�� packet ����
	 * @param sb sendByte - ������ ����Ʈ */
	private void broadcasting(byte[] sb) {
		try {
			String test = new String(sb);
			System.out.print("player[");
			for (int i = 0; i < Server.playersInServer; i++) {
				// ��Ŷ ����
				DatagramPacket sendPacket = new DatagramPacket(sb, sb.length, player[i].getIpAddr(), SENDPORT);
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
	private void handlingMsg(DatagramPacket packet) throws IOException { // TODO: ���߿� �������� ������� �ȿ� try/catch ����
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
			// int target = Integer.parseInt(splitMsg[ID]); // delf: �޽����� ���� Ÿ���� ����
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
	private void sendMsg(int id, String msg) {
		// �����, �α�
		System.out.println("TO[" + id + "]" + "send message(port: " + SENDPORT + "): " + msg);
		if (player[id] == null) {
			System.out.println("���ӵǾ� ���� ���� id: " + id);
			return;
		}
		// ����
		try {
			byte[] msgByte = new byte[128];
			msgByte = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(msgByte, msgByte.length, player[id].getIpAddr(), SENDPORT);
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

	public static void main(String[] args) {
		new Server();
	}
}
