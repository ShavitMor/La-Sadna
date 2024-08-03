package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrBuyPolicy extends CompositeBuyPolicy{

    public OrBuyPolicy(int id, BuyPolicy policy1, BuyPolicy policy2) {
        super(id, policy1, policy2);
    }

    public OrBuyPolicy(BuyPolicy policy1, BuyPolicy policy2) {
        super(policy1, policy2);
    }

    public OrBuyPolicy() {
    }

    @Override
    public Set<String> canBuy(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user) {
        Set<String> res1 = policy1.canBuy(cart, products, user);
        Set<String> res2 = policy2.canBuy(cart, products, user);
        if(res1.isEmpty() || res2.isEmpty()) {
            return new HashSet<>();
        }
        res1.addAll(res2);
        return res1;
    }

    @Override
    protected boolean dependsOnUser() {
        return policy1.dependsOnUser() && policy2.dependsOnUser();
    }

    @Override
    public String getPolicyDesc() {
        return policy1.getPolicyDesc() + " OR " + policy2.getPolicyDesc();
    }

    @Override
    public BuyPolicyData generateData() {
        return new CompositeBuyPolicyData(policy1.getId(), policy2.getId(), "ORL");
    }
}
