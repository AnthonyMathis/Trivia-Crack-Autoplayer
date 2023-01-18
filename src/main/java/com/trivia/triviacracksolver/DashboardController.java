package com.trivia.triviacracksolver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;



public class DashboardController {
    private Data data = LoginController.getData();
    private String id;
    private String sessionID;
    private String username;
    private  JSONObject userDetails;
    private int lives;
    private int livesIncrement;
    private int delay = 5;
    private Autoplay autoplay;
    private Thread autoplayThread;
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Label nextLifeText;

    @FXML
    private TextField delayField;

    @FXML
    private TextArea console;

    @FXML
    private Button autoPlayButton;

    @FXML
    private Label loginText;

    @FXML
    private Label livesText;

    @FXML
    private ListView<String> allGamesList;


    @FXML
    public void initialize() throws IOException {
        setupDetails();
    }

    private void setupDetails() throws IOException {
      id = data.getID();
      sessionID = data.getSession();
      username = data.getUsername();
      userDetails = getUserDetails(id,sessionID);
      getLives(userDetails);
      try {
          getGameDetails(userDetails, allGamesList);
      }catch (Exception e) {

      }
        loginText.setText(loginText.getText() + " " + username);
        setLivesText(livesText,lives);
        console.appendText("Delay set to: 5 seconds!\n");
        determineNextLife(lives,livesIncrement,nextLifeText);
    }

    public static JSONObject getUserDetails(String userID, String sessionID) throws IOException {
        String url = "https://api.preguntados.com/api/users/" + userID + "/dashboard?app_config_version=0";
        URL myurl = new URL(url);
        HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Android/SDK-19(samsung SM-N950W) Package:com.etermax.preguntados.lite/Version:2.98.1");
        con.setRequestProperty("Eter-Agent", "1|And-And|samsung SM-N950W|1|Android 19|0|2.98.1|en|en|US|1");
        con.setRequestProperty("Cookie", "ap_session=" + sessionID);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        JSONObject details = new JSONObject(response.toString());
        return details;
    }

    private void getLives(JSONObject details){
       JSONObject livesObject = new JSONObject(details.get("lives").toString());
       lives = Integer.parseInt(livesObject.get("quantity").toString());
       try{
       livesIncrement = Integer.parseInt(livesObject.get("next_increment").toString());
       }catch (Exception e){
       }
    }

    public static void determineNextLife(int lives, int livesIncrement, Label nextLifeText){
        if(lives > 2){
            nextLifeText.setText("New Life at: N/A");
        }else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, livesIncrement);
            String timeOfDay;
            if(calendar.get(Calendar.AM_PM) == 1){
                timeOfDay = "PM";
            }else {
                timeOfDay = "AM";
            }
            String calendarHour;
            if(calendar.get(Calendar.HOUR) == 0 ){
                calendarHour = "12";
            }else {
                calendarHour = String.valueOf(calendar.get(Calendar.HOUR));
            }
            if(calendar.get(Calendar.MINUTE) < 10){
                nextLifeText.setText("New Life at: " +calendarHour + ":0" + calendar.get(Calendar.MINUTE) +" "+timeOfDay);
            }else{
                nextLifeText.setText("New Life at: " +calendarHour + ":" + calendar.get(Calendar.MINUTE) +" "+timeOfDay);
            }

        }

    }

    public static void setLivesText(Label livesText, int lives){
        livesText.setText("Lives available: "+lives);
    }

    public static void getGameDetails(JSONObject details, ListView<String> allGamesList){
        allGamesList.getItems().clear();
        JSONArray allGamesArray = new JSONArray(details.get("list").toString());
        for(int i = 0; i < allGamesArray.length(); i++){
            JSONObject individualGame = new JSONObject(allGamesArray.get(i).toString());
            String gameStatus = individualGame.get("game_status").toString();
            String gameID = individualGame.get("id").toString();
            String myTurn = individualGame.get("my_turn").toString();
            if(gameStatus.equalsIgnoreCase("ACTIVE") || gameStatus.equalsIgnoreCase("PENDING_APPROVAL")){
            String opponentName = getOpponentUsername(individualGame);
            int playerNumber = Integer.parseInt(individualGame.get("my_player_number").toString());
            JSONObject statisticsObject = new JSONObject(individualGame.get("statistics").toString());
            JSONObject playerOneStatistics = new JSONObject(statisticsObject.get("player_one_statistics").toString());
            int playerOneCrowns = Integer.parseInt(playerOneStatistics.get("crowns_won").toString());
            JSONObject playerTwoStatistics = new JSONObject(statisticsObject.get("player_two_statistics").toString());
            int playerTwoCrowns = Integer.parseInt(playerTwoStatistics.get("crowns_won").toString());
            String addGameString;
            if(playerNumber == 1){
                addGameString = "["+gameID+ "] Opponent: "+opponentName +" || Score: (Me) "+playerOneCrowns +" vs "+playerTwoCrowns;
            }else{
                addGameString = "["+gameID+ "] Opponent: "+opponentName +" || Score: (Me) "+playerTwoCrowns +" vs "+playerOneCrowns;
            }
                if(myTurn.equalsIgnoreCase("true")) {
                    allGamesList.getItems().add(addGameString + " (Status = READY)");
                }else{
                    allGamesList.getItems().add(addGameString+ " (Status = WAITING)");
                }
            }
        }
    }
    private static String getOpponentUsername(JSONObject individualGame){
        JSONObject opponent = new JSONObject(individualGame.get("opponent").toString());
        return opponent.get("username").toString();
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("login-view.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root, 500 , 400);
        stage.setTitle("Login");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void toggleAutoPlay(ActionEvent event) throws IOException, InterruptedException {
        if(autoPlayButton.getText().equalsIgnoreCase("Start")){
            autoPlayButton.setText("Stop");
            console.appendText("Starting autoplay!\n");
            String buttonState = autoPlayButton.getText().toString();
            autoplay = new Autoplay(userDetails,  delay,  sessionID,  id,buttonState, console, allGamesList,livesText,nextLifeText);
            autoplayThread = new Thread(autoplay);
            autoplayThread.start();
        }else {
            autoPlayButton.setText("Start");
            String buttonState = autoPlayButton.getText().toString();
            autoplay.setButtonState(buttonState);
            autoplayThread.interrupt();
        }
    }

    @FXML
    void setDelay(ActionEvent event) {
        try {
            if (delayField.getText().isEmpty() || delayField.getText() == "0") {
                console.appendText("Please input a delay!\n");
            } else {
                delay = Integer.parseInt(delayField.getText().toString());
                if(autoplay != null){
                    autoplay.setDelay(delay);
                }
                console.appendText("Delay set to: " + delay + " seconds!\n");
            }
        }catch (Exception e){

            console.appendText("Delay must be an integer!\n");
        }
    }

}

