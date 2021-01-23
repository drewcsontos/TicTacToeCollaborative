import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicTacToeServer {

    static String[] board = new String[9];
    static Player currentPlayer;

    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(59090)) {
            System.out.println("The server is running...");
            ExecutorService pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new Player(listener.accept(), "X"));
                pool.execute(new Player(listener.accept(), "O"));
            }
        }
    }

    public static boolean hasWon() {
        for (int i = 0; i < 3; i++) {
            if (board[3 * i] == board[3 * i + 1] && board[3 * i + 1] == board[3 * i + 2] && board[3 * i] != null)
                return true;
            if (board[i] == board[i + 3] && board[i + 3] == board[i + 6] && board[i] != null)
                return true;
        }
        if (board[0] == board[4] && board[4] == board[8] && board[0] != null)
            return true;
        if (board[2] == board[4] && board[4] == board[6] && board[2] != null)
            return true;

        return false;
    }

    public static void changeBoard(int num) {
        board[num] = currentPlayer.type;
    }

    public static String printBoard() {
        String b = "";
        for (int i = 0; i < board.length; i++) {
            if (i % 3 == 0)
                b += "\n";
            if (board[i] == null)
                b += i + " ";
            else
                b += board[i] + " ";

        }
        return b;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static class Player implements Runnable {
        private Socket s;
        private String type;
        private Player opponent;
        private Scanner in;
        private PrintWriter out;

        Player(Socket s, String type) throws IOException {
            this.s = s;
            this.type = type;
            in = new Scanner(this.s.getInputStream());
            out = new PrintWriter(this.s.getOutputStream(), true);
            if (this.type == "X") {
                currentPlayer = this;
                out.println("Waiting for opponent to join.");
            } else {
                out.println("Waiting for opponent to move");
                opponent = currentPlayer;
                opponent.opponent = this;
                opponent.out.println("Your turn");
                opponent.out.println(printBoard());
            }
        }

        @Override
        public void run() {
            System.out.println("Connected: " + s);
            try {
                while (in.hasNextLine()) {
                    String selection = in.nextLine();
                    if (opponent == null)
                        out.println("The other player has not joined!");
                    else if (this.type != currentPlayer.type)
                        out.println("It's not your turn yet!");
                    else if (isNumeric(selection) && Integer.parseInt(selection) < 9 && Integer.parseInt(selection) >= 0
                            && board[Integer.parseInt(selection)] == null) {
                        changeBoard(Integer.parseInt(selection));
                        out.println(printBoard());
                        if (hasWon()) {
                            out.println("You won!");
                            opponent.out.println(printBoard());
                            opponent.out.println("You lost! Better luck next time.");
                            opponent.s.close();
                            this.s.close();
                            System.exit(0);
                        } else {
                            out.println("You just moved. Waiting for opponents move.");
                            currentPlayer = opponent;
                            opponent.out.println("Your Turn");
                            opponent.out.println(printBoard());
                        }
                    } else
                        out.println("Type a valid number from 0 to 8.");
                }
            } catch (Exception e) {
                System.out.println("Error:" + s);
            } finally {
                try {
                    s.close();
                } catch (IOException e) {
                }
                System.out.println("Closed: " + s);
            }
        }
    }
}