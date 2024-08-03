package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BuyPolicy {
    private Integer id;

    BuyPolicy(int id) {
        this.id = id;
    }

    BuyPolicy() {
        this.id = null;
    }

    public abstract Set<String> canBuy(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user);

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    protected abstract boolean dependsOnUser();

    public abstract String getPolicyDesc();

    public abstract Set<Integer> getPolicyProductIds();

    public abstract boolean equals(Object other);

    public abstract BuyPolicyData generateData();
}
