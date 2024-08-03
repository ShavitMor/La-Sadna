package com.sadna.sadnamarket.domain.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.Config;
import com.sadna.sadnamarket.domain.payment.WSEPHandshakeRequest;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

public class ExternalPaymentAPI {
    private WebClient client;
    private ObjectMapper mapper;
    public static String ERROR_TIMEOUT = "TIMEOUT";

    public ExternalPaymentAPI(){
        HttpClient timoutClient = HttpClient.create().responseTimeout(Duration.ofSeconds(10));
        client = WebClient.builder().baseUrl(Config.PAYMENT_URL)
                .clientConnector(new ReactorClientHttpConnector(timoutClient))
                .build();
        mapper = new ObjectMapper();
    }

    public String handshake() throws JsonProcessingException {
        WSEPHandshakeRequest request = new WSEPHandshakeRequest();
        return sendRequest(request);
    }

        public String pay(double amount, String creditCardNumber, int expirationMonth, int expirationYear, String holderName, String cvv, String holderId) throws JsonProcessingException {
        WSEPPayRequest request = new WSEPPayRequest(amount, creditCardNumber, expirationMonth, expirationYear, holderName, cvv, holderId);
        return sendRequest(request);
    }

    private String sendRequest(WSEPRequest request) {
        try {
            return client.post().uri("").body(request.getBody()).retrieve().bodyToMono(String.class).block();
        }catch (WebClientRequestException e){
            if(e.getCause() instanceof ReadTimeoutException){
                return ERROR_TIMEOUT;
            }
            throw e;
        }
    }
}
