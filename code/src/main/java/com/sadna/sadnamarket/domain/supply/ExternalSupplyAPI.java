package com.sadna.sadnamarket.domain.supply;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.Config;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.*;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

public class ExternalSupplyAPI {
    private WebClient client;
    private ObjectMapper mapper;
    public static String ERROR_TIMEOUT = "TIMEOUT";

    public ExternalSupplyAPI(){
        HttpClient timoutClient = HttpClient.create().responseTimeout(Duration.ofSeconds(10));
        client = WebClient.builder().baseUrl(Config.SUPPLY_URL)
                .clientConnector(new ReactorClientHttpConnector(timoutClient))
                .build();
        mapper = new ObjectMapper();
    }
    public String handshake() throws JsonProcessingException {
        WSEPHandshakeRequest request = new WSEPHandshakeRequest();
        return sendRequest(request);
    }

    public String supply(String name, String address, String city, String country, String zip) throws JsonProcessingException {
        WSEPSupplyRequest request = new WSEPSupplyRequest();
        request.setName(name);
        request.setAddress(address);
        request.setCity(city);
        request.setCountry(country);
        request.setZip(zip);
        return sendRequest(request);
    }

    public String cancelSupply(String transaction) throws JsonProcessingException {
        WSEPCancelSupplyRequest request = new WSEPCancelSupplyRequest();
        request.setTransaction_id(transaction);
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
