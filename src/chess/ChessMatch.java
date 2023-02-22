package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;

public class ChessMatch {

    private int turn;
    private ColorEnum currentPlayer;
    private Board board;
    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();
    private boolean inCheck;
    private boolean inCheckMate;

    public ChessMatch() {
        board = new Board(8, 8);
        turn = 1;
        currentPlayer = ColorEnum.WHITE;
        initialSetup();
    }

    public boolean getInCheckMate() {
        return inCheckMate;
    }

    public int getTurn() {
        return turn;
    }

    public ColorEnum getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getInCheck() {
        return inCheck;
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == ColorEnum.WHITE) ? ColorEnum.BLACK : ColorEnum.WHITE;
    }

    public ChessPiece[][] getPieces() {
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j);
            }
        }
        return mat;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();
        validateSourcePosition(source);
        validateTargetPosition(source, target);
        Piece capturedPiece = makeMove(source, target);

        if (testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("You can't put yourself in check");
        }

        inCheck = testCheck(opponent(currentPlayer));

        if (testCheckMate(opponent(currentPlayer))) {
            inCheckMate = true;
        }

        nextTurn();
        return (ChessPiece) capturedPiece;
    }

    private void validateSourcePosition(Position position) {
        if (!board.thereIsAPiece(position)) {
            throw new ChessException("There is no piece on source position");
        }
        if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) {
            throw new ChessException("The chosen piece isn't yours");
        }
        if (!board.piece(position).isThereAnyPossibleMove()) {
            throw new ChessException("There is no possible moves for the chosen piece");
        }
    }

    private void validateTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target)) {
            throw new ChessException("The chosen piece can't move to target position");
        }
    }

    private Piece makeMove(Position source, Position target) {
        ChessPiece p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);

        if (p != null) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece) {
        ChessPiece p = (ChessPiece) board.removePiece(target);
        board.placePiece(p, source);
        p.decreaseMoveCount();

        if (capturedPiece != null) {
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }

        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }
    }

    private ColorEnum opponent(ColorEnum color) {
        return color == ColorEnum.WHITE ? ColorEnum.BLACK : ColorEnum.WHITE;
    }

    private ChessPiece king(ColorEnum color) {
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
                .collect(Collectors.toList());
        for (Piece p : list) {
            if (p instanceof King) {
                return (ChessPiece) p;
            }
        }
        throw new IllegalStateException("There is no " + color + " king on the board");
    }

    private boolean testCheck(ColorEnum color) {
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream()
                .filter(x -> ((ChessPiece) x).getColor() == opponent(color)).collect(Collectors.toList());
        for (Piece p : opponentPieces) {
            boolean[][] mat = p.possibleMoves();
            if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
                return true;
            }
        }
        return false;
    }

    private boolean testCheckMate(ColorEnum color) {
        if (!testCheck(color)) {
            return false;
        }
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
                .collect(Collectors.toList());
        for (Piece p : list) {
            boolean[][] mat = p.possibleMoves();
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if (mat[i][j]) {
                        Position source = ((ChessPiece) p).getChessPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean test = testCheck(color);
                        undoMove(source, target, capturedPiece);

                        if (!test) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }

    private void initialSetup() {
        placeNewPiece('a', 1, new Rook(board, ColorEnum.WHITE));
        placeNewPiece('b', 1, new Knight(board, ColorEnum.WHITE));
        placeNewPiece('c', 1, new Bishop(board, ColorEnum.WHITE));
        placeNewPiece('e', 1, new King(board, ColorEnum.WHITE, this));
        placeNewPiece('f', 1, new Bishop(board, ColorEnum.WHITE));
        placeNewPiece('g', 1, new Knight(board, ColorEnum.WHITE));
        placeNewPiece('h', 1, new Rook(board, ColorEnum.WHITE));
        placeNewPiece('a', 2, new Pawn(board, ColorEnum.WHITE));
        placeNewPiece('b', 2, new Pawn(board, ColorEnum.WHITE));
        placeNewPiece('c', 2, new Pawn(board, ColorEnum.WHITE));
        placeNewPiece('d', 2, new Pawn(board, ColorEnum.WHITE));
        placeNewPiece('e', 2, new Pawn(board, ColorEnum.WHITE));
        placeNewPiece('f', 2, new Pawn(board, ColorEnum.WHITE));
        placeNewPiece('g', 2, new Pawn(board, ColorEnum.WHITE));
        placeNewPiece('h', 2, new Pawn(board, ColorEnum.WHITE));
        placeNewPiece('d', 1, new Queen(board, ColorEnum.WHITE));

        placeNewPiece('a', 8, new Rook(board, ColorEnum.BLACK));
        placeNewPiece('b', 8, new Knight(board, ColorEnum.BLACK));
        placeNewPiece('c', 8, new Bishop(board, ColorEnum.BLACK));
        placeNewPiece('e', 8, new King(board, ColorEnum.BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, ColorEnum.BLACK));
        placeNewPiece('g', 8, new Knight(board, ColorEnum.BLACK));
        placeNewPiece('h', 8, new Rook(board, ColorEnum.BLACK));
        placeNewPiece('d', 8, new Queen(board, ColorEnum.BLACK));
        placeNewPiece('a', 7, new Pawn(board, ColorEnum.BLACK));
        placeNewPiece('b', 7, new Pawn(board, ColorEnum.BLACK));
        placeNewPiece('c', 7, new Pawn(board, ColorEnum.BLACK));
        placeNewPiece('d', 7, new Pawn(board, ColorEnum.BLACK));
        placeNewPiece('e', 7, new Pawn(board, ColorEnum.BLACK));
        placeNewPiece('f', 7, new Pawn(board, ColorEnum.BLACK));
        placeNewPiece('g', 7, new Pawn(board, ColorEnum.BLACK));
        placeNewPiece('h', 7, new Pawn(board, ColorEnum.BLACK));

    }
}
