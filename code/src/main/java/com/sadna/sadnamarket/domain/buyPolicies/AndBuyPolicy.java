package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AndBuyPolicy extends CompositeBuyPolicy{

    public AndBuyPolicy(int id, BuyPolicy policy1, BuyPolicy policy2) {
        super(id, policy1, policy2);
    }

    public AndBuyPolicy(BuyPolicy policy1, BuyPolicy policy2) {
        super(policy1, policy2);
    }

    public AndBuyPolicy() {
    }

    @Override
    public Set<String> canBuy(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user) {
        Set<String> res1 = policy1.canBuy(cart, products, user);
        Set<String> res2 = policy2.canBuy(cart, products, user);
        res1.addAll(res2);
        return res1;
    }

    @Override
    public BuyPolicyData generateData() {
        return new CompositeBuyPolicyData(policy1.getId(), policy2.getId(), BuyPolicyTypeCodes.AND);
    }

    @Override
    protected boolean dependsOnUser() {
        return policy1.dependsOnUser() || policy2.dependsOnUser();
    }

    @Override
    public String getPolicyDesc() {
        return policy1.getPolicyDesc() + " AND " + policy2.getPolicyDesc();
    }

}
