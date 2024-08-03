package com.sadna.sadnamarket.domain.payment;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PaymentProxy implements PaymentInterface{
    PaymentAdapter real;

    public PaymentProxy(){
        real = new PaymentAdapter();
    }

    @Override
    public boolean creditCardValid(CreditCardDTO creditDetails) throws JsonProcessingException {
        if(real.implemented()){
            return real.creditCardValid(creditDetails);
        }
        return true;
    }

    @Override
    public boolean pay(double amount, CreditCardDTO payerCard, BankAccountDTO receiverAccount) throws JsonProcessingException {
        if(real.implemented()){
            return real.pay(amount, payerCard, receiverAccount);
        }
        return true;
    }
}
