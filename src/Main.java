import java.net.SocketException;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {
        Thread server_t = new Thread(new Server());
        server_t.start();

        Thread.sleep(500);

        Thread client_t = new Thread(new Client());
        client_t.start();
    }
}
