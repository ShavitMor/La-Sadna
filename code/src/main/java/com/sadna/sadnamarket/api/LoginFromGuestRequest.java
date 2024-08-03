package com.sadna.sadnamarket.api;

public class LoginFromGuestRequest {
    private String username;
    private String password;
    private int guestId;
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public int getGuestId() {
        return guestId;
    }   
    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }
}
