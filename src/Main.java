import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.start();

        Client c1 = new Client();
        System.out.println("Enter your messages:");
        Scanner sc = new Scanner(System.in);
        boolean can = true;
        while (can) {
            String s = sc.nextLine();
            if (s.equals("end"))
                can = false;
            c1.sendEcho(s);
        }
    }
}
