package com.sadna.sadnamarket.domain.payment;

import java.util.Date;

public class CreditCardDTO {
    String creditCardNumber;
    String digitsOnTheBack;
    Date expirationDate;
    String ownerId;
    String ownerName;

    public CreditCardDTO(String creditCardNumber, String digitsOnTheBack, Date expirationDate, String ownerId) {
        this.creditCardNumber = creditCardNumber;
        this.digitsOnTheBack = digitsOnTheBack;
        this.expirationDate = expirationDate;
        this.ownerId = ownerId;
        this.ownerName = "israel";
    }

    public CreditCardDTO(){

    }

    public CreditCardDTO(String creditCardNumber, String digitsOnTheBack, Date expirationDate, String ownerId, String ownerName) {
        this.creditCardNumber = creditCardNumber;
        this.digitsOnTheBack = digitsOnTheBack;
        this.expirationDate = expirationDate;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public String getDigitsOnTheBack() {
        return digitsOnTheBack;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
