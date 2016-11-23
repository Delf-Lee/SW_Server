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
			} catch (IOException e) { // Ŭ���̾�Ʈ�� ������ ����� ���� �߻�
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

	/** ������ �����ϴ� Ŭ���̾�Ʈ�� �迭���� ����� Ŭ���̾�Ʈ�� �����Ѵ�. */
	public void exitPlayer() throws IOException {
		dos.close();
		dis.close();
		socket.close();
		// delf: Ŭ���̾�Ʈ ������ƾ
		if (id == Server.P2) {
			players[Server.P2] = players[Server.P1];
			players[Server.P2] = null;
		}
		players[Server.P1] = null;
		Server.playersInServer--;
	}

	/** ������
	 * @param socket Ŭ���̾�Ʈ�� ����� ����
	 * @param id Ŭ���̾�Ʈ�� id. 1p�� 0, 2p�� 1�� �Ѵ�. */
	public PlayerHandler(Socket socket, int id, PlayerHandler[] players) {
		this.socket = socket;
		this.id = id;
		// this.nickname = nickname;
		setHandler();
		start(); // delf: ���� ������ ����
		sendMsg(G.MYID + " " + id); // delf: ������ ���̵� ����
	}

	/** �÷��̾ ��� ��������, ��� �÷��̾�� �÷��̾� �ڵ鷯�� �˷��ش�.
	 * @param player �÷��̾� �ڵ鷯�� �迭 */
	public void setPlayers(PlayerHandler[] players) {
		this.players = players; // delf: �ٸ� Ŭ���̾�Ʈ���� broadcasting�ϱ� ���� �÷��̾� array
	}

	/** ��Ʈ�� ���� */
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

	/** Ŭ���̾�Ʈ�� ���� �޽����� �о���̰� �� ���� ��ȯ�Ѵ�.
	 * @return Ŭ���̾��Ʈ�� ���� ������ �޽��� ���ڿ�
	 * @exception IOException �޽��� ���ſ� �����ϰų� Ŭ���̾�Ʈ�� ������ ����� ���� �߻� */
	private String readMeg() throws IOException {
		byte[] buffer = new byte[128];
		String msg;
		// String splitMsg[];
		dis.read(buffer); // ���
		msg = new String(buffer);
		broadcastingMsg(msg); // delf: �ӽ�
		return msg;
	}

	/** ��� Ŭ���̾�Ʈ�鿡�� �޽����� �����Ѵ�.
	 * @param str ������ �޽��� ���ڿ� */
	private void broadcastingMsg(String str) {
		for (int i = 0; i < Server.playersInServer; i++) {
			players[i].sendMsg(str);
		}
		System.out.println("[" + str   .trim() + "]����");
	}

	/** �ش� Ŭ���̾�Ʈ���Ը� �޽��� ����
	 * @param str ������ �޽��� ���ڿ� */
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