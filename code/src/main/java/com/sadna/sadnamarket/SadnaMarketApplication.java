package com.sadna.sadnamarket;

import com.sadna.sadnamarket.api.Response;
import com.sadna.sadnamarket.domain.payment.BankAccountDTO;
import com.sadna.sadnamarket.domain.payment.CreditCardDTO;
import com.sadna.sadnamarket.domain.payment.PaymentInterface;
import com.sadna.sadnamarket.domain.payment.PaymentService;
import com.sadna.sadnamarket.domain.stores.IStoreRepository;
import com.sadna.sadnamarket.domain.stores.MemoryStoreRepository;
import com.sadna.sadnamarket.domain.stores.StoreDTO;
import com.sadna.sadnamarket.domain.supply.AddressDTO;
import com.sadna.sadnamarket.domain.supply.OrderDetailsDTO;
import com.sadna.sadnamarket.domain.supply.SupplyInterface;
import com.sadna.sadnamarket.domain.supply.SupplyService;
import com.sadna.sadnamarket.service.MarketService;
import com.sadna.sadnamarket.service.RealtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.AbstractEnvironment;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SadnaMarketApplication {
	public static void main(String[] args) {
		Config.read("config.json");
		System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME,"default");
		SpringApplication.run(SadnaMarketApplication.class, args);
	}

}
