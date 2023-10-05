package com.example.crosswordsample;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        //Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        highlight.addListener((observableValue, node, t1) -> {
            ((Label) node).setBackground(Background.fill(Color.BLACK));
            ((Label) t1).setBackground(Background.fill(Color.BEIGE));
        });
        GridPane gridPane = grid;
        gridPane.add(highlight.get(), 0,0);
        gridPane.setOnKeyPressed(gridKeyPressed);
        Scene scene = new Scene(gridPane, 700, 700);
        stage.setTitle("Crossword puzzle maker");
        stage.setScene(scene);
        stage.show();
        grid.requestFocus();
    }

    public Supplier<Label> newLabel = () -> {
        Label label = new Label("Ο");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 30.0));
        label.setBackground(Background.fill(Color.BLACK));
        return label;
    };

    public SimpleObjectProperty<Node> highlight = new SimpleObjectProperty<>(this, "highlight", newLabel.get());
    public GridPane grid = new GridPane();
    public enum CellTraversal {
        LEFT(pair -> new Pair<>(pair.getKey()-1, pair.getValue())),
        RIGHT(pair -> new Pair<>(pair.getKey()+1, pair.getValue())),
        UP(pair -> new Pair<>(pair.getKey(), pair.getValue()-1)),
        DOWN(pair -> new Pair<>(pair.getKey(), pair.getValue()+1));
        public final UnaryOperator<Pair<Integer, Integer>> traversal;
        CellTraversal(UnaryOperator<Pair<Integer, Integer>> traversal) {
            this.traversal = traversal;
        }
    }

    public EventHandler<? super KeyEvent> gridKeyPressed = (EventHandler<KeyEvent>) keyEvent -> {
        Integer i = GridPane.getRowIndex(highlight.get());
        Integer j = GridPane.getColumnIndex(highlight.get());
        int R = grid.getRowCount();
        int C = grid.getColumnCount();
        UnaryOperator<Integer> r = I -> (I % grid.getRowCount() + grid.getRowCount()) % grid.getRowCount();
        UnaryOperator<Integer> c = J -> (J % grid.getColumnCount() + grid.getColumnCount()) % grid.getColumnCount();
        if (keyEvent.getCode().isLetterKey()) {
            ((Label) highlight.get()).setText(keyEvent.getText());
            ((Label) highlight.get()).setBackground(Background.fill(Color.LAVENDER));
            highlight.set(getInGrid(grid, i, c.apply(j + 1)));
            return;
        }
        switch (keyEvent.getCode()) {
            case LEFT -> {
                if (keyEvent.isShiftDown()) {
                    ArrayList<Node> nodes = new ArrayList<>();
                    for (int k = 0; k < R; k++) {
                        nodes.add(newLabel.get());
                    }
                    //grid.addColumn(j-1, nodes.toArray(new Node[]{}));
                    insertColumn(grid, j-1, nodes.toArray(new Node[]{}));
                }
                highlight.set(getInGrid(grid, i, c.apply(j - 1)));
            }
            case RIGHT -> {
                if (keyEvent.isShiftDown()) {
                    ArrayList<Node> nodes = new ArrayList<>();
                    for (int k = 0; k < R; k++) {
                        nodes.add(newLabel.get());
                    }
                    //grid.addColumn(j+1, nodes.toArray(new Node[]{}));
                    insertColumn(grid, j+1, nodes.toArray(new Node[]{}));
                }
                highlight.set(getInGrid(grid, i, c.apply(j + 1)));
            }
            case UP -> {
                if (keyEvent.isShiftDown()) {
                    ArrayList<Node> nodes = new ArrayList<>();
                    for (int k = 0; k < C; k++) {
                        nodes.add(newLabel.get());
                    }
                    //grid.addRow(i-1, nodes.toArray(new Node[]{}));
                    insertRow(grid, i-1, nodes.toArray(new Node[]{}));
                }
                highlight.set(getInGrid(grid, r.apply(i - 1), j));
            }
            case DOWN -> {
                if (keyEvent.isShiftDown()) {
                    ArrayList<Node> nodes = new ArrayList<>();
                    for (int k = 0; k < C; k++) {
                        nodes.add(newLabel.get());
                    }
                    //grid.addRow(i+1, nodes.toArray(new Node[]{}));
                    insertRow(grid, i+1, nodes.toArray(new Node[]{}));
                }
                highlight.set(getInGrid(grid, r.apply(i + 1), j));
            }
        }
    };

    public Node getInGrid(GridPane pane, int row, int col) {
        Optional<Node> any = pane.getChildren().stream().filter(node -> GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col).findAny();
        return any.get();
    }

    public void insertRow(GridPane pane, int i, Node... nodes) {
        pane.getChildren().forEach(node -> {
            Integer index = GridPane.getRowIndex(node);
            if (index>=i) GridPane.setRowIndex(node, index+1);
        });
        pane.addRow(i, nodes);
    }

    public void insertColumn(GridPane pane, int j, Node... nodes) {
        pane.getChildren().forEach(node -> {
            Integer index = GridPane.getColumnIndex(node);
            if (index>=j) GridPane.setColumnIndex(node, index+1);
        });
        pane.addColumn(j, nodes);
    }

    public static void main(String[] args) {
        launch();
    }
}