package com.trivia.triviacracksolver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class LoginController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    private String id;
    private String session;
    private String username;

    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;

    @FXML
    private PasswordField passwordField;

    private static Data data = new Data();

    public static Data getData(){
        return data;
    }

    @FXML
    void onLoginClick(ActionEvent event)  {
       try{
           login();
           data.setID(id);
           data.setSession(session);
           data.setUsername(username);
           switchToDashboard(event);
       } catch (IOException e) {
           errorLabel.setText("Error! Wrong login. Try again.");
       }
    }


    private void switchToDashboard(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("dashboard-view.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root, 660 , 620);
        stage.setTitle("Dashboard");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void login() throws IOException {
        String email = emailField.getText();
        String password = passwordField.getText();
        String url = "https://api.preguntados.com/api/login";
        String urlParameters = "{\"email\":\"" + email + "\",\"user_device\":{\"courier_service_type\":\"GCM\",\"device\":\"ANDROID\"},\"password\":\"" + password + "\",\"guest\":false,\"autogenerate_username\":false,\"validate_domain\":true}";
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        URL myurl = new URL(url);
        HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
        con.setDoOutput(true);
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Android/SDK-19(samsung SM-N950W) Package:com.etermax.preguntados.lite/Version:2.98.1");
        con.setRequestProperty("Eter-Agent", "1|And-And|samsung SM-N950W|1|Android 19|0|2.98.1|en|en|US|1");
        con.setRequestProperty("Content-Type", "application/json");
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(postData);
        }
        StringBuilder content;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String line;
            content = new StringBuilder();
            while ((line = in.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
            JSONObject loginDetails = new JSONObject(content.toString());
            username = loginDetails.get("username").toString();
            id = loginDetails.get("id").toString();

            JSONObject sessionDetails = new JSONObject(loginDetails.get("session").toString());
            session = sessionDetails.get("session").toString();



        }
    }
}