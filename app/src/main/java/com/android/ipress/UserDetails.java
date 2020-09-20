package com.android.ipress;

public class UserDetails {
    String uid,FullName,Email,Username,PicUrl;

    public UserDetails(){
        //empty constructor needed
    }

    public UserDetails(String uid,String fullName, String email, String username, String PicUrl) {
        this.uid = uid;
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
}