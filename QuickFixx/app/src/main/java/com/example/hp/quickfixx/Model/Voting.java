package com.example.hp.quickfixx.Model;

public class Voting {
    private String UserEmail,Vote,key;

    public String getUserEmail() {
        return UserEmail;
    }

    public String getVote() {
        return Vote;
    }

    public String getKey() {
        return key;
    }

    public void setUserEmail(String userEmail) {
        UserEmail = userEmail;
    }

    public void setVote(String vote) {
        Vote = vote;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
