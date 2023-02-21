import java.util.Scanner;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class App {
    public static void main(String[] args) throws Exception {
        ChessMatch chessMatch = new ChessMatch();
        Scanner s = new Scanner(System.in);

        while (true) {
            UI.printBoard(chessMatch.getPieces());
            System.out.print("\nSource: ");
            ChessPosition source = UI.readChessPosition(s);

            System.out.print("\nTarget: ");
            ChessPosition target = UI.readChessPosition(s);

            ChessPiece capturedPiece = chessMatch.performChessMove(source, target);
        }
    }
}
