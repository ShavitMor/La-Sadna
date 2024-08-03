package com.sadna.sadnamarket.domain.discountPolicies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sadna.sadnamarket.domain.discountPolicies.Discounts.Discount;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.service.Error;

public class MemoryDiscountPolicyManager extends DiscountPolicyManager{
    private List<Integer>  discountIds;
    public MemoryDiscountPolicyManager(DiscountPolicyFacade discountPolicyFacade) {
        super(discountPolicyFacade);
        discountIds = new ArrayList<>();
    }

    @Override
    public synchronized boolean hasDiscountPolicy(int discountId) {
        return discountIds.contains(discountId);
    }

    public List<Integer> getDiscountIds() {
        return discountIds;
    }

    public synchronized void addDiscountPolicy(int discountPolicyId) throws Exception{
        if(discountIds.contains(discountPolicyId))
            throw new IllegalArgumentException(Error.makeDiscountPolicyAlreadyExistsError(discountPolicyId));
        discountIds.add(discountPolicyId);
    }

    public synchronized void removeDiscountPolicy(int discountPolicyId) throws Exception{
        if (!hasDiscountPolicy(discountPolicyId)) {
            throw new IllegalArgumentException(Error.makeNoDiscountWithIdExistInStoreError(discountPolicyId));
        }
        Discount discount = discountPolicyFacade.getDiscountPolicy(discountPolicyId);
        if(discount.isDefault()) {
            throw new IllegalArgumentException(Error.makeCannotRemoveDefaultDiscountFromStoreError(discountPolicyId));
        }
        discountIds.removeIf(id -> id == discountPolicyId);
    }

    public List<ProductDataPrice> giveDiscount(List<CartItemDTO> cart, Map<Integer, ProductDTO> productDTOMap) throws Exception {
        List<ProductDataPrice> listProductDataPrice = new ArrayList<>();
        //create the ProductDataPrices and add them to listProductDataPrice
        for (CartItemDTO cartItemDTO : cart) {
            ProductDTO pDTO = productDTOMap.get(cartItemDTO.getProductId());
            ProductDataPrice productDataPrice = new ProductDataPrice(cartItemDTO.getProductId(),cartItemDTO.getStoreId(), pDTO.getProductName(),
                    cartItemDTO.getAmount(),
                    pDTO.getProductPrice(), pDTO.getProductPrice());
            listProductDataPrice.add(productDataPrice);
        }
        for(Integer discountID : discountIds){
            Discount discount = discountPolicyFacade.getDiscountPolicy(discountID);
            discount.giveDiscount(productDTOMap, listProductDataPrice);
        }
        return listProductDataPrice;
    }

    @Override
    public void clear() {
        this.discountIds = new ArrayList<>();
    }

}
