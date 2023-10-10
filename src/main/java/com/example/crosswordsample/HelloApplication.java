package com.example.crosswordsample;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        //Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        highlight.set(newLabel.get());
        highlight.addListener((observableValue, node, t1) -> {
            String nodeText = ((Label) node).getText();
            String t1Text = ((Label) t1).getText();
            ((Label) node).setText("@");
            ((Label) t1).setText("@");
            ((Label) node).setText(nodeText);
            ((Label) t1).setText(t1Text);
            //((Label) node).setBackground(Background.fill(Color.BLACK));
            //((Label) t1).setBackground(Background.fill(Color.BEIGE));
        });
        GridPane gridPane = grid;
        gridPane.setPadding(new Insets(5.0,5.0,5.0,5.0));
        gridPane.add(highlight.get(), 0,0);
        GridPane.setMargin(highlight.get(), insets);
        gridPane.setOnKeyPressed(gridKeyPressed);
        Scene scene = new Scene(gridPane, 700, 700);
        stage.setTitle("Crossword puzzle maker");
        stage.setScene(scene);
        stage.show();
        grid.requestFocus();
        TutorialScreen tutorialScreen = new TutorialScreen().displayOne();
    }

    public SimpleObjectProperty<Node> highlight = new SimpleObjectProperty<>(this, "highlight", new Label());

    public Supplier<Label> newLabel = () -> {
        Label label = new Label("");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 30.0));
        label.setBackground(Background.fill(Color.BLACK));
        label.setMinWidth(40);
        label.setMinHeight(40);
        label.setAlignment(Pos.CENTER);
        label.backgroundProperty().bind(label.textProperty().map(s -> {
            if (Objects.equals(getHighlight(s), label)) return Background.fill(Color.LAVENDER);
            return Background.fill(s.isEmpty() ? Color.BLACK : Color.BEIGE);
        }));
        label.setOnMouseClicked(mouseEvent -> highlight.set(label));
        return label;
    };
    public Node getHighlight(String s) {
        if (highlight == null) {
            return null;
        }
        return highlight.get();
    }

    public Insets insets = new Insets(0.3,0.3,0.3,0.3);
    public GridPane grid = new GridPane();
    public enum CellTraversal {
        LEFT(pair -> new Pair<>(pair.getKey()-1, pair.getValue()), pair -> new Pair<>(pair.getKey()+1, pair.getValue())),
        RIGHT(pair -> new Pair<>(pair.getKey()+1, pair.getValue()), pair -> new Pair<>(pair.getKey()-1, pair.getValue())),
        UP(pair -> new Pair<>(pair.getKey(), pair.getValue()-1), pair -> new Pair<>(pair.getKey(), pair.getValue()+1)),
        DOWN(pair -> new Pair<>(pair.getKey(), pair.getValue()+1), pair -> new Pair<>(pair.getKey(), pair.getValue()-1));
        public final UnaryOperator<Pair<Integer, Integer>> traversal;
        public UnaryOperator<Pair<Integer, Integer>> inverse;
        CellTraversal(UnaryOperator<Pair<Integer, Integer>> traversal) {
            this.traversal = traversal;
        }
        CellTraversal(UnaryOperator<Pair<Integer, Integer>> traversal, UnaryOperator<Pair<Integer, Integer>> inverse) {
            this.traversal = traversal;
            this.inverse = inverse;
        }
        public static HashMap<CellTraversal, CellTraversal> inverses = new HashMap<>(Map.ofEntries(
                Map.entry(LEFT, RIGHT),
                Map.entry(RIGHT, LEFT),
                Map.entry(UP, DOWN),
                Map.entry(DOWN, UP)
        ));
    }
    public CellTraversal typingTraversal = CellTraversal.RIGHT;

    public EventHandler<? super KeyEvent> gridKeyPressed = (EventHandler<KeyEvent>) keyEvent -> {
        Integer i = GridPane.getRowIndex(highlight.get());
        Integer j = GridPane.getColumnIndex(highlight.get());
        int R = grid.getRowCount();
        int C = grid.getColumnCount();
        UnaryOperator<Integer> r = I -> (I % grid.getRowCount() + grid.getRowCount()) % grid.getRowCount();
        UnaryOperator<Integer> c = J -> (J % grid.getColumnCount() + grid.getColumnCount()) % grid.getColumnCount();
        if (keyEvent.getCode().isLetterKey()) {
            ((Label) highlight.get()).setText(keyEvent.getText());
            //((Label) highlight.get()).setBackground(Background.fill(Color.LAVENDER));
            Pair<Integer, Integer> traversed = typingTraversal.traversal.apply(new Pair<>(j, i));
            highlight.set(getInGrid(grid, r.apply(traversed.getValue()), c.apply(traversed.getKey())));
            return;
        }
        if (keyEvent.getCode()== KeyCode.BACK_SPACE) {
            ((Label) highlight.get()).setText("");
            //((Label) highlight.get()).setBackground(Background.fill(Color.LAVENDER));
            Pair<Integer, Integer> traversed = typingTraversal.inverse.apply(new Pair<>(j, i));
            highlight.set(getInGrid(grid, r.apply(traversed.getValue()), c.apply(traversed.getKey())));
            return;
        }
        switch (keyEvent.getCode()) {
            case NUMPAD2 -> {
                typingTraversal = CellTraversal.DOWN;
            }
            case NUMPAD6 -> {
                typingTraversal = CellTraversal.RIGHT;
            }
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
        Arrays.stream(nodes).forEach(node -> GridPane.setMargin(node, insets));
    }

    public void insertColumn(GridPane pane, int j, Node... nodes) {
        pane.getChildren().forEach(node -> {
            Integer index = GridPane.getColumnIndex(node);
            if (index>=j) GridPane.setColumnIndex(node, index+1);
        });
        pane.addColumn(j, nodes);
        Arrays.stream(nodes).forEach(node -> GridPane.setMargin(node, insets));
    }

    public static void main(String[] args) {
        launch();
    }
}

class Utils {
    public static Supplier<Label> newLabel = () -> {
        Label label = new Label("");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 30.0));
        label.setBackground(Background.fill(Color.AZURE));
        label.setMinWidth(40);
        label.setMinHeight(40);
        label.setAlignment(Pos.CENTER);
        return label;
    };
}

class TutorialScreen {
    public GridPane grid;
    public Scene scene;
    public Stage stage;

    public TutorialScreen() {
        grid = new GridPane();
        scene = new Scene(grid);
        stage = new Stage();
        stage.setScene(scene);
    }

    public TutorialScreen displayOne() {
        Stream<String> stringStream = Stream.of("navigate cells", "add adjacent rows/columns", "enter letters into cells", "switch direction of entering letters");
        grid.addColumn(0, stringStream.map(s -> {
            Label label = Utils.newLabel.get();
            label.setText(s);
            label.setPadding(new Insets(10.0,10.0,10.0,10.0));
            return label;
        }).toArray(value -> new Node[4]));
        stringStream = Stream.of("arrow keys", "shift + arrow keys", "A-Z", "comma, period");
        grid.addColumn(1, stringStream.map(s -> {
            Label label = Utils.newLabel.get();
            label.setText(s);
            label.setPadding(new Insets(10.0,10.0,10.0,10.0));
            return label;
        }).toArray(value -> new Node[4]));
        stage.show();
        return this;
    }
}