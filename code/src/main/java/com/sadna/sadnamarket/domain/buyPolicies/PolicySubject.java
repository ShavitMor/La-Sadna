package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
public abstract class PolicySubject {

    public PolicySubject() {}

    public abstract int subjectAmount(List<CartItemDTO> cart, Map<Integer, ProductDTO> products);
    public abstract boolean isSubject(ProductDTO product);
    public abstract String getSubject();
    public abstract String getDesc();
    public abstract int getProductId();
    public abstract boolean equals(Object other);

    public abstract String dataString();
}
