package com.trivia.triviacracksolver;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Questions {
    public static JSONObject answerQuestion(String sessionID,String playerID, String gameID, String questionCategory, String questionID, String correctAnswer, String questionType) throws IOException {
        String url = "https://api.preguntados.com/api/users/" + playerID + "/games/" + gameID + "/v2/answers";
        String urlParameters = "{\"answers\":[{\"category\":\"" + questionCategory + "\",\"power_ups\":[],\"second_chance_question\":false,\"id\":" + questionID + ",\"answer\":" + correctAnswer + "}],\"type\":\""+questionType+"\"}";
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        URL myurl = new URL(url);
        HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Android/SDK-19(samsung SM-N950W) Package:com.etermax.preguntados.lite/Version:2.98.1");
        con.setRequestProperty("Eter-Agent", "1|And-And|samsung SM-N950W|1|Android 19|0|2.98.1|en|en|US|1");
        con.setRequestProperty("Cookie", "ap_session=" + sessionID);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
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
        JSONObject individualGame = new JSONObject(content.toString());
        return individualGame;
    }
}
