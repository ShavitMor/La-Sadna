package com.sadna.sadnamarket.domain.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.reactive.function.BodyInserters;

public class WSEPPayRequest extends WSEPRequest{
    double amount;
    String creditCardNumber;
    int expirationMonth;
    int expirationYear;
    String holderName;
    String cvv; // digits on the back;
    String holderId;

    public WSEPPayRequest(double amount, String creditCardNumber, int expirationMonth, int expirationYear, String holderName, String cvv, String holderId) {
        this.amount = amount;
        this.creditCardNumber = creditCardNumber;
        this.expirationMonth = expirationMonth;
        this.expirationYear = expirationYear;
        this.holderName = holderName;
        this.cvv = cvv;
        this.holderId = holderId;
    }


        @Override
    public BodyInserters.FormInserter getBody() {
        return BodyInserters.fromFormData("action_type","pay")
                .with("amount", String.valueOf(amount))
                .with("currency", "USD")
                .with("card_number", creditCardNumber)
                .with("month", String.valueOf(expirationMonth))
                .with("year", String.valueOf(expirationYear))
                .with("holder", holderName)
                .with("cvv", cvv)
                .with("id", holderId);
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public int getExpirationMonth() {
        return expirationMonth;
    }

    public void setExpirationMonth(int expirationMonth) {
        this.expirationMonth = expirationMonth;
    }

    public int getExpirationYear() {
        return expirationYear;
    }

    public void setExpirationYear(int expirationYear) {
        this.expirationYear = expirationYear;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getcvv() {
        return cvv;
    }

    public void setcvv(String cvv) {
        this.cvv = cvv;
    }

    public String getHolderId() {
        return holderId;
    }

    public void setHolderId(String holderId) {
        this.holderId = holderId;
    }
}
