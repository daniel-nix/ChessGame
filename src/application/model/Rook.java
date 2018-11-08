package application.model;

import java.util.ArrayList;

import application.model.Board.Type;

public class Rook extends Piece 
{
	public Rook(Type color)
	{
		super (color);
	}

	public ArrayList<Coordinate> getAvailableMovements(int r, int c, Board board) {
		ArrayList<Coordinate> availCoords = new ArrayList<>();
							
		//first number is the vertical direction, the second is the horizontal direction
		//negative is up or left, positive is down or right. Zero keeps it stationary
		addMovements(availCoords, -1,0, r, c, board);//South direction
		addMovements(availCoords, 0,-1, r, c, board);//West direction
		addMovements(availCoords, 1,0, r, c, board);//North direction
		addMovements(availCoords, 0,1, r, c, board);//East direction
		
		return availCoords;
	}
	
	public void addMovements(ArrayList<Coordinate> availCoords,int rowInc, int columnInc
															, int r, int c, Board board){		
		for (int i = 1; i < 8 ; i++) {
			if (boundsChecker(r+i*rowInc, c+i*columnInc)) {//Checks if location is on the board
			
				if(board.hasPiece(new Coordinate(r + i*rowInc, c+i*columnInc))) { //A piece is on this
					//location
					if(board.getPiece(new Coordinate(r+i*rowInc,c+i*columnInc)).getType()//Its an enemy
																	== this.otherType()) //piece
						availCoords.add(new Coordinate(r+i*rowInc,c+i*columnInc));			
					break;				
				}
			else //Location is a free space
				availCoords.add(new Coordinate(r+i*rowInc,c+i*columnInc));			
			}
		}
	}		

}
