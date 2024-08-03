package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;
import java.util.Map;
import java.util.Objects;
@Entity
@DiscriminatorValue("PRODUCT")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProductSubject extends PolicySubject{
    private int productId;

    public ProductSubject(int productId) {
        this.productId = productId;
    }

    @Override
    public int subjectAmount(List<CartItemDTO> cart, Map<Integer, ProductDTO> products) {
        int productAmount = 0;
        for(CartItemDTO item : cart) {
            if(isSubject(products.get(item.getProductId())))
                productAmount += item.getAmount();
        }
        return productAmount;
    }

    @Override
    public boolean isSubject(ProductDTO product) {
        return product.getProductID() == this.productId;
    }

    @Override
    public String getSubject() {
        return String.valueOf(productId);
    }

    @Override
    public String getDesc() {
        return String.format("product with id %d", productId);
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductSubject that = (ProductSubject) o;
        return productId == that.productId;
    }

    @Override
    public String dataString() {
        return "P-"+productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
