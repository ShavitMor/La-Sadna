package com.sadna.sadnamarket.domain.discountPolicies.Discounts;

import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "maximumdiscount")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MaximumDiscount extends CompositeDiscount{
    public MaximumDiscount(int id, Discount discountA, Discount discountB) {
        super(id, discountA, discountB);
    }
    public MaximumDiscount(Discount discountA, Discount discountB) {
        super(discountA, discountB);
    }
    public MaximumDiscount(){}

    @Override
    public void giveDiscount(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> ListProductsPrice) {
        boolean condDiscountA =discountA.checkCond(productDTOMap,ListProductsPrice);
        boolean condDiscountB =discountB.checkCond(productDTOMap,ListProductsPrice);
        double totalDiscountA = discountA.giveTotalPriceDiscount(productDTOMap,ListProductsPrice);
        double totalDiscountB = discountB.giveTotalPriceDiscount(productDTOMap,ListProductsPrice);
        if(condDiscountA && condDiscountB){
            if(totalDiscountA > totalDiscountB){
                discountA.giveDiscountWithoutCondition(productDTOMap, ListProductsPrice);
            }
            else{
                discountB.giveDiscountWithoutCondition(productDTOMap, ListProductsPrice);
            }
        }
        else if(condDiscountA){
            discountA.giveDiscountWithoutCondition(productDTOMap, ListProductsPrice);
        }
        else if(condDiscountB){
            discountB.giveDiscountWithoutCondition(productDTOMap, ListProductsPrice);
        }

    }

    @Override
    public void giveDiscountWithoutCondition(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> ListProductsPrice) {
        boolean condDiscountA =discountA.checkCond(productDTOMap,ListProductsPrice);
        boolean condDiscountB =discountB.checkCond(productDTOMap,ListProductsPrice);
        double totalDiscountA = discountA.giveTotalPriceDiscount(productDTOMap,ListProductsPrice);
        double totalDiscountB = discountB.giveTotalPriceDiscount(productDTOMap,ListProductsPrice);
        if(totalDiscountA > totalDiscountB){
            discountA.giveDiscountWithoutCondition(productDTOMap, ListProductsPrice);
        }
        else{
            discountB.giveDiscountWithoutCondition(productDTOMap, ListProductsPrice);
        }
    }

    @Override
    public boolean checkCond(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> ListProductsPrice) {
        return discountA.checkCond(productDTOMap, ListProductsPrice) || discountB.checkCond(productDTOMap, ListProductsPrice);
    }

    @Override
    public double giveTotalPriceDiscount(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice) {
        boolean condDiscountA =discountA.checkCond(productDTOMap,listProductsPrice);
        boolean condDiscountB =discountB.checkCond(productDTOMap,listProductsPrice);
        double totalDiscountA = discountA.giveTotalPriceDiscount(productDTOMap,listProductsPrice);
        double totalDiscountB = discountB.giveTotalPriceDiscount(productDTOMap,listProductsPrice);
        if(condDiscountA && condDiscountB){
            return Math.max(totalDiscountA, totalDiscountB);
        }
        else if(condDiscountA){
            return totalDiscountA;
        }
        else if(condDiscountB){
            return totalDiscountB;
        }
        else {
            return 0;
        }
    }

    @Override
    public String description() {
        String description = "takes only one of two discounts based on if its condition is met and saves more money\n";
        description = description + "discountA: " + discountA.description() + "\n";
        description = description + "discountB: " + discountB.description() + "\n";
        return description;
    }

}
