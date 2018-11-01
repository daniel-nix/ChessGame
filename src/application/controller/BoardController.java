package application.controller;

import java.util.Iterator;
import application.model.Coordinate;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class BoardController {
	private ImageView clickedPiece;
	private boolean aPieceHasBeenClicked;
	private Pane parentOfClickedPiece;

	@FXML
	private Label whiteName;

	@FXML
	private Label blackName;

	@FXML
	private GridPane boardFX;

	@FXML
	public void handlePieceClick(MouseEvent event) {
		if (event.getSource() instanceof Pane && parentOfClickedPiece != null && parentOfClickedPiece.equals(event.getSource())) {
			
			aPieceHasBeenClicked = true;
		} else {
			if (aPieceHasBeenClicked) {
				Coordinate a = findCoordinate(boardFX, event);
				try {
					Pane pane = (Pane)getNodeByRowColumnIndex(a.getRowIndex()
												, a.getColumnIndex(), boardFX);
					boardFX.getChildren().remove(clickedPiece);		
					pane.getChildren().add(clickedPiece);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
				clickedPiece = null;
				parentOfClickedPiece = null;
				aPieceHasBeenClicked = false;
			} else {
				if (event.getSource() instanceof ImageView) {
					clickedPiece = (ImageView) event.getSource();
					parentOfClickedPiece = (Pane) clickedPiece.getParent();
					aPieceHasBeenClicked = true;
				}
			}
		}

	}

	private static Coordinate findCoordinate(GridPane boardFX, MouseEvent event) {
		Coordinate a = new Coordinate();
		Node source;
		if(((Node)event.getSource()).getParent() instanceof GridPane) 
			source = (Node)event.getSource();
		else 
			source = ((Node)event.getSource()).getParent();
		
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
	
	public Node getNodeByRowColumnIndex (final int row, final int column, GridPane gridPane) {
	    Node result = null;
	    ObservableList<Node> childrens = gridPane.getChildren();
	    Iterator<Node> it = childrens.iterator();
	    while(it.hasNext()) {
	    	Node node = it.next();
	    	int i, j;
	    	if(GridPane.getRowIndex(node) != null)
	    		i = GridPane.getRowIndex(node);
	    	else
	    		i = 0;
	    	if(GridPane.getColumnIndex(node) != null)
	    		j = GridPane.getColumnIndex(node);
	    	else
	    		j = 0;
	        if(i == row && j == column) {
	            result = node;
	            break;
	        }
	    }
	    return result;
	}

	
	@FXML
	void initialize() {
		assert whiteName != null : "fx:id=\"whiteName\" was not injected: check your FXML file 'Board.fxml'.";
		assert blackName != null : "fx:id=\"blackName\" was not injected: check your FXML file 'Board.fxml'.";
		assert boardFX != null : "fx:id=\"boardFX\" was not injected: check your FXML file 'Board.fxml'.";

		aPieceHasBeenClicked = false;
		blackName.setText(StartScreenController.names.get(1));
		whiteName.setText(StartScreenController.names.get(0));

	}

}
