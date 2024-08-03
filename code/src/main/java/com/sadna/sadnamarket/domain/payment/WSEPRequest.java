package com.sadna.sadnamarket.domain.payment;

import org.springframework.web.reactive.function.BodyInserters;

public abstract class WSEPRequest {
    public abstract BodyInserters.FormInserter getBody();
}