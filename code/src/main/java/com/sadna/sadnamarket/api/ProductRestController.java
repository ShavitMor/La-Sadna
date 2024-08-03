package com.sadna.sadnamarket.api;

import com.sadna.sadnamarket.domain.stores.StoreFacade;
import com.sadna.sadnamarket.service.MarketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@Profile("default")
@CrossOrigin(origins = "*",allowedHeaders = "*") // Allow cross-origin requests from any source
@RequestMapping("/api/product")
public class ProductRestController {
    @Autowired
    MarketService marketService;

    @GetMapping("/getAllProducts")
    public Response getAllProducts(@RequestParam String username) {
        return marketService.getAllProducts(username);
    }

     @GetMapping("/getFilteredProducts")
    public Response getFilteredProducts(
        @RequestParam String productName,
        @RequestParam double minProductPrice,
        @RequestParam double maxProductPrice,
        @RequestParam String productCategory,
        @RequestParam double minProductRank
    ) {
        return marketService.getFilteredProducts(
            "", 
            productName, 
            minProductPrice, 
            maxProductPrice, 
            productCategory.equals("all") ? null : productCategory, 
            minProductRank
        );
    }
    @GetMapping("/getTopProducts")
    public Response getTopProducts() {
        return marketService.getTopProducts();
    }
    
}
