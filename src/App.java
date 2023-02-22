import java.util.InputMismatchException;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class App {
    public static void main(String[] args) throws Exception {
        ChessMatch chessMatch = new ChessMatch();
        Scanner s = new Scanner(System.in);

        while (true) {
            try {
                UI.clearScreen();
                UI.printBoard(chessMatch.getPieces());
                System.out.print("\nSource: ");
                ChessPosition source = UI.readChessPosition(s);

                System.out.print("\nTarget: ");
                ChessPosition target = UI.readChessPosition(s);

                ChessPiece capturedPiece = chessMatch.performChessMove(source, target);
            } catch (ChessException e) {
                System.out.println(e.getMessage());
                s.nextLine();
                s.nextLine();
            } catch (InputMismatchException e) {
                System.out.println(e.getMessage());
                s.nextLine();
                s.nextLine();
            }
        }
    }
}
