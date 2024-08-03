package com.sadna.sadnamarket.domain.discountPolicies.Discounts;

import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import org.hibernate.Session;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "additiondiscount")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AdditionDiscount extends CompositeDiscount{
    public AdditionDiscount(int id, Discount discountA, Discount discountB) {
        super(id, discountA, discountB);
    }
    public AdditionDiscount(Discount discountA, Discount discountB) {
        super(discountA, discountB);
    }
    public AdditionDiscount() {
    }
    @Override
    public void giveDiscount(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice) {
        List<ProductDataPrice> newListProductsPrice= new ArrayList<>();
        double NewPriceA;
        double NewPriceB;
        ProductDataPrice currentProduct;
        List<Double> oldNewPrices = new ArrayList<>();
        for(ProductDataPrice productDataPrice : listProductsPrice){
            oldNewPrices.add(productDataPrice.getNewPrice());
            newListProductsPrice.add(productDataPrice.deepCopy());
        }
        //i kept giveDiscount and not giveDiscountWithoutCond on purpose

        discountA.giveDiscount(productDTOMap, listProductsPrice);
        discountB.giveDiscount(productDTOMap, newListProductsPrice);
        //loop over every item and merge both discounts
        for(int i = 0; i <listProductsPrice.size(); i++){
            currentProduct = listProductsPrice.get(i);
            NewPriceA  = currentProduct.getNewPrice();
            NewPriceB  = newListProductsPrice.get(i).getNewPrice();
            //NewPriceA = oldNewPrice - discountA; NewPriceB = oldNewPrice - discountB;
            //NewPriceA - (oldNewPrice - NewPriceB) = oldNewPrice - discountA - discountB
            currentProduct.setNewPrice(NewPriceA - (oldNewPrices.get(i) - NewPriceB));

        }
    }

    //gives both of them
    @Override
    public void giveDiscountWithoutCondition(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice) {
        List<ProductDataPrice> newListProductsPrice= new ArrayList<>();
        double NewPriceA;
        double NewPriceB;
        ProductDataPrice currentProduct;
        List<Double> oldNewPrices = new ArrayList<>();
        for(ProductDataPrice productDataPrice : listProductsPrice){
            oldNewPrices.add(productDataPrice.getNewPrice());
            newListProductsPrice.add(productDataPrice.deepCopy());
        }
        discountA.giveDiscountWithoutCondition(productDTOMap, listProductsPrice);
        discountB.giveDiscountWithoutCondition(productDTOMap, newListProductsPrice);
        //loop over every item and merge both discounts
        for(int i = 0; i <listProductsPrice.size(); i++){
            currentProduct = listProductsPrice.get(i);
            NewPriceA  = currentProduct.getNewPrice();
            NewPriceB  = newListProductsPrice.get(i).getNewPrice();
            //NewPriceA = oldNewPrice - discountA; NewPriceB = oldNewPrice - discountB;
            //NewPriceA - (oldNewPrice - NewPriceB) = oldNewPrice - discountA - discountB
            currentProduct.setNewPrice(NewPriceA - (oldNewPrices.get(i) - NewPriceB));

        }
    }

    @Override
    public boolean checkCond(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice) {
        return discountA.checkCond(productDTOMap, listProductsPrice) || discountB.checkCond(productDTOMap, listProductsPrice);
    }

    @Override
    public double giveTotalPriceDiscount(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice) {
        List<ProductDataPrice> newListProductsPriceA= new ArrayList<>();
        List<ProductDataPrice> newListProductsPriceB= new ArrayList<>();
        double countTotalNewNewPrice = 0;
        double countTotalOldNewPrice = 0;
        double NewPriceA;
        double NewPriceB;
        ProductDataPrice currentProduct;
        List<Double> oldNewPrices = new ArrayList<>();
        for(ProductDataPrice productDataPrice : listProductsPrice){
            countTotalOldNewPrice = countTotalOldNewPrice + productDataPrice.getNewPrice();
            oldNewPrices.add(productDataPrice.getNewPrice());
            newListProductsPriceA.add(productDataPrice.deepCopy());
            newListProductsPriceB.add(productDataPrice.deepCopy());

        }
        discountA.giveDiscount(productDTOMap, newListProductsPriceA);
        discountB.giveDiscount(productDTOMap, newListProductsPriceB);
        //loop over every item and merge both discounts
        for(int i = 0; i <newListProductsPriceA.size(); i++){
            currentProduct = newListProductsPriceA.get(i);
            NewPriceA  = currentProduct.getNewPrice();
            NewPriceB  = newListProductsPriceB.get(i).getNewPrice();
            //NewPriceA = oldNewPrice - discountA; NewPriceB = oldNewPrice - discountB;
            //NewPriceA - (oldNewPrice - NewPriceB) = oldNewPrice - discountA - discountB
            currentProduct.setNewPrice(NewPriceA - (oldNewPrices.get(i) - NewPriceB));
        }

        for(ProductDataPrice productDataPrice : newListProductsPriceA){
            countTotalNewNewPrice = countTotalNewNewPrice + productDataPrice.getNewPrice();
        }
        return countTotalOldNewPrice - countTotalNewNewPrice;
    }



    @Override
    public String description() {
        String description = "takes two discounts and apply both of the at the same time together\n";
        description = description + "discountA: " + discountA.description() + "\n";
        description = description + "discountB: " + discountB.description();
        return description;
    }

}
