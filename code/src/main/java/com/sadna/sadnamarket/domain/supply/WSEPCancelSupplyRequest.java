package com.sadna.sadnamarket.domain.supply;

import org.springframework.web.reactive.function.BodyInserters;

public class WSEPCancelSupplyRequest extends WSEPRequest{
    String transaction_id;

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    @Override
    public BodyInserters.FormInserter getBody() {
        return BodyInserters.fromFormData("action_type","cancel_supply").with("transaction_id",transaction_id);
    }
}
