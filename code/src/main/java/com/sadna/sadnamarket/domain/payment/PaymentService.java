package com.sadna.sadnamarket.domain.payment;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PaymentService {
    private static PaymentService instance = null;
    private PaymentInterface controller;
    public static PaymentService getInstance(){
        if(instance == null){
            instance = new PaymentService();
        }
        return instance;
    }

    public PaymentService(){
        this.controller = new PaymentProxy();
    }

    public boolean checkCardValid(CreditCardDTO card){
        try {
            return controller.creditCardValid(card);
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public boolean pay(double amount, CreditCardDTO card, BankAccountDTO bank) throws JsonProcessingException {
        return controller.pay(amount, card, bank);
    }

    public void setController(PaymentInterface impl){
        this.controller = impl;
    }
}
