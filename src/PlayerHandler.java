import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/** @author delf */
public class PlayerHandler {

	private InetAddress ipAddr = InetAddress.getLoopbackAddress();
	private int port;
	private int id;

	private PlayerHandler[] players = new PlayerHandler[Server.MAX_PLAYER];
	public final static int BUFSIZE = 128;

	private byte[] bb = new byte[BUFSIZE]; // byte ����
	private DatagramPacket udp_packet = new DatagramPacket(bb, bb.length); // ������ ��Ŷ

	/** ������
	 * @param socket Ŭ���̾�Ʈ�� ����� ����
	 * @param id Ŭ���̾�Ʈ�� id. 1p�� 0, 2p�� 1�� �Ѵ�. */
	public PlayerHandler(InetAddress ipAddr, int id, PlayerHandler[] players) { // ���� �Ⱦ� this.port = port;
		this.ipAddr = ipAddr;
		this.id = id;
	}

	public PlayerHandler(InetAddress ipAddr, int id, int port) { // ���� �Ⱦ� this.port = port;
		this.ipAddr = ipAddr;
		this.id = id;
		this.port = port;
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

	/** �÷��̾ ��� ��������, ��� �÷��̾�� �÷��̾� �ڵ鷯�� �˷��ش�.
	 * @param player �÷��̾� �ڵ鷯�� �迭 */
	public void setPlayers(PlayerHandler[] players) {
		this.players = players; // delf: �ٸ� Ŭ���̾�Ʈ���� broadcasting�ϱ� ���� �÷��̾� array
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