import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws UnknownHostException {
        Thread server = new Server(4445);
        server.start();

        Client c1 = new Client(4445);
        for (int i = 0; i < 10000; i++) {
            try {
                c1.broadcast("Broadcast Hello " + i, InetAddress.getByName("255.255.255.255"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


//        System.out.println("Enter your messages:");
//        Scanner sc = new Scanner(System.in);
//        boolean can = true;
//        while (can) {
//            String s = sc.nextLine();
//            if (s.equals("end"))
//                can = false;
//            c1.sendEcho(s);
//        }
    }
}
