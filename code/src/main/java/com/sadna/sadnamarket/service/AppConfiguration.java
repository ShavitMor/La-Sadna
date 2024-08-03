package com.sadna.sadnamarket.service;

import com.sadna.sadnamarket.Config;
import com.sadna.sadnamarket.domain.stores.MemoryStoreRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;

@Configuration
public class AppConfiguration {

    @Lazy(true)
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Profile("default")
    public MarketService marketService(ObjectProvider<RealtimeService> innerBeanProvider){
        return new MarketService(innerBeanProvider.getObject());
    }


    @Lazy(true)
    @Bean
    @Profile("test")
    public MarketService testingService(ObjectProvider<RealtimeService> innerBeanProvider){
        Config.read("testconfig.json");
        return new MarketService(innerBeanProvider.getObject());
    }

    @Bean
    public RealtimeService realtimeService(){
        return new RealtimeService();
    }

    @Lazy(true)
    @Bean
    @Profile("test")
    public MarketServiceTestAdapter bridge(){
        return new MarketServiceTestAdapter();
    }

    @Lazy(true)
    @Bean
    @Profile("test")
    public MarketServiceTestAdapter memoryBridge(){
        Config.read("testconfig.json");
        MarketServiceTestAdapter bridge = new MarketServiceTestAdapter();
        bridge.reset();
        return bridge;
    }
}
