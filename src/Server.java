import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

	public final static int P1 = 0;
	public final static int P2 = 1;
	public final static int MAX_PLAYER = 2;
	public static int playersInServer = 0;
	private ServerSocket serverSocket; // ��������
	private Socket socket; // �������
	private int port; // ��Ʈ��ȣ

	private PlayerHandler[] player = new PlayerHandler[MAX_PLAYER];

	@Override
	public void run() {
		while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
			try {
				System.out.print("���� ��� ��...");
				socket = serverSocket.accept(); // accept�� �Ͼ�� �������� ���� �����
				System.out.println("�÷��̾� ����");
				addPlayer(socket);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("accept error");
				// ���⼭ ������ ����� Ŭ���̾�Ʈ
			}
		}
	}

	/** ������ �÷��̾� �߰�.	
	 * @param socket �÷��̾��� ���� ��ȣ */
	private boolean addPlayer(Socket socket) {
		if(playersInServer < MAX_PLAYER) {
			player[playersInServer] = new PlayerHandler(socket, playersInServer);
			return true;
		}
		return false;
	}

	public Server() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		} // ������ ��Ʈ ���ºκ�
		serverStart(30023);
	}

	public void serverStart(int port) {
		this.port = port;
		try {
			serverSocket = new ServerSocket(port); // ������ ��Ʈ ���ºκ�
			if (serverSocket != null) { // socket �� ���������� ��������
				start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new Server();
	}
}
