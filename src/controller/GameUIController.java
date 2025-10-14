package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import model.TrashItem;

import java.io.*;
import java.net.*;

public class GameUIController {
    @FXML private Pane trashArea;
    @FXML private Label scoreLabel;
    @FXML private ImageView plasticBin, paperBin, metalBin;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private int score = 0;

    public void initConnection(String playerName) {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(this::listenServer).start();
            out.println(playerName);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void listenServer() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("📩 " + msg);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void initialize() { setupDragAndDrop(); }

    private void setupDragAndDrop() {
        TrashItem trash = new TrashItem("Chai nhựa", "plastic");
        Label trashLabel = new Label(trash.getName());
        trashLabel.setLayoutX(100); trashLabel.setLayoutY(150);
        trashLabel.setStyle("-fx-background-color: lightblue; -fx-padding: 5px;");
        trashArea.getChildren().add(trashLabel);

        trashLabel.setOnDragDetected(event -> {
            Dragboard db = trashLabel.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(trash.getType());
            db.setContent(content);
            event.consume();
        });

        plasticBin.setOnDragOver(e -> {
            if (e.getGestureSource() != plasticBin && e.getDragboard().hasString()) e.acceptTransferModes(TransferMode.MOVE);
            e.consume();
        });

        plasticBin.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if ("plastic".equals(db.getString())) {
                score += 10;
                scoreLabel.setText("Điểm: " + score);
                out.println("Phân loại đúng: nhựa");
            } else out.println("Phân loại sai!");
            e.setDropCompleted(true);
            e.consume();
        });
    }

    @FXML
    private void handleExit() {
        try { if (socket != null) socket.close(); System.exit(0); } catch (IOException e) { e.printStackTrace(); }
    }
}
