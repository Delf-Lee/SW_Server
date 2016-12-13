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

	// delf: udp ����, ������ �����ڿ���
	//private DatagramSocket socket = null;

	private PlayerHandler[] players = new PlayerHandler[Server.MAX_PLAYER];
	public final static int BUFSIZE = 128;

	// delf: udp�� ����ϱ� ���� �ɹ�
	private byte[] bb = new byte[BUFSIZE]; // byte ����
	private DatagramPacket udp_packet = new DatagramPacket(bb, bb.length); // ������ ��Ŷ

	/** ������
	 * @param socket Ŭ���̾�Ʈ�� ����� ����
	 * @param id Ŭ���̾�Ʈ�� id. 1p�� 0, 2p�� 1�� �Ѵ�. */
	public PlayerHandler(InetAddress ipAddr, int id, PlayerHandler[] players) { // ���� �Ⱦ� this.port = port;
		this.ipAddr = ipAddr;
		//this.port = port;
//		try {
//			socket = new DatagramSocket(port);
//		} catch (SocketException e) {
//			e.printStackTrace();
//		}
		this.id = id;
		//start(); // delf: ���� ������ ����
		// sendMsg(G.MYID + " " + id); // delf: ������ ���̵� ����
	}
	
	public PlayerHandler(InetAddress ipAddr, int id, int port) { // ���� �Ⱦ� this.port = port;
		this.ipAddr = ipAddr;
		this.id = id;
		this.port = port;
	}

	/** {@link PlayerHandler}�� �����ϴ� �ٽ� ����
	 * @author delf */
	@Override
	public void run() {
//		while (true) {
//			try {
//				socket.receive(udp_packet); // delf :�޽��� ����
//				broadcastingMsg(udp_packet); // delf: �ڽ��� ������ ���� �� Ŭ���̾�Ʈ���� broadcasting
//			} catch (IOException e) {
//				// TODO: ���ܹ߻�...? UDP������ ���� ���� ������? // �����Բ� ����
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

	/** ������ �����ϴ� Ŭ���̾�Ʈ�� �迭���� ����� Ŭ���̾�Ʈ�� �����Ѵ�. */
	public void exitPlayer() throws IOException {
		// delf: Ŭ���̾�Ʈ ������ƾ
		if (id == Server.P2) {
			players[Server.P2] = players[Server.P1];
			players[Server.P2] = null;
		}
		players[Server.P1] = null;
		Server.playersInServer--;
	}

	// /** ������
	// * @param socket Ŭ���̾�Ʈ�� ����� ����
	// * @param id Ŭ���̾�Ʈ�� id. 1p�� 0, 2p�� 1�� �Ѵ�. */
	// public PlayerHandler(Socket socket, int id, PlayerHandler[] players) {
	// this.socket = socket;
	// this.id = id;
	// // this.nickname = nickname;
	// setHandler();
	// start(); // delf: ���� ������ ����
	// sendMsg(G.MYID + " " + id); // delf: ������ ���̵� ����
	// }

	/** �÷��̾ ��� ��������, ��� �÷��̾�� �÷��̾� �ڵ鷯�� �˷��ش�.
	 * @param player �÷��̾� �ڵ鷯�� �迭 */
	public void setPlayers(PlayerHandler[] players) {
		this.players = players; // delf: �ٸ� Ŭ���̾�Ʈ���� broadcasting�ϱ� ���� �÷��̾� array
	}

	/** Ŭ���̾�Ʈ�� ���� �޽����� �о���̰� �� ���� ��ȯ�Ѵ�.
	 * @return Ŭ���̾��Ʈ�� ���� ������ �޽��� ���ڿ�
	 * @exception IOException �޽��� ���ſ� �����ϰų� Ŭ���̾�Ʈ�� ������ ����� ���� �߻� */
	// private String readMeg() throws IOException {
	// byte[] buffer = new byte[128];
	// String msg;
	// // String splitMsg[];
	// dis.read(buffer); // ���
	// msg = new String(buffer);
	// broadcastingMsg(msg); // delf: �ӽ�
	// return msg;
	// }

	/** ��� Ŭ���̾�Ʈ�鿡�� �޽����� �����Ѵ�.
	 * @param str ������ �޽��� ���ڿ� */
	// private void broadcastingMsg(String str) {
	// for (int i = 0; i < Server.playersInServer; i++) {
	// players[i].sendMsg(str);
	// }
	// System.out.println("[" + str.trim() + "]����");
	// }

//	private void broadcastingMsg(DatagramPacket packet) { // delf: �Ѹ���
//		for (int i = 0; i < Server.playersInServer; i++) {
//			players[i].sendMsg(packet);
//		}
//	}

	/** �ش� Ŭ���̾�Ʈ���Ը� �޽��� ����
	 * @param str ������ �޽��� ���ڿ�
	 * @author delf */
//	public void sendMsg(String str) {
//		try {
//			byte[] msgByte = new byte[128];
//			msgByte = str.getBytes();
//			DatagramPacket packet = new DatagramPacket(msgByte, msgByte.length, ipAddr, port);
//			socket.send(packet); // ����
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	/** @author delf �׽�Ʈ �޼ҵ� DatagramPacket ���� */
//	public void sendMsg(DatagramPacket packet) {
//		try {
//			packet.setAddress(ipAddr);
//			packet.setPort(port);
//			socket.send(packet); // ����
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	public void handlingMsg(DatagramPacket packet) {
		String msg = bb.toString();
		msg = msg.trim(); // delf: �޽������� ���� ����
		String splitMsg[];
		splitMsg = msg.split(G.BLANK); // delf: ��ĭ�� �������� ������ ��´�.
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