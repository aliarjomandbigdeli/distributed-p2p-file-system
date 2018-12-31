import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String s = "Please enter your command in the form of the following commands : \np2p –receive fileName \np2p –serve -name fileName -path filePath\nend";
            System.err.println(s);
            Seesion currentSession = interpreter(scanner.nextLine());
            if(currentSession == null)
                return;
            else if(currentSession.isServer()){
                Thread server_t = new Thread(new Server(currentSession));
                server_t.start();
                server_t.join();
            }
            else{
                Thread client_t = new Thread(new Client(currentSession));
                client_t.start();
                client_t.join();
            }
        }
    }

    public static Seesion interpreter(String s) {
        Seesion result = null;
        String[] splited = s.split("\\s+");

        if(splited.length < 2)
            return result;

        else if(splited[1].equals("–receive") && splited.length == 3){
            result = new Seesion(false);
            result.setFileName(splited[2]);
        }
        else if(splited[1].equals("–serve") && splited.length == 6){
            result = new Seesion(true);
            result.setFileName(splited[3]);
            result.setPath(splited[5]);
        }
        return result;
    }

}
