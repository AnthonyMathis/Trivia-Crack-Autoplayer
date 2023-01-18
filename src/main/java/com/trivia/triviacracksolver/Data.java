package com.trivia.triviacracksolver;

public class Data {
    private String id;
    private String session;
    private String username;

    public String getSession(){
        return session;
    }

    public String getID(){
        return id;
    }

    public String getUsername(){
        return username;
    }

    public void setSession(String sessionID){
        this.session = sessionID;
    }
    public void setID(String userID){
        this.id = userID;
    }

    public void setUsername(String usernameID){
        this.username = usernameID;
    }
}
