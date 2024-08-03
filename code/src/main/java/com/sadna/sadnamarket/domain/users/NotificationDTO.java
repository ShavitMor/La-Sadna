package com.sadna.sadnamarket.domain.users;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationDTO {
    private String message;
    private int id;
    private String date;

    public NotificationDTO(String message,LocalDateTime date,int id) {
        this.message=message;
        this.date=date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.id=id;
    }

    public NotificationDTO(){

    }

    public NotificationDTO(Notification notific) {
        this.message = notific.getMessage();
        this.date = notific.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.id=notific.getId();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "NotificationDTO{" +
                "message='" + message + '\'' +
                ", date=" + date +
                '}';
    }
}
