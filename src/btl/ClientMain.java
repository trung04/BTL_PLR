/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package btl;

import controller.ClientController;
import controller.LoginUIController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println(getClass().getResource("/view/LoginUI.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginUI.fxml"));
        Scene scene = new Scene(loader.load());
        LoginUIController loginController = loader.getController();
        ClientController clientController = new ClientController("localhost", 2208);
        loginController.setClientController(clientController);
        stage.setScene(scene);
        stage.setTitle("Phân loại rác - Đăng nhập");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
