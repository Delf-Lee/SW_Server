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

	private byte[] bb = new byte[BUFSIZE]; // byte 전송
	private DatagramPacket udp_packet = new DatagramPacket(bb, bb.length); // 전송할 패킷

	/** 생성자
	 * @param socket 클라이언트와 연결된 소켓
	 * @param id 클라이언트의 id. 1p는 0, 2p는 1로 한다. */
	public PlayerHandler(InetAddress ipAddr, int id, PlayerHandler[] players) { // 소켓 안씀 this.port = port;
		this.ipAddr = ipAddr;
		this.id = id;
	}

	public PlayerHandler(InetAddress ipAddr, int id, int port) { // 소켓 안씀 this.port = port;
		this.ipAddr = ipAddr;
		this.id = id;
		this.port = port;
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

	/** 플레이어가 모두 갖춰지면, 모든 플레이어에게 플레이어 핸들러를 알려준다.
	 * @param player 플레이어 핸들러의 배열 */
	public void setPlayers(PlayerHandler[] players) {
		this.players = players; // delf: 다른 클라이언트에게 broadcasting하기 위한 플레이어 array
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