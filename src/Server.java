import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

	public final static int P1 = 0;
	public final static int P2 = 1;
	public final static int MAX_PLAYER = 2;
	public static int playersInServer = 0;
	private ServerSocket serverSocket; // 서버소켓
	private Socket socket; // 연결소켓
	private int port; // 포트번호

	private PlayerHandler[] player = new PlayerHandler[MAX_PLAYER];

	@Override
	public void run() {
		while (true) { // 사용자 접속을 계속해서 받기 위해 while문
			try {
				System.out.print("접속 대기 중...");
				socket = serverSocket.accept(); // accept가 일어나기 전까지는 무한 대기중
				System.out.println("플레이어 접속");
				addPlayer(socket);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("accept error");
				// 여기서 비정상 종료시 클라이언트
			}
		}
	}

	/** 서버에 플레이어 추가.	
	 * @param socket 플레이어의 소켓 번호 */
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
		} // 서버가 포트 여는부분
		serverStart(30023);
	}

	public void serverStart(int port) {
		this.port = port;
		try {
			serverSocket = new ServerSocket(port); // 서버가 포트 여는부분
			if (serverSocket != null) { // socket 이 정상적으로 열렸을때
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
