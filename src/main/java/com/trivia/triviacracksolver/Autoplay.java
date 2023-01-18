package com.trivia.triviacracksolver;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Autoplay  implements Runnable{
    private JSONObject details;
    private int delay;
    private String session;
    private String id;
    private String buttonState;
    private Label livesText;
    private Label nextLifeText;

    private TextArea console;
    private ListView<String> allGamesList;

    public Autoplay(JSONObject allDetails, int monitorDelay, String sessionID, String playerID, String buttonState, TextArea console, ListView<String > allGamesList, Label livesText, Label nextLifeText) {
        this.details = allDetails;
        this.delay = monitorDelay;
        this.session = sessionID;
        this.id = playerID;
        this.buttonState = buttonState;
        this.console = console;
        this.allGamesList = allGamesList;
        this.livesText = livesText;
        this.nextLifeText = nextLifeText;
    }

    @Override
    public void run() {
        try {
            startGames();
        } catch (IOException | InterruptedException e) {
            console.appendText("Autoplay turned off!\n");
        }
    }

    public void setButtonState(String buttonState){
        this.buttonState = buttonState;
    }

    public void setDelay(int delay){
        this.delay = delay;
    }
    public void startGames() throws InterruptedException, IOException {
        while(buttonState.equalsIgnoreCase("Stop")) {
        details = DashboardController.getUserDetails(id,session);
        try {
            autoPlayGames();
        }catch (Exception e){
        }
        Thread.sleep(delay * 1000);
        }

    }
    private void autoPlayGames() throws IOException, InterruptedException {
        JSONArray allGamesArray;
        try {
             allGamesArray = new JSONArray(details.get("list").toString());
        }catch (Exception e){
            createGame();
            allGamesArray = new JSONArray(details.get("list").toString());
        }
        boolean readyStatus = false;
        for (int i = 0; i < allGamesArray.length(); i++) {
            boolean sameOpponent = false;
            JSONObject individualGame = new JSONObject(allGamesArray.get(i).toString());
            String gameStatus = individualGame.get("game_status").toString();
            String myTurn = individualGame.get("my_turn").toString();
            String gameID = individualGame.get("id").toString();
            while(buttonState.equalsIgnoreCase("Stop") && (gameStatus.equalsIgnoreCase("ACTIVE") || gameStatus.equalsIgnoreCase("PENDING_APPROVAL"))
                    && myTurn.equalsIgnoreCase("true")){
                String opponentName = individualGame.getJSONObject("opponent").get("username").toString();
                    readyStatus = true;
                    if(!sameOpponent) {
                        console.appendText("[Autoplay] Found playable match against: " + opponentName + "!\n");
                        sameOpponent = true;
                    }
                    String spinData = individualGame.getJSONObject("spins_data").getJSONArray("spins").get(0).toString();
                    JSONObject allQuestions = new JSONObject(spinData);
                    String questionType = allQuestions.get("type").toString();
                    String question = allQuestions.getJSONArray("questions").get(0).toString();
                    JSONObject questionData = new JSONObject(question);
                    String questionID = questionData.getJSONObject("question").get("id").toString();
                    String correctAnswer = questionData.getJSONObject("question").get("correct_answer").toString();
                    String questionCategory = questionData.getJSONObject("question").get("category").toString();
                    individualGame = Questions.answerQuestion(session,id,gameID,questionCategory,questionID,correctAnswer,questionType);
                    myTurn = individualGame.get("my_turn").toString();
                    String newGameStatus = individualGame.get("game_status").toString();
                    String finalText = "";

                    details = DashboardController.getUserDetails(id,session);

                JSONObject livesObject = new JSONObject(details.get("lives").toString());
                int lives = Integer.parseInt(livesObject.get("quantity").toString());


                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        DashboardController.getGameDetails(details,allGamesList);
                        DashboardController.setLivesText(livesText,(lives));
                        try {
                            int livesIncrement = Integer.parseInt(livesObject.get("next_increment").toString());
                            DashboardController.determineNextLife((lives), livesIncrement, nextLifeText);

                        }catch (Exception e){

                        }
                    }
                });
                    if (newGameStatus.equalsIgnoreCase("ENDED")) {
                        finalText = "[Autoplay] Game over! You beat: " + opponentName;
                        console.appendText(finalText + "\n");
                        break;
                    } else if (questionType.equalsIgnoreCase("CROWN")) {
                        finalText = "[Autoplay] Game ID: " + gameID + " || Round over! Question type: CROWN";
                    } else if (questionType.equalsIgnoreCase("NORMAL")) {
                        finalText = "[Autoplay] Game ID: " + gameID + " || Round over! Question type: NORMAL";
                    }
                    console.appendText(finalText+"\n");
                    if (!myTurn.equalsIgnoreCase("true")) {
                        console.appendText("Turn over with: "+opponentName+"\n");
                    }
                    Thread.sleep(delay * 1000);
            }
        }
        if(!readyStatus){
            createGame();
        }
    }

    private void createGame() throws IOException {
        details = DashboardController.getUserDetails(id,session);
        JSONObject livesObject = new JSONObject(details.get("lives").toString());
        int lives = Integer.parseInt(livesObject.get("quantity").toString());

        if(lives > 0){
            URL url = new URL("https://api.preguntados.com/api/users/" + id + "/games");
            String urlParameters = "{\"language\":\"EN\",\"opponent_selection_type\":\"RANDOM\",\"type\":\"NORMAL\"}";
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Android/SDK-19(samsung SM-N950W) Package:com.etermax.preguntados.lite/Version:2.98.1");
            con.setRequestProperty("Eter-Agent", "1|And-And|samsung SM-N950W|1|Android 19|0|2.98.1|en|en|US|1");
            con.setRequestProperty("Cookie", "ap_session=" + session);
            con.setRequestProperty("Content-Type","application/json");
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
            }
            details = DashboardController.getUserDetails(id,session);
            JSONObject newLivesObject = new JSONObject(details.get("lives").toString());
            int newLives = Integer.parseInt(livesObject.get("quantity").toString());
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    console.appendText("[Autoplay] New game created!\n");
                    DashboardController.getGameDetails(details,allGamesList);
                    DashboardController.setLivesText(livesText,newLives);
                    try {
                        int livesIncrement = Integer.parseInt(newLivesObject.get("next_increment").toString());
                        DashboardController.determineNextLife(newLives, livesIncrement, nextLifeText);
                    }catch (Exception e){

                    }
                    }
            });

        }else{
            console.appendText("[Autoplay] No games available to play! Sleeping for " + delay + " second(s)\n");
        }
    }

}
