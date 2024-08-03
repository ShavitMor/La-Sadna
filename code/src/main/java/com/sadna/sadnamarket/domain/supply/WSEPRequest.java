package com.sadna.sadnamarket.domain.supply;

import org.springframework.web.reactive.function.BodyInserters.FormInserter;

public abstract class WSEPRequest {
    public abstract FormInserter getBody();
}
