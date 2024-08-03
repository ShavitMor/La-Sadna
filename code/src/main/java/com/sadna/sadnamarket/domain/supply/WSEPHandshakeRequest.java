package com.sadna.sadnamarket.domain.supply;

import org.springframework.web.reactive.function.BodyInserters;

public class WSEPHandshakeRequest extends WSEPRequest{

    @Override
    public BodyInserters.FormInserter getBody() {
        return BodyInserters.fromFormData("action_type","handshake");
    }
}
