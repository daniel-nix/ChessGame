package application.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import application.model.AI;
import application.model.Board;
import application.model.Board.Type;
import application.model.Coordinate;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

/**
 * This is the controller for the board.fxml
 * 
 * @author Chris Crabtree, Daniel Nix, Majd Hamoudah
 *	UTSA Application Programming CS3443 Fall 2018
 */
public class BoardController {
	private ImageView selectedPiece;
	private boolean isSuggesting;
	private Coordinate clickedPieceCoordinate;
	public static Board boardModel;
	private Coordinate pawnToPromote;
	private boolean timesUp, isStalemate;
	private Type turn;
	private ArrayList<Coordinate> availableMoves;
	@FXML
	public Label p1Min, p2Min;
	public Pane timer1Pane, timer2Pane, infoPane;
	@FXML 
	private Button suggestionButton, infoButton;

	@FXML
	private Label whiteNameLabel, whiteNameLabel2;

	@FXML
	public Label feedbackLabel;
	
	@FXML
	private Label blackNameLabel, blackNameLabel2;

	@FXML
	private Label checkLabel;

	@FXML
	private Label endGameLabel;

	@FXML
	private Label turnLabel;

	@FXML
	private GridPane boardFX;
	
	@FXML
	private Pane promotionPane;
	
	private AI myAI = new AI();
	
	/**
	 * Shows the info for C3PO
	 * 
	 * @param event
	 */
	@FXML
	public void showInfo(ActionEvent event) {
		infoPane.setVisible(true);
		infoPane.setMouseTransparent(false);
		timesUp = true;
	}
	
	/**
	 * Allows the user to return to game after reading
	 * C3PO info
	 * 
	 * @param event
	 */
	@FXML
	public void returnToGame(ActionEvent event) {
		timesUp = false;
		infoPane.setMouseTransparent(true);

		infoPane.setVisible(false);

	}
	
	/**
	 * Handles the processing when the suggestion button is pressed
	 * 
	 * @param event
	 */
	@FXML
	public void handleSuggestion(ActionEvent event) {
		if(!isSuggesting) {
			isSuggesting = true;
			timesUp = true;
			Coordinate[] moveSug = myAI.getBestMove(boardModel, 3, true, new Random(), false);
			if(clickedPieceCoordinate != null) {
				unselectPiece(clickedPieceCoordinate);
			}
			selectPiece2(moveSug[0]);
			Task<?> t3 = new Task() {

				@Override
				protected Object call() throws Exception {
					Thread.sleep(1500);
					return null;
				}

			};

			t3.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent t) {
					moveAndReturn(moveSug);
					timesUp = false;
					selectedPiece = null;
					clickedPieceCoordinate = null;

				}
			});
			Thread th3 = new Thread(t3);
			th3.setDaemon(true);
			th3.start();
		}

	}
	
	/**
	 * Demonstrates a move suggestion to the user
	 * 
	 * @param moveSug Coordinate array representing a move
	 */
	public void moveAndReturn(Coordinate[] moveSug) {
		Pane pFrom =getPaneByRowColumnIndex(clickedPieceCoordinate.getRowIndex()
				, clickedPieceCoordinate.getColumnIndex());
		Pane pTo =getPaneByRowColumnIndex(moveSug[1].getRowIndex()
				, moveSug[1].getColumnIndex());
		Node killedPiece = null;
    	removeDots(moveSug[1]);

		if(boardModel.hasPiece(moveSug[1]) && pTo != null && pTo.getChildren().size() >0 
										&& !(pTo.getChildren().get(0) instanceof Circle )) {
			killedPiece = pTo.getChildren().get(0);
			killPiece(pTo);
		}
		else {
			movePiece(pTo);
		}
		final Node fKilledPiece = killedPiece;
		Task<?> t7 = new Task() {

			@Override
			protected Object call() throws Exception {
				Thread.sleep(1500);
				return null;
			}
			
		};
		
		t7.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
		    @Override
		    public void handle(WorkerStateEvent t) {
				pFrom.getChildren().add(pTo.getChildren().get(0));
				if(fKilledPiece != null) {
					
					pTo.getChildren().add(fKilledPiece);
				}
				isSuggesting = false;
				
		    }
		});
		Thread th3 = new Thread(t7);
		th3.setDaemon(true);
		th3.start();
		

		
		
	}
	
	/**
	 * Handles when a player promotes a pawn and must decide what peice to
	 * replace it with
	 * 
	 * @param event
	 */
	@FXML
	public void handleChoice(ActionEvent event) {
		String pieceChosenStr =((Button) event.getSource()).getText().toLowerCase();
		boardModel.exchangePiece(pieceChosenStr, pawnToPromote, boardModel.getPreviousTurn());
		ImageView view = ((ImageView)getPaneByRowColumnIndex(pawnToPromote.getRowIndex()
				, pawnToPromote.getColumnIndex()).getChildren().get(0));
		String imagePath = "images/";
		if(boardModel.getPreviousTurn() == Type.WHITE) 
			imagePath +="white_";			
		
		if(boardModel.getPreviousTurn()  == Type.BLACK) 
			imagePath +="black_";			
		
		imagePath += pieceChosenStr+".png";
		
		putImage(view, imagePath );
		promotionPane.setVisible(false);
		
		
	}
	
	/**
	 * Getter for the model representation of the board
	 * 
	 * @return Board The board for the model
	 */
	synchronized Board getBoard() {
		return this.boardModel;
	}

	/**
	 * Handles The clicking of pieces and spaces on the board for 
	 * the view
	 * 
	 * @param event
	 */
	@FXML
	public void handlePieceClick(MouseEvent event) {
		if((!StartScreenController.isAI || boardModel.getTurn() == Type.WHITE)
				&& !timesUp && !boardModel.isBlackIsCheckmated() && !boardModel.isWhiteIsCheckmated()) {
			// ***A piece has not been selected yet***
			if (selectedPiece == null) {
				selectPiece(event);
				//boardModel.display();

			}


			// ***A piece has already been selected***
			else {
				Coordinate c = findCoordinate(event);
				Pane clickedPane = (Pane) getPaneByRowColumnIndex(c.getRowIndex(), c.getColumnIndex());
				int typeOfMove = boardModel.movePieces(clickedPieceCoordinate, c);

				// **Move was not possible**
				if(typeOfMove == 0)
					unselectPiece(c);

				// **Move was possible**
				if (typeOfMove >= 1  ) {

					// Moved to an empty space
					if (clickedPane.getChildren().size() != 0 && clickedPane.getChildren().get(0) 
							instanceof Circle) {
						movePiece(clickedPane);

						//**Move was an En Passant**
						if (typeOfMove == 2)
							processEnPassant(clickedPieceCoordinate, c);

						//Pawn reached opposite side
						if(typeOfMove == 3) {					
							promotePawn(boardModel.getPreviousTurn(), c);
						}

						//moving rook
						if (typeOfMove == 5) {
							Pane p = (Pane) getPaneByRowColumnIndex(c.getRowIndex(), 7);

							if(p.getChildren().get(0) != null) {

								ImageView thing = (ImageView) p.getChildren().get(0);
								p.getChildren().remove(0);
								Pane destination = (Pane) getPaneByRowColumnIndex(c.getRowIndex(),  c.getColumnIndex() - 1);
								destination.getChildren().add(thing);

							}

						}
						if (typeOfMove == 4) {
							Pane p = (Pane) getPaneByRowColumnIndex(c.getRowIndex(), 0);
							if(p.getChildren().get(0) != null) {
								ImageView thing = (ImageView) p.getChildren().get(0);
								p.getChildren().remove(thing);
								Pane destination = (Pane) getPaneByRowColumnIndex(c.getRowIndex(),  c.getColumnIndex() + 1);
								destination.getChildren().add(thing);
							}
						}


					}

					// Moved to enemy space
					else {
						killPiece(clickedPane);
						//Pawn reached opposite side
						if(typeOfMove == 3) {	
							promotePawn(boardModel.getPreviousTurn(), c);
						}
					}
					
					if(StartScreenController.giveFeedback) {
						myAI.feedbackThread(this, clickedPieceCoordinate, c);
					}
					// Reset variables and see if its check or checkmate
					endOfMoveProcessing(c);
					if(StartScreenController.isAI) {
						myAI.moveAIThread(this);
					}
					
				}
			}

		}
	}
	
	/**
	 * Moves the AI for the thread controlling its movement, but
	 * slows the movement to allow the user to see where the AI 
	 * moved to
	 * 
	 * @param a Coordinate of where a move was from
	 * @param b Coordinate of where a move was to
	 */
	synchronized public void moveAI(Coordinate a, Coordinate b) {
		if(boardModel.isCheckmate(Type.BLACK)||boardModel.isWhiteIsCheckmated()|| timesUp|| this.isStalemate)
			return;
		Coordinate c = b;

		clickedPieceCoordinate = a;
		selectPiece(clickedPieceCoordinate);
		final Coordinate cf = c;
		Task<?> t3 = new Task() {

			@Override
			protected Object call() throws Exception {
				Thread.sleep(1500);
				return null;
			}
			
		};
		
		t3.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
		    @Override
		    public void handle(WorkerStateEvent t) {
		        moveAI2(cf);
		    }
		});
		Thread th3 = new Thread(t3);
		th3.setDaemon(true);
		th3.start();
	}
	
	/**
	 * Handles the second half of the movement of the AI
	 * 
	 * @param c
	 */
	synchronized public void moveAI2(Coordinate c) {
		Pane clickedPane = (Pane) getPaneByRowColumnIndex(c.getRowIndex(), c.getColumnIndex());
		int typeOfMove = boardModel.movePieces(clickedPieceCoordinate, c);
		// **Move was not possible**
		if(typeOfMove == 0)
			unselectPiece(c);

		
		// **Move was possible**
		if (typeOfMove >= 1  ) {

			// Moved to an empty space
			if (clickedPane.getChildren().size() != 0 && clickedPane.getChildren().get(0) 
					instanceof Circle) {
				movePiece(clickedPane);

				//**Move was an En Passant**
				if (typeOfMove == 2)
					processEnPassant(clickedPieceCoordinate, c);

				//Pawn reached opposite side
				if(typeOfMove == 3) {					
					promotePawn(boardModel.getPreviousTurn(), c);
				}

				//moving rook
				if (typeOfMove == 5) {
					Pane p = (Pane) getPaneByRowColumnIndex(c.getRowIndex(), 7);

					if(p.getChildren().get(0) != null) {

						ImageView thing = (ImageView) p.getChildren().get(0);
						p.getChildren().remove(0);
						Pane destination = (Pane) getPaneByRowColumnIndex(c.getRowIndex(),  c.getColumnIndex() - 1);
						destination.getChildren().add(thing);

					}

				}
				if (typeOfMove == 4) {
					Pane p = (Pane) getPaneByRowColumnIndex(c.getRowIndex(), 0);
					if(p.getChildren().get(0) != null) {
						ImageView thing = (ImageView) p.getChildren().get(0);
						p.getChildren().remove(thing);
						Pane destination = (Pane) getPaneByRowColumnIndex(c.getRowIndex(),  c.getColumnIndex() + 1);
						destination.getChildren().add(thing);
					}
				}


			}

			// Moved to enemy space
			else {
				killPiece(clickedPane);
				//Pawn reached opposite side
				if(typeOfMove == 3) {	
					promotePawn(boardModel.getPreviousTurn(), c);
				}
			}

			// Reset variables and see if its check or checkmate
			endOfMoveProcessing(c);
		}
	}
	
	/**
	 * Handles pawn promotion processing
	 * 
	 * @param type	The turn of the player who is 
	 * @param c		Coordinate of the pawn to promote
	 */
	public void promotePawn(Type type, Coordinate c) {
		pawnToPromote = c;
		promotionPane.setVisible(true);
		ObservableList<Node> children = promotionPane.getChildren();
		if(children.size() == 8 && type == Type.WHITE) {
			putImage(((ImageView)children.get(4)),"images/white_rook.png" );
			putImage(((ImageView)children.get(5)),"images/white_knight.png" );
			putImage(((ImageView)children.get(6)),"images/white_bishop.png" );
			putImage(((ImageView)children.get(7)),"images/white_queen.png" );
		}
		if(children.size() == 8 && type == Type.BLACK) {
			putImage(((ImageView)children.get(4)),"images/black_rook.png" );
			putImage(((ImageView)children.get(5)),"images/black_knight.png" );
			putImage(((ImageView)children.get(6)),"images/black_bishop.png" );
			putImage(((ImageView)children.get(7)),"images/black_queen.png" );
		}
	}
	
	/**
	 * Displays the images for the pawn promotion screen
	 * 
	 * @param view View for the image to be displayed
	 * @param imagePath Filepath for the image
	 */
	public void putImage(ImageView view, String imagePath) {
		File file = new File(imagePath);
		Image image = new Image(file.toURI().toString());
		view.setVisible(true);
		view.setImage(image);

	}

	/**
	 * Processes the selecting of a piece
	 * 
	 * @param event
	 */
	public void selectPiece(MouseEvent event) {
		clickedPieceCoordinate = findCoordinate(event);
		Pane p = (Pane) event.getSource();
		if (p.getChildren().size() != 0 && boardModel.hasPiece(clickedPieceCoordinate)) {
			if (boardModel.getPiece(clickedPieceCoordinate).getType() == boardModel.getTurn()) {
				selectedPiece = (ImageView) p.getChildren().get(0);
				availableMoves = boardModel.getMoves(clickedPieceCoordinate);
				addDots(clickedPieceCoordinate);
			}
		}
	}
	
	/**
	 * Processes the selecting of a piece by coordinate
	 * 
	 * @param c Coordinate of piece to select
	 */
	public void selectPiece2(Coordinate c) {
		clickedPieceCoordinate = c;
		Pane p = (Pane) getPaneByRowColumnIndex(clickedPieceCoordinate.getRowIndex()
									, clickedPieceCoordinate.getColumnIndex());
		if (p.getChildren().size() != 0 && boardModel.hasPiece(clickedPieceCoordinate)) {
			if (boardModel.getPiece(clickedPieceCoordinate).getType() == boardModel.getTurn()) {
				selectedPiece = (ImageView) p.getChildren().get(0);
				availableMoves = boardModel.getMoves(clickedPieceCoordinate);
				addDots(clickedPieceCoordinate);
			}
		}
	}
	
	/**
	 * A synchronized version of select piece to prevent thread overlap
	 * 
	 * @param c Coordinate of piece to select
	 */
	synchronized public void selectPiece(Coordinate c) {

		clickedPieceCoordinate = c;
		Pane p = (Pane) getPaneByRowColumnIndex(c.getRowIndex(), c.getColumnIndex());
		if (p.getChildren().size() != 0 && boardModel.hasPiece(clickedPieceCoordinate)) {
			if (boardModel.getPiece(clickedPieceCoordinate).getType() == boardModel.getTurn()) {
				selectedPiece = (ImageView) p.getChildren().get(0);
				availableMoves = boardModel.getMoves(clickedPieceCoordinate);
				addDots(clickedPieceCoordinate);
			}
		}
	}
	
	/**
	 * Move piece processing
	 * 
	 * @param clickedPane
	 */
	public void movePiece(Pane clickedPane) {
		boardFX.getChildren().remove(selectedPiece);
		clickedPane.getChildren().add(selectedPiece);
	}

	/**
	 * Kill piece processing
	 * 
	 * @param clickedPane
	 */
	public void killPiece(Pane clickedPane) {
		ImageView enemyPiece = (ImageView) clickedPane.getChildren().get(0);
		clickedPane.getChildren().remove(enemyPiece);
		boardFX.getChildren().remove(selectedPiece);
		clickedPane.getChildren().add(selectedPiece);
	}

	/**
	 * Processing for end of move
	 * 
	 * @param c Coordinate for processing
	 */
	public void endOfMoveProcessing(Coordinate c) {
		turnLabelAppearance();
		testForCheckAndCheckmate(boardModel.getPiece(c).otherType());
		unselectPiece(c);
	}

	/**
	 * Processing for unselect piece
	 * 
	 * @param c
	 */
	public void unselectPiece(Coordinate c) {
		removeDots(c);
		availableMoves = null;
		selectedPiece = null;
		clickedPieceCoordinate = null;
	}

	/**
	 * Tests for check, checkmate, and stalemate and processes 
	 * accordingly
	 * 
	 * @param type Color of team to check
	 */
	public void testForCheckAndCheckmate(Type type) {

		if (boardModel.isCheck(type)) {
			if(type == Type.WHITE) {
				boardModel.whiteEverChecked = true;
			}
			else if(type == Type.BLACK) {
				boardModel.blackEverChecked = true;
			}
			checkLabel.setVisible(true);
		}
		else
			checkLabel.setVisible(false);

		if (boardModel.isCheckmate(type)) {
			checkLabel.setVisible(false);
			endGameLabel.setVisible(true);
			if(type == Type.WHITE) {
				boardModel.setWhiteIsCheckmated(true);
				endGameLabel.setText("Checkmate!\n"+blackNameLabel.getText()+" wins");

			}
			else {
				boardModel.setBlackIsCheckmated(true);
				endGameLabel.setText("Checkmate!\n"+whiteNameLabel.getText()+" wins");

			}
		}else if (boardModel.isStalemate(type)) {
			checkLabel.setVisible(false);
			endGameLabel.setVisible(true);
			boardModel.setWhiteIsCheckmated(true);
			isStalemate = true;
			boardModel.setBlackIsCheckmated(true);
			endGameLabel.setText("Stalemate!\nEveryone's a winner!");

		}


	}

	/**
	 * Processing for the en passant move
	 * 
	 * @param fromPosition
	 * @param toPosition
	 */
	public void processEnPassant(Coordinate fromPosition, Coordinate toPosition) {
		Pane p = (Pane) getPaneByRowColumnIndex(fromPosition.getRowIndex(), toPosition.getColumnIndex());
		if (p.getChildren().get(0) != null) {
			ImageView killedPawn = (ImageView) p.getChildren().get(0);
			p.getChildren().remove(killedPawn);
		}
	}

	/**
	 * Adds dots to display available moves
	 * 
	 * @param b Coordinate of source piece
	 */
	public void addDots(Coordinate b) {
		Pane pane2 = (Pane) getPaneByRowColumnIndex(b.getRowIndex(), b.getColumnIndex());
		pane2.setStyle("-fx-background-color: #F9A602;");
		for (Coordinate c : availableMoves) {
			Pane pane = (Pane) getPaneByRowColumnIndex(c.getRowIndex(), c.getColumnIndex());
			if (!boardModel.hasPiece(c)) {
				Circle circle = new Circle(37.0, 37.0, 10.0);
				pane.getChildren().add(circle);
			} else {
				pane.setStyle("-fx-background-color: #FF0000;");
			}

		}
	}

	/**
	 * Removes dots after move is selected
	 * 
	 * @param d Coordinate of source piece
	 */
	public void removeDots(Coordinate d) {
		changeToOriginalColor(clickedPieceCoordinate);
		for (Coordinate c : availableMoves) {
			Pane pane = (Pane) getPaneByRowColumnIndex(c.getRowIndex(), c.getColumnIndex());
			if (!boardModel.hasPiece(c) || c.equals(d)) {
				changeToOriginalColor(c);
				if (pane.getChildren().get(0) instanceof Circle)
					pane.getChildren().remove(0);
			} else {
				if(pane.getChildren().size()>1) {
					if(pane.getChildren().get(0) instanceof Circle)
						pane.getChildren().remove(0);
				}
				changeToOriginalColor(c);
			}
		}
	}

	/**
	 * Changes the colors of selected piece panes to their original color
	 * 
	 * @param a
	 */
	public void changeToOriginalColor(Coordinate a) {
		Pane pane = (Pane) getPaneByRowColumnIndex(a.getRowIndex(), a.getColumnIndex());

		if (((a.getColumnIndex() + a.getRowIndex()) % 2) == 1)
			pane.setStyle("-fx-background-color:  #595756;");
		else
			pane.setStyle("-fx-background-color:  white;");
	}

	/**
	 * Returns the gridpane coordinates of a mouse click
	 * 
	 * @param event Mouse click
	 * @return Coordinat Coordinate of click
	 */
	private static Coordinate findCoordinate(MouseEvent event) {
		Coordinate a = new Coordinate();
		Node source;
		if (((Node) event.getSource()).getParent() instanceof GridPane)
			source = (Node) event.getSource();
		else
			source = ((Node) event.getSource()).getParent();

		if (GridPane.getRowIndex(source) != null)
			a.setRowIndex(GridPane.getRowIndex(source));
		else
			a.setRowIndex(0);
		if (GridPane.getColumnIndex(source) != null)
			a.setColumnIndex(GridPane.getColumnIndex(source));
		else
			a.setColumnIndex(0);

		return a;
	}

	/**
	 * Returns the pane of that a mouse clicked
	 * 
	 * @param row Row of click
	 * @param column Column of click
	 * @return Pane
	 */
	public Pane getPaneByRowColumnIndex(final int row, final int column) {
		Node result = null;
		ObservableList<Node> childrens = boardFX.getChildren();
		Iterator<Node> it = childrens.iterator();
		while (it.hasNext()) {
			Node node = it.next();
			int i, j;
			if (GridPane.getRowIndex(node) != null)
				i = GridPane.getRowIndex(node);
			else
				i = 0;
			if (GridPane.getColumnIndex(node) != null)
				j = GridPane.getColumnIndex(node);
			else
				j = 0;
			if (i == row && j == column) {
				result = node;
				break;
			}
		}
		return (Pane) result;
	}

	/**
	 * Changes the turn label after a move
	 */
	public void turnLabelAppearance() {
		Type turn = boardModel.getTurn();
		int whiteName = whiteNameLabel.getText().length();
		int blackName = blackNameLabel.getText().length();
		if (turn.equals(Type.WHITE)) {
			turnLabel.setText(whiteNameLabel.getText() + (whiteNameLabel.getText().charAt(whiteName-1) == 's' ? "' turn":  "'s turn"));
		} else if (turn.equals(Type.BLACK)) {
			turnLabel.setText(blackNameLabel.getText() + (blackNameLabel.getText().charAt(blackName-1) == 's' ? "' turn":  "'s turn"));
		}
	}
	
	/**
	 * Threaded Timers for competition mode
	 */
	public <T> void diffTimer2() {
		Task<T> t1 = new Task<T>() {

			@Override
			protected T call() throws Exception {
				int player1Time = 60*StartScreenController.minutes;

				long startTime = System.currentTimeMillis();
				while(player1Time >= 0 && !boardModel.isWhiteIsCheckmated()
						&&!boardModel.isBlackIsCheckmated() && !isStalemate) {
					if(getBoard().getTurn() == Type.WHITE) {
						//final int p1FTime = player1Time;
						updateMessage(player1Time/60+":"+String.format("%02d", (player1Time%60)));						
						player1Time--;						
					}
					long elapsedTime = System.currentTimeMillis()- startTime;
					Thread.sleep(1000-(elapsedTime%1000)+5);
				}		

				return null;
			}
			
		};
		p1Min.textProperty().bind(t1.messageProperty());
		t1.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				// TODO Auto-generated method stub
				if( !boardModel.isWhiteIsCheckmated()&&!boardModel.isBlackIsCheckmated()&&!isStalemate) {
					endGameLabel.setVisible(true);
					String finishStr = "Times up!\n"+blackNameLabel.getText()+" wins";
					endGameLabel.setText(finishStr);
					timesUp = true;
				}
			}
			
		});
		
		Task<T> t2 = new Task<T>() {

			@Override
			protected T call() throws Exception {
				int origTime = 60*StartScreenController.minutes;
				int player2Time = origTime;

				long startTime = System.currentTimeMillis();
				while(player2Time >= 0 && !boardModel.isWhiteIsCheckmated()
						&&!boardModel.isBlackIsCheckmated() && !isStalemate) {
					if(getBoard().getTurn() == Type.BLACK || player2Time == origTime) {
						updateMessage(player2Time/60+":"+String.format("%02d", (player2Time%60)));
						player2Time--;
					}
					long elapsedTime = System.currentTimeMillis()- startTime;
					Thread.sleep(1000-(elapsedTime%1000)+5);
				}	

				return null;
			}
			
		};
		p2Min.textProperty().bind(t2.messageProperty());
		t2.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				// TODO Auto-generated method stub
				if( !boardModel.isWhiteIsCheckmated()&&!boardModel.isBlackIsCheckmated()
												&& !isStalemate) {
					endGameLabel.setVisible(true);
					String finishStr = "Times up!\n"+whiteNameLabel.getText()+" wins";
					endGameLabel.setText(finishStr);
					timesUp = true;
				}
			}
			
		});
		Thread thread1 = new Thread(t1);
		Thread thread2 = new Thread(t2);

		thread1.setDaemon(true);
		thread2.setDaemon(true);
		thread1.start();
		thread2.start();

	}
	
	/**
	 * Gets the AI
	 * 
	 * @return
	 */
	public AI getMyAI() {
		return myAI;
	}

	/**
	 * Sets the AI
	 * 
	 * @param myAI
	 */
	public void setMyAI(AI myAI) {
		this.myAI = myAI;
	}

	/**
	 * Initializes the board class
	 */
	@FXML
	void initialize() {
		setTurn(Type.WHITE);
		isStalemate = false;
		isSuggesting = false;
		assert whiteNameLabel != null : "fx:id=\"whiteName\" was not injected: check your FXML file 'Board.fxml'.";
		assert blackNameLabel != null : "fx:id=\"blackName\" was not injected: check your FXML file 'Board.fxml'.";
		assert turnLabel != null : "fx:id=\"turnName\" was not injected: check your FXML file 'Board.fxml'.";
		assert boardFX != null : "fx:id=\"boardFX\" was not injected: check your FXML file 'Board.fxml'.";
		String blackNameString = StartScreenController.names.get(1),
				whiteNameString = StartScreenController.names.get(0);
		this.blackNameLabel.setWrapText(true);
		this.whiteNameLabel.setWrapText(true);
		this.blackNameLabel2.setWrapText(true);
		this.whiteNameLabel2.setWrapText(true);
		this.feedbackLabel.setVisible(false);
		this.timer1Pane.setVisible(false);
		this.timer2Pane.setVisible(false);
		this.suggestionButton.setVisible(false);
		this.infoButton.setVisible(false);
		this.infoPane.setVisible(false);
		this.blackNameLabel.setText(blackNameString);
		this.whiteNameLabel.setText(whiteNameString);
		this.blackNameLabel2.setText(blackNameString);
		this.whiteNameLabel2.setText(whiteNameString);
		String whiteTurn = whiteNameString + "'" + (whiteNameString.charAt(whiteNameString.length()-1) == 's' ? "" : "s") + " turn";
		turnLabel.setText(whiteTurn);
		checkLabel.setVisible(false);
		endGameLabel.setVisible(false);
		promotionPane.setVisible(false);
		timesUp = false;
		boardModel = new Board(whiteNameString, blackNameString);
		selectedPiece = null;
		availableMoves = new ArrayList<Coordinate>();
		if(StartScreenController.haveTimer) {
			this.timer1Pane.setVisible(true);
			this.timer2Pane.setVisible(true);
			diffTimer2();
		}
		else if(StartScreenController.giveFeedback) {
			feedbackLabel.setVisible(true);
			infoButton.setVisible(true);
			this.suggestionButton.setVisible(true);
		}
	}

	public Type getTurn() {
		return turn;
	}

	public void setTurn(Type turn) {
		this.turn = turn;
	}

}