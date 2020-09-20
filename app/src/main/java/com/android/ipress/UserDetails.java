package com.android.ipress;

import com.google.firebase.database.Exclude;

public class UserDetails {
    String uid,FullName,Email,Username,Key,PicUrl;

    public UserDetails(){
        //empty constructor needed
    }

    public UserDetails(String uid, String fullName, String email, String username, String PicUrl) {
        this.uid = uid;
        this.FullName = fullName;
        this.Email = email;
        this.Username = username;
        this.PicUrl = PicUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getFullName() {
        return FullName;
    }

    public String getEmail() {
        return Email;
    }

    public String getUsername() {
        return Username;
    }

    public String getPicUrl() {
        return PicUrl;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public void setPicUrl(String picUrl) {
        PicUrl = picUrl;
    }

    @Exclude
    public String getKey() {
        return Key;
    }

    @Exclude
    public void setKey(String key) {
        Key = key;
    }
}