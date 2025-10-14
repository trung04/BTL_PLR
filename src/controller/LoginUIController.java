/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.Parent;
import model.Message;

/**
 * FXML Controller class
 *
 * @author D E L L
 */
public class LoginUIController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private ClientController clientController;//

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    @FXML
    private void handleLogin() throws IOException, ClassNotFoundException {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            System.out.println(username + password);
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Vui lòng nhập đầy đủ thông tin.");
                return;
            }
            String[] credentials = {username, password};
            System.out.println("đã gủi");
            Message loginMessage = new Message("login", credentials);
            clientController.sendMessage(loginMessage);
            Message response = clientController.receiveMessage();
            if (response.getType().equals("login_success")) {
                errorLabel.setText("Đăng nhập thành công! ");
            } else {
                errorLabel.setText("Sai username hoặc password!");
            }
        } catch (Exception e) {
            System.out.println("Không thể kết nối với server");
        }

    }

    @FXML
    private void handleRegister() throws IOException, ClassNotFoundException {
        try {
            // Kiểm tra đường dẫn FXML
            System.out.println(getClass().getResource("/view/RegisterUI.fxml"));
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterUI.fxml"));
            Scene scene = new Scene(loader.load());

            // Lấy controller Register
//            RegisterController registerController = loader.getController();
//            registerController.setClientController(clientController); // inject ClientController đã tạo từ Login
            // Tạo Stage mới
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Phân loại rác - Đăng ký");
            stage.show();

            // Optional: đóng cửa sổ Login hiện tại
            ((Stage) usernameField.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

    public void showError(String error) {
        Platform.runLater(() -> {
            errorLabel.setText(error);
        });
    }
}
