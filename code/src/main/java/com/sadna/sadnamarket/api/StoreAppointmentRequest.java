package com.sadna.sadnamarket.api;

public class StoreAppointmentRequest {
    String appointer;
    String appointee;
    int storeId;

    public String getAppointer() {
        return appointer;
    }

    public void setAppointer(String appointer) {
        this.appointer = appointer;
    }

    public String getAppointee() {
        return appointee;
    }

    public void setAppointee(String appointee) {
        this.appointee = appointee;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }
}
