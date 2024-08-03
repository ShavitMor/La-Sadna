package com.sadna.sadnamarket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.api.Response;
import com.sadna.sadnamarket.domain.users.RequestDTO;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Config {
    public static String BEGIN;
    public static boolean HAS_STATE = false;
    public static String SYSMAN;
    public static String SUPPLY_URL;
    public static String PAYMENT_URL;
    public static String DB_URL = "jdbc:mysql://sadnadb.c1ueowgcy9zp.eu-west-3.rds.amazonaws.com:3306/sadna_test?useSSL=true";
    //DB_URL is Test by default so that the DB Integration/Unit tests will automatically use the test DB
    public static boolean SUPPLY_ENABLE = false;
    public static boolean PAYMENT_ENABLE = false;
    public static boolean CLEAR = false;
    public static int CONCURRENT_LOOPS = 0;
    public static boolean TESTING_MODE=false;


    public static void read(String path){
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + path);
            }
            JsonNode conf = objectMapper.readTree(inputStream);
            if(!conf.has("sysman")){
                throw new IllegalArgumentException("System must have a system manager");
            }
            if(!conf.has("db")){
                throw new IllegalArgumentException("System must have db");
            }
            Config.SYSMAN = conf.get("sysman").asText();
            Config.DB_URL = conf.get("db").asText();
            if(conf.has("clear")){
                Config.CLEAR = conf.get("clear").asText().equals("on");
            }else{
                Config.CLEAR = false;
            }
            if(conf.has("supply")){
                Config.SUPPLY_URL = conf.get("supply").asText();
                SUPPLY_ENABLE = true;
            }else{
                SUPPLY_ENABLE = false;
            }
            if(conf.has("payment")){
                Config.PAYMENT_URL = conf.get("payment").asText();
                PAYMENT_ENABLE = true;
            }else{
                PAYMENT_ENABLE = false;
            }
            if(conf.has("state")){
                Config.BEGIN = conf.get("state").asText();
                HAS_STATE = true;
            }else{
                HAS_STATE = false;
            }
            if(conf.has("concurrency_loop")){
                Config.CONCURRENT_LOOPS = conf.get("concurrency_loop").asInt();
            }
            if(conf.has("testing")){
                Config.TESTING_MODE = conf.get("testing").asText().equals("on");
            }else{
                TESTING_MODE = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
