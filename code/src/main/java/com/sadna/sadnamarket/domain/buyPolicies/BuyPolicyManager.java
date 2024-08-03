package com.sadna.sadnamarket.domain.buyPolicies;

import java.util.*;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;
import com.sadna.sadnamarket.service.Error;

public abstract class BuyPolicyManager {
    protected BuyPolicyFacade facade;

    public BuyPolicyManager(BuyPolicyFacade facade) {
        this.facade = facade;
    }

    public abstract boolean hasPolicy(int policyId);

    public abstract List<Integer> getAllPolicyIds();

    public abstract void addBuyPolicy(int buyPolicyId);

    public abstract void addLawBuyPolicy(int buyPolicyId);

    public abstract void removeBuyPolicy(int buyPolicyId);

    public abstract Set<String> canBuy(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user);
    public abstract void clear();
}
