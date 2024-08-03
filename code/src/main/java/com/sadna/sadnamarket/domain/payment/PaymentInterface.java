package com.sadna.sadnamarket.domain.payment;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Date;

public interface PaymentInterface {
    public boolean creditCardValid(CreditCardDTO creditDetails) throws JsonProcessingException;
    public boolean pay(double amount, CreditCardDTO payerCard, BankAccountDTO receiverAccount) throws JsonProcessingException;
}
