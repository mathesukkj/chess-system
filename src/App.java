import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class App {
    public static void main(String[] args) throws Exception {
        ChessMatch chessMatch = new ChessMatch();
        Scanner s = new Scanner(System.in);
        List<ChessPiece> capturedPieces = new ArrayList<>();

        while (true) {
            try {
                UI.clearScreen();
                UI.printMatch(chessMatch, capturedPieces);
                System.out.println();
                UI.printBoard(chessMatch.getPieces());
                System.out.print("\nSource: ");
                ChessPosition source = UI.readChessPosition(s);

                boolean[][] possibleMoves = chessMatch.possibleMoves(source);
                UI.clearScreen();
                UI.printBoard(chessMatch.getPieces(), possibleMoves);

                System.out.print("\nTarget: ");
                ChessPosition target = UI.readChessPosition(s);

                ChessPiece capturedPiece = chessMatch.performChessMove(source, target);

                if (capturedPiece != null) {
                    capturedPieces.add(capturedPiece);
                }
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
