package com.sadna.sadnamarket.domain.discountPolicies;

import com.sadna.sadnamarket.domain.buyPolicies.BuyPolicyFacade;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DiscountPolicyManager  {
    protected DiscountPolicyFacade discountPolicyFacade;

    public DiscountPolicyManager(DiscountPolicyFacade facade) {
        this.discountPolicyFacade = facade;
    }

    public abstract boolean hasDiscountPolicy(int discountId);

    public abstract List<Integer> getDiscountIds();

    public abstract void addDiscountPolicy(int discountPolicyId) throws Exception;

    public abstract void removeDiscountPolicy(int discountPolicyId) throws Exception;

    public abstract List<ProductDataPrice> giveDiscount(List<CartItemDTO> cart, Map<Integer, ProductDTO> productDTOMap) throws Exception;
    public abstract void clear();
}
