/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;

/**
 * FXML Controller class
 *
 * @author D E L L
 */
public class RegisterUIController implements Initializable {

    /**
     * Initializes the controller class.
     *
     */
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtName;
    @FXML
    private Label lblStatus;
    private ClientController clientController;//

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void handleRegisterAction() {
        User user = new User(txtUsername.getText(), txtPassword.getText(), txtName.getText());

    }

    @FXML
    private void goToLogin() throws IOException {
        System.out.println(getClass().getResource("/view/LoginUI.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginUI.fxml"));
        Scene scene = new Scene(loader.load());
        LoginUIController loginController = loader.getController();
        ClientController clientController = new ClientController("localhost", 2208);
        loginController.setClientController(clientController);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Phân loại rác - Đăng nhập");
        stage.show();
        ((Stage) txtUsername.getScene().getWindow()).close();

    }

}
