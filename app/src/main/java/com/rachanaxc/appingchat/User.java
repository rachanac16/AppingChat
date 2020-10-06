package com.rachanaxc.appingchat;

import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

    String uId;
    String name = "anonymous";
    HashMap<String, HashMap<String, String>> conversations;
    String email;
    String lastMsg;
    String status="";

    public User(){}

    public User(HashMap<String, HashMap<String, String>> conversations) {
        this.conversations = conversations;
    }

    public User(String name, String email, String status){
        this.name = name;
        this.email = email;
        this.status =status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public HashMap<String, HashMap<String, String>> getConversations() {
        return conversations;
    }

    public void setConversations(HashMap<String, HashMap<String, String>> conversations) {
        this.conversations = conversations;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getuId() {
        return uId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }
}
