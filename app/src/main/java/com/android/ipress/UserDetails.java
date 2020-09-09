package com.android.ipress;

import com.google.firebase.database.Exclude;

public class UserDetails {
    String FullName,Email,Username,Key,PicUrl;

    public UserDetails(){
        //empty constructor needed
    }

    public UserDetails(String fullName, String email, String username, String PicUrl) {
        this.FullName = fullName;
        this.Email = email;
        this.Username = username;
        this.PicUrl = PicUrl;
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

    public void setEmail(String email) {
        Email = email;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public void setUsername(String username) {
        Username = username;
    }

    @Exclude
    public String getKey() {
        return Key;
    }

    @Exclude
    public void setKey(String key) {
        Key = key;
    }

    static class Appliances{
        int appliance1,appliance2;
    }
}