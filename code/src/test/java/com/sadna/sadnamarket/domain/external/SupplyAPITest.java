package com.sadna.sadnamarket.domain.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.Config;
import com.sadna.sadnamarket.domain.orders.MemoryOrderRepository;
import com.sadna.sadnamarket.domain.payment.CreditCardDTO;
import com.sadna.sadnamarket.domain.payment.PaymentService;
import com.sadna.sadnamarket.domain.supply.AddressDTO;
import com.sadna.sadnamarket.domain.supply.SupplyProxy;
import com.sadna.sadnamarket.domain.supply.SupplyService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Date;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SupplyAPITest {

    SupplyService service;

    @BeforeAll
    void setUp(){
        service = SupplyService.getInstance();
        Config.SUPPLY_URL = "https://damp-lynna-wsep-1984852e.koyeb.app/";
        service.setController(new SupplyProxy());
    }

    @Test
    void supplySuccessTest() throws JsonProcessingException {
        Config.SUPPLY_ENABLE = true;
        AddressDTO addressDTO = new AddressDTO("Israel", "Rishon", "Street", "Street 2",
                "29382938", "Felix", "0520520520", "felix@gmail.com");
        String transaction = service.makeOrder(null,addressDTO);
        Assert.assertNotEquals("-1",transaction);
        Config.SUPPLY_ENABLE = false;
    }

    @Test
    void cancelSupplySuccessTest() throws JsonProcessingException {
        Config.SUPPLY_ENABLE = true;
        AddressDTO addressDTO = new AddressDTO("Israel", "Rishon", "Street", "Street 2",
                "29382938", "Felix", "0520520520", "felix@gmail.com");
        String transaction = service.makeOrder(null,addressDTO);
        Assert.assertTrue(service.cancelOrder(transaction));
        Config.SUPPLY_ENABLE = false;
    }

    @Test
    void cancelSupplyFailureTest() throws JsonProcessingException {
        Config.SUPPLY_ENABLE = true;
        AddressDTO addressDTO = new AddressDTO("Israel", "Rishon", "Street", "Street 2",
                "29382938", "Felix", "0520520520", "felix@gmail.com");
        String transaction = service.makeOrder(null,addressDTO);
        Assert.assertTrue(service.cancelOrder("transactionthatneverhappened"));
        Config.SUPPLY_ENABLE = false;
    }
}
