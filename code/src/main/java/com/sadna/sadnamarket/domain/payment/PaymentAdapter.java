package com.sadna.sadnamarket.domain.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.Config;

import javax.management.ServiceNotFoundException;
import java.util.Calendar;

public class PaymentAdapter implements PaymentInterface{
    private ExternalPaymentAPI service;

    public PaymentAdapter() {
        this.service = new ExternalPaymentAPI();
    }

    @Override
    public boolean creditCardValid(CreditCardDTO creditDetails) throws JsonProcessingException {
        return pay(0, creditDetails, null);
    }

    private boolean checkService() throws JsonProcessingException {
        String resp = service.handshake();
        return resp.equals("OK");
    }

    @Override
    public boolean pay(double amount, CreditCardDTO payerCard, BankAccountDTO receiverAccount) throws JsonProcessingException {
        if(!checkService())
            return false;

        String creditCardNumber = payerCard.getCreditCardNumber();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(payerCard.getExpirationDate());
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        String ownerName = payerCard.getOwnerName();
        String cvv = payerCard.getDigitsOnTheBack();
        String ownerId = payerCard.getOwnerId();

        String resp = service.pay(amount, creditCardNumber, month, year, ownerName, cvv, ownerId);
        if(resp.equals("-1")) {
            return false;
        }
        if(resp.equals(ExternalPaymentAPI.ERROR_TIMEOUT)){
            throw new RuntimeException("Payment service has timed out");
        }
        return true;
    }

    public boolean implemented(){
        return Config.PAYMENT_ENABLE;
    }
}
