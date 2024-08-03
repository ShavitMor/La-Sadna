package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;
import com.sadna.sadnamarket.service.Error;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConditioningBuyPolicy extends CompositeBuyPolicy{
    // policy1 -> policy2 = !policy1 || policy2
    public ConditioningBuyPolicy(int id, BuyPolicy policy1, BuyPolicy policy2) {
        super(id, policy1, policy2);
    }

    public ConditioningBuyPolicy(BuyPolicy policy1, BuyPolicy policy2) {
        super(policy1, policy2);
    }

    public ConditioningBuyPolicy() {
    }

    @Override
    public Set<String> canBuy(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user) {
        Set<String> res1 = policy1.canBuy(cart, products, user);
        Set<String> res2 = policy2.canBuy(cart, products, user);
        if(user == null && policy1.dependsOnUser())
            return res2;

        Set<String> res = new HashSet<>();
        if(!(!res1.isEmpty() || res2.isEmpty())) {
            res.add(Error.makeConditioningBuyPolicyError(String.join("\n", res1), String.join("\n", res2)));
        }
        return res;
    }

    @Override
    public BuyPolicyData generateData() {
        return new CompositeBuyPolicyData(policy1.getId(), policy2.getId(), BuyPolicyTypeCodes.CONDITION);
    }

    @Override
    protected boolean dependsOnUser() {
        return policy1.dependsOnUser() || policy2.dependsOnUser();
    }

    @Override
    public String getPolicyDesc() {
        return "IF " + policy1.getPolicyDesc() + " THEN " + policy2.getPolicyDesc();
    }
}
