package com.sadna.sadnamarket.domain.payment;

import org.springframework.web.reactive.function.BodyInserters;

public class WSEPHandshakeRequest extends WSEPRequest{
    public BodyInserters.FormInserter getBody() {
        return BodyInserters.fromFormData("action_type","handshake");
    }

}
