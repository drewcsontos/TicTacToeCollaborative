import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TicTacToeClient {

    public static void main(String[] args) throws Exception {
        try (Socket s = new Socket("72.83.97.26", 59090)) {
            System.out.println("Enter lines of text then Ctrl+D or Ctrl+C to quit");
            Scanner scn = new Scanner(System.in);
            Scanner in = new Scanner(s.getInputStream());
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            Runnable rn = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String line = "";
                        try {
                            line = in.nextLine();
                        } catch (java.util.NoSuchElementException e) {
                            System.exit(0);
                        }

                        System.out.println(line);
                    }
                }
            };
            Thread thread1 = new Thread(rn);
            thread1.start();
            while (true) {
                String str = scn.nextLine();
                if (str.equals("exit")) {
                    break;
                }
                out.println(str);

            }
            in.close();
            scn.close();
        }
    }
}