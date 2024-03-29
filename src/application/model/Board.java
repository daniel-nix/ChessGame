package application.model;

import java.util.ArrayList;

/**
 * This is a class representation of a chess board
 * 
 * @author Chris Crabtree, Daniel Nix, Jonathan Balraj, Majd Hamoudah
 *	UTSA Application Programming CS3443 Fall 2018
 */
public class Board implements Cloneable{
	private String whiteName;
	private String blackName;
	public boolean blackEverChecked;
	public boolean whiteEverChecked;
	private Piece rook1;
	private Piece rook2;
	private Board previousBoard;
	public boolean blackIsCheckmated;
	public boolean whiteIsCheckmated;
	private Pawn pawnDoubleMoved;

	public static enum Type {
		BLACK, WHITE
	}

	public boolean isCurrentlyCheck;
	private Type turn;
	private Piece[][] board;

	/**
	 * Constructor for Board class
	 * 
	 * @param whiteName
	 * @param blackName
	 */
	public Board(String whiteName, String blackName) {
		this.blackIsCheckmated = false;
		this.whiteIsCheckmated= false;
		this.pawnDoubleMoved=null;

		this.previousBoard = null;
		this.whiteName = whiteName;
		this.blackName = blackName;
		this.rook1 = null;
		this.rook2 = null;
		this.blackEverChecked = false;
		this.whiteEverChecked = false;
		isCurrentlyCheck = false;
		turn = Type.WHITE;
		board = new Piece[8][8];
		for (int i = 0; i < 8; i++) {
			board[6][i] = new Pawn(6, i, Type.WHITE);
			board[1][i] = new Pawn(1, i, Type.BLACK);
		}
		board[0][0] = new Rook(Type.BLACK);
		board[0][7] = new Rook(Type.BLACK);
		board[7][0] = new Rook(Type.WHITE);
		board[7][7] = new Rook(Type.WHITE);

		board[0][1] = new Knight(Type.BLACK);
		board[0][6] = new Knight(Type.BLACK);
		board[7][1] = new Knight(Type.WHITE);
		board[7][6] = new Knight(Type.WHITE);

		board[0][2] = new Bishop(Type.BLACK);
		board[0][5] = new Bishop(Type.BLACK);
		board[7][2] = new Bishop(Type.WHITE);
		board[7][5] = new Bishop(Type.WHITE);

		board[0][3] = new Queen(Type.BLACK);
		board[7][3] = new Queen(Type.WHITE);

		board[0][4] = new King(Type.BLACK);
		board[7][4] = new King(Type.WHITE);
	}
	
	/**
	 * Creates a copy of a board
	 * 
	 * @param oldBoard Board to be copied
	 */
	public Board(Board oldBoard) {
		this.previousBoard = null;
		this.pawnDoubleMoved=oldBoard.pawnDoubleMoved;

		this.blackIsCheckmated = oldBoard.blackIsCheckmated;
		this.whiteIsCheckmated = oldBoard.whiteIsCheckmated;
		this.whiteName = oldBoard.getWhiteName();
		this.blackName = oldBoard.getBlackName();
		this.rook1 = oldBoard.getRook1();
		this.rook2 = oldBoard.getRook2();
		this.blackEverChecked = oldBoard.isBlackEverChecked();
		this.whiteEverChecked = oldBoard.isWhiteEverChecked();
		this.isCurrentlyCheck = oldBoard.isCurrentlyCheck();
		this.turn = oldBoard.getTurn();
		this.board = new Piece[8][8];
		for(int i = 0; i < 8; i++)
			for(int j = 0; j < 8; j++)
				if(oldBoard.hasPiece(i,j)) {
					this.board[i][j] = oldBoard.getPiece(i,j).copyPiece(oldBoard.getPiece(i,j));
				}
		
	}
	
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}
	
	/**
	 * Undoes the previous move
	 */
	public void undo() {
		if(this.previousBoard == null)
			return;
		this.whiteName = this.previousBoard.getWhiteName();
		this.blackName = this.previousBoard.getBlackName();
		this.rook1 = this.previousBoard.getRook1();
		this.rook2 = this.previousBoard.getRook2();
		this.blackEverChecked = this.previousBoard.isBlackEverChecked();
		this.whiteEverChecked = this.previousBoard.isWhiteEverChecked();
		this.isCurrentlyCheck = this.previousBoard.isCurrentlyCheck();
		this.turn = this.previousBoard.getTurn();
		this.board = this.previousBoard.getBoard();
		this.previousBoard = null;

	}
	
	/**
	 * Displays the contents of the board to stdout
	 */
	public void display() {
		for(int i = 0; i < 8 ; i++) {
			for(int j = 0; j < 8 ; j++) {
				System.out.print("| ");
				if(this.hasPiece(i,j)) {
					System.out.print(this.getPiece(i,j).toString2()+" ");
				}
				else
					System.out.print("  ");
			}
			System.out.println("|");
		}
		System.out.println();
	}
	
	public String getWhiteName() {
		return whiteName;
	}

	public void setWhiteName(String whiteName) {
		this.whiteName = whiteName;
	}

	public String getBlackName() {
		return blackName;
	}

	public void setBlackName(String blackName) {
		this.blackName = blackName;
	}

	synchronized public Type getTurn() {
		return turn;
	}
	public Type getPreviousTurn() {
		return turn == Type.BLACK ? Type.WHITE : Type.BLACK;
	}

	public void setTurn(Type turn) {
		this.turn = turn;
	}

	public Piece[][] getBoard() {
		return board;
	}

	public void setBoard(Piece[][] board) {
		this.board = board;
	}

	/**
	 * Checks if the board is in stalemate for a team
	 * of Type type
	 * 
	 * @param type Team to check
	 * @return
	 */
	public boolean isStalemate(Type type) {
		ArrayList<Coordinate> possibleMoves= new ArrayList<Coordinate>();
		for(int i =0; i< 8; i++) {
			for(int j= 0; j < 8; j++) {
				if(this.hasPiece(i,j) && this.getPiece(i,j).getType() == type ) {
					possibleMoves.addAll(this.getMoves(new Coordinate(i,j)));
				}
			}
		}
		if(possibleMoves.size() == 0)
			return true;
		else
			return false;
	}
	
	/**
	 * @param coord
	 * @return piece at that location
	 */
	public Piece getPiece(Coordinate coord, Type turn) {
		if (board[coord.getRowIndex()][coord.getColumnIndex()] != null)
			return isGettable(coord) ? board[coord.getRowIndex()][coord.getColumnIndex()] : null;
			return null;
	}

	/**
	 * @param r    row
	 * @param c    column
	 * @param turn
	 * @return piece at that location
	 */
	public Piece getPiece(int r, int c, Type turn) {
		return getPiece(new Coordinate(r, c), turn);
	}

	public Piece getPiece(Coordinate coord) {
		return board[coord.getRowIndex()][coord.getColumnIndex()];
	}

	public Piece getPiece(int r, int c) {
		return this.getPiece(new Coordinate(r, c));
	}

	public boolean isGettable(Coordinate coord) {
		Piece piece = board[coord.getRowIndex()][coord.getColumnIndex()];
		if (piece == null)
			return false;
		return piece.getType() == turn;
	}

	/**
	 * @param row
	 * @param col
	 * @return boolean if there is a piece at that location
	 */
	public boolean hasPiece(int row, int col) {
		return board[row][col] != null;
	}

	/**
	 * @param coord
	 * @return boolean if there is a piece at that location
	 */
	public boolean hasPiece(Coordinate coord) {
		return board[coord.getRowIndex()][coord.getColumnIndex()] != null;
	}
	
	
	
	public boolean isBlackIsCheckmated() {
		return blackIsCheckmated;
	}

	public void setBlackIsCheckmated(boolean blackIsCheckmated) {
		this.blackIsCheckmated = blackIsCheckmated;
	}

	public boolean isWhiteIsCheckmated() {
		return whiteIsCheckmated;
	}

	public void setWhiteIsCheckmated(boolean whiteIsCheckmated) {
		this.whiteIsCheckmated = whiteIsCheckmated;
	}

	public void exchangePiece(String name, Coordinate c, Type type) {
		if(board[c.getRowIndex()][c.getColumnIndex()] instanceof Pawn) {
			if(name.equals("rook")) 
				board[c.getRowIndex()][c.getColumnIndex()] = new Rook(type);
			if(name.equals("knight")) 
				board[c.getRowIndex()][c.getColumnIndex()] = new Knight(type);
			if(name.equals("bishop")) 
				board[c.getRowIndex()][c.getColumnIndex()] = new Bishop(type);
			if(name.equals("queen")) 
				board[c.getRowIndex()][c.getColumnIndex()] = new Queen(type);
							
		}

	}

	/**
	 * Moves piece at oldLoc to newLoc (removing piece at newLoc if there was one)
	 * and checking for special moves, processing those when detected
	 * 
	 * @param oldLoc
	 * @param newLoc
	 */
	public int movePieces(Coordinate oldLoc, Coordinate newLoc) {
		Piece piece = board[oldLoc.getRowIndex()][oldLoc.getColumnIndex()];
		ArrayList<Coordinate> availMoves = this.getMoves(oldLoc);
		boolean wasEnPassant = false;
		boolean pawnCrossed = false;
		boolean castledRight = false;
		boolean castledLeft = false;


		
		for (Coordinate c : availMoves)
			if (c.equals(newLoc)) {
				this.previousBoard = new Board(this);


				
				if(board[newLoc.getRowIndex()][0] != null 
								&& board[newLoc.getRowIndex()][0] instanceof Rook) {
					rook1 = board[newLoc.getRowIndex()][0];
				}
				else {
					rook1 = null;
				}
				
				if(board[newLoc.getRowIndex()][7] != null 
						&& board[newLoc.getRowIndex()][7] instanceof Rook) {
					rook2 = board[newLoc.getRowIndex()][7];
				}
				else {
					rook2 = null;
				}
				
				if(piece instanceof King  && piece.getHasMoved()==false) {
					if (newLoc.getColumnIndex() < 3 && rook1 != null && !rook1.getHasMoved()) {
						board[newLoc.getRowIndex()][newLoc.getColumnIndex() + 1] = rook1; // if new loc was occupied, the piece that
						rook1.setHasMoved(true);
						// was there is now deleted as there is
						// no reference to it
						board[newLoc.getRowIndex()][0] = null;
						castledLeft = true;
					} else if (newLoc.getColumnIndex() > 5 && rook2 != null && !rook2.getHasMoved()) {
						board[newLoc.getRowIndex()][newLoc.getColumnIndex() - 1] = rook2; // if new loc was occupied, the piece that
						rook2.setHasMoved(true);
						// was there is now deleted as there is
						// no reference to it
						board[newLoc.getRowIndex()][7] = null;
						castledRight = true;
					}
					

				}
				
				if (!piece.getHasMoved())
					piece.setHasMoved(true);

				if (piece instanceof Pawn ) {
						didDoubleMoveCheck(oldLoc, newLoc);
						wasEnPassant = enPassantTest(oldLoc, newLoc);
						pawnCrossed = pawnCrossedTest(newLoc, piece.getType());
				}else if(this.pawnDoubleMoved != null){
					this.pawnDoubleMoved.justDidDoubleMove = false;
					this.pawnDoubleMoved = null;
				}

				board[newLoc.getRowIndex()][newLoc.getColumnIndex()] = piece; // if new loc was occupied, the piece that
				// was there is now deleted as there is
				// no reference to it
				board[oldLoc.getRowIndex()][oldLoc.getColumnIndex()] = null;

				// sets and unsets the double moves for pawns
				//doubleMoveCheckAndSet(piece, oldLoc, newLoc);

				changeTurn();
				//Printer.boardCheck(previousBoard,this);

				
				if(castledRight)
					return 5;
				
				if(castledLeft)
					return 4;
				
				if(pawnCrossed)
					return 3;
				
				if(wasEnPassant)
					return 2;
				
				return 1;

			}
		return 0;
	}
	
	/**
	 * Checks if a pawn has crossed to the other side of the board
	 * 
	 * @param newLoc
	 * @param turn
	 * @return
	 */
	public boolean pawnCrossedTest(Coordinate newLoc, Type turn) {
		if(turn == Type.WHITE && newLoc.getRowIndex() == 0)
			return true;
		if(turn == Type.BLACK && newLoc.getRowIndex() == 7)
			return true;
		return false;
	}
	
	public void didDoubleMoveCheck(Coordinate oldLoc, Coordinate newLoc) {
		if(this.hasPiece(oldLoc) ) {
			Piece p = this.getPiece(oldLoc);
			if(p instanceof Pawn && Math.abs(oldLoc.getRowIndex() - newLoc.getRowIndex()) == 2) {
					((Pawn)this.getPiece(oldLoc)).justDidDoubleMove = true;
					this.pawnDoubleMoved = (Pawn) this.getPiece(oldLoc);
			}else if( p instanceof Pawn)
				this.pawnDoubleMoved = null;
		}
	}
	
	/**
	 * Checks if an en passant move occured
	 * 
	 * @param oldLoc
	 * @param newLoc
	 * @return
	 */
	public boolean enPassantTest(Coordinate oldLoc, Coordinate newLoc) {
		if( (newLoc.getColumnIndex() - oldLoc.getColumnIndex()) != 0){
			if (this.hasPiece(oldLoc.getRowIndex(), newLoc.getColumnIndex())) {
				if(!this.hasPiece(newLoc.getRowIndex(), newLoc.getColumnIndex())) {
					Piece pawn = this.getPiece(oldLoc);
					Piece pawnBehind = this.getPiece(oldLoc.getRowIndex(), newLoc.getColumnIndex());
					if(pawn.getType() != pawnBehind.getType()) {
						board[oldLoc.getRowIndex()][newLoc.getColumnIndex()] = null;
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Resets booleans that track if a pawn just did a double move on
	 * the previous turn
	 * 
	 * @param type
	 */


	/**
	 *  checks if the move was done by a pawn and if it was a double move
	 *  
	 * @param piece
	 * @param oldLoc
	 * @param newLoc
	 */
	/*public void doubleMoveCheckAndSet(Piece piece, Coordinate oldLoc, Coordinate newLoc) {
		if (piece instanceof Pawn) {
			if (Math.abs((oldLoc.getRowIndex() - newLoc.getRowIndex())) == 2) {
				((Pawn) piece).justDidDoubleMove = true;
				doubleMovesToFalse(piece.otherType());
			} else {
				doubleMovesToFalse(piece.getType());
				doubleMovesToFalse(piece.otherType());
			}

		} else {
			doubleMovesToFalse(piece.getType());
			doubleMovesToFalse(piece.otherType());
		}
	}*/

	/**
	 *  uses print statements to double check which pawn just did a double move
	 */
/*	public void checkDoubleMoves() {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				if (this.hasPiece(r, c)) {
					if (this.getPiece(r, c) instanceof Pawn)
						if (((Pawn) this.getPiece(r, c)).justDidDoubleMove == true)
							System.out.println("(" + r + "," + c + ") just did double move");
				}
			}
		}
	}*/

	/**
	 * Checks if board is in Check for a color
	 * 
	 * @param type Color of person that might be in Check
	 * @return
	 */
	public boolean isCheck(Type type) {
		Coordinate kingLoc = getKingCoordinate(type);

		// checks each spot on the board
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				Coordinate k = new Coordinate(i, j);

				// checks if the spot has a piece
				if (this.hasPiece(k)) {
					Piece p = this.getPiece(k);

					// checks if its a piece from the other team
					if (p.getType() != type) {

						// Checks all of its moves
						for (Coordinate c : p.getAvailableMovements(i, j, this)) {

							// if the king is in there return true
							if (c.equals(kingLoc)) {
								return true;
							}
						}
					}

				}
			}
		}
		return false;
	}

	public Coordinate getKingCoordinate(Type type) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (board[i][j] instanceof King && board[i][j].getType() == type) {
					return new Coordinate(i, j);
				}
			}
		}
		return null;
	}

	/**
	 * Checks if board is in Check for a color given King's location
	 * 
	 * @param type    type Color of person that might be in check
	 * @param kingLoc
	 * @return
	 */
	public boolean getKing(Type type, Coordinate kingLoc) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (board[i][j] instanceof Piece) {
					if (board[i][j].getAvailableMovements(i, j, this).contains(kingLoc)
							&& !board[i][j].getType().equals(type))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if board is in Checkmate for a color
	 * 
	 * @param type Color of person that might be in Checkmate
	 * @return
	 */
	public boolean isCheckmate(Type type) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (board[i][j] instanceof Piece && board[i][j].getType() == type) {
					if (this.getMoves(new Coordinate(i, j)).size() > 0)
						return false;
				}
			}
		}
		return true;
		// check if king can be attacked
	}

	/**
	 * Checks if board is in Checkmate for a color given King's location
	 * 
	 * @param type    Color of person that might be in Checkmate
	 * @param kingLoc
	 * @return
	 */
	public boolean isCheckmate(Type type, Coordinate kingLoc) {
		Coordinate[] kingsPosMoves = { new Coordinate(kingLoc.getRowIndex() - 1, kingLoc.getColumnIndex() - 1),
				new Coordinate(kingLoc.getRowIndex() - 1, kingLoc.getColumnIndex() + 1),
				new Coordinate(kingLoc.getRowIndex() + 1, kingLoc.getColumnIndex() - 1),
				new Coordinate(kingLoc.getRowIndex() + 1, kingLoc.getColumnIndex() + 1) };
		for (Coordinate posMove : kingsPosMoves) {
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					if (board[i][j] instanceof Piece) {
						if (board[i][j].getAvailableMovements(i, j, this).contains(posMove)
								&& !board[i][j].getType().equals(type))
							return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Gets AvailableMoves from the piece at location specified
	 * 
	 * @param coord
	 * @return
	 */
	public ArrayList<Coordinate> getMoves(Coordinate coord) {
		int r = coord.getRowIndex(), c = coord.getColumnIndex();
		ArrayList<Coordinate> availMoves = new ArrayList<Coordinate>();

		// checks that the piece is really there
		if (this.board[r][c] != null && this.board[r][c].getType() == turn) {
			availMoves = this.board[r][c].getAvailableMovements(r, c, this);
	
			// You cant remove elements inside a for-each loop from an ArrayList you're
			// iterating through.
			// So toBeRemoved keeps track of those elements
			// It took me two hours to learn this
			ArrayList<Coordinate> toBeRemoved = new ArrayList<Coordinate>();

			// goes through each available move for the piece
			for (Coordinate c2 : availMoves) {

				// checks if moving to c2 would put the piece's team in check
				if (movePutsTeamInCheck(coord, c2)) {
					toBeRemoved.add(c2);
				}
			}

			// removes bad movements from available moves
			availMoves.removeAll(toBeRemoved);
			return availMoves;
		} else {
			return availMoves;
		}
	}

	/**
	 * Simulates a move and checks if that move puts the other team in check
	 * 
	 * @param oldLoc
	 * @param newLoc
	 * @return
	 */
	public boolean movePutsTeamInCheck(Coordinate oldLoc, Coordinate newLoc) {
		Piece killedPiece = null;
		Piece movingPiece = board[oldLoc.getRowIndex()][oldLoc.getColumnIndex()];

		// if a piece is being killed, save it
		if (this.hasPiece(newLoc)) {
			killedPiece = board[newLoc.getRowIndex()][newLoc.getColumnIndex()];
		}

		// simulate the new board after piece moves
		board[oldLoc.getRowIndex()][oldLoc.getColumnIndex()] = null;
		board[newLoc.getRowIndex()][newLoc.getColumnIndex()] = movingPiece;

		// find if this creates check for that piece's team
		if (isCheck(movingPiece.getType())) {

			// replace pieces that were moved
			board[oldLoc.getRowIndex()][oldLoc.getColumnIndex()] = movingPiece;
			if (killedPiece != null)
				board[newLoc.getRowIndex()][newLoc.getColumnIndex()] = killedPiece;
			else
				board[newLoc.getRowIndex()][newLoc.getColumnIndex()] = null;

			return true;
		}

		// move did not create a check for the piece's team
		else {

			// replace pieces that were moved
			board[oldLoc.getRowIndex()][oldLoc.getColumnIndex()] = movingPiece;
			if (killedPiece != null)
				board[newLoc.getRowIndex()][newLoc.getColumnIndex()] = killedPiece;
			else
				board[newLoc.getRowIndex()][newLoc.getColumnIndex()] = null;

			return false;
		}

	}
	
	public boolean isBlackEverChecked() {
		return blackEverChecked;
	}

	public void setBlackEverChecked(boolean blackEverChecked) {
		this.blackEverChecked = blackEverChecked;
	}

	public boolean isWhiteEverChecked() {
		return whiteEverChecked;
	}

	public void setWhiteEverChecked(boolean whiteEverChecked) {
		this.whiteEverChecked = whiteEverChecked;
	}

	public Piece getRook1() {
		return rook1;
	}

	public void setRook1(Piece rook1) {
		this.rook1 = rook1;
	}

	public Piece getRook2() {
		return rook2;
	}

	public void setRook2(Piece rook2) {
		this.rook2 = rook2;
	}

	public Board getPreviousBoard() {
		return previousBoard;
	}

	public void setPreviousBoard(Board previousBoard) {
		this.previousBoard = previousBoard;
	}

	public boolean isCurrentlyCheck() {
		return isCurrentlyCheck;
	}

	public void setCurrentlyCheck(boolean isCurrentlyCheck) {
		this.isCurrentlyCheck = isCurrentlyCheck;
	}

	public void changeTurn() {
		turn = turn == Type.BLACK ? Type.WHITE : Type.BLACK;

	}
}
