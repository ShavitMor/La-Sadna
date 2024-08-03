package com.sadna.sadnamarket.domain.users;

public class RequestDTO extends NotificationDTO {
    
    private String senderName;
    private int storeId;
    private String role;

    public RequestDTO(Request request) {
        super(request.getMessage(), request.getDate(),request.getId());
        this.senderName = request.getSender();
        this.storeId = request.getStoreId();
        this.role = request.getRole();
    }
    public RequestDTO() {

    }
    public String getSenderName() {
        return senderName;
    }

    public int getStoreId() {
        return storeId;
    }

    public String getRole() {
        return role;
    }
}
