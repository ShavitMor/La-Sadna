package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.service.Error;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;
import java.util.Map;
import java.util.Objects;
public class CategorySubject extends PolicySubject{
    private String category;

    public CategorySubject(String category) {
        if(category == null || category.trim().equals(""))
            throw new IllegalArgumentException(Error.makeEmptyCategoryError());
        this.category = category;
    }


    @Override
    public int subjectAmount(List<CartItemDTO> cart, Map<Integer, ProductDTO> products) {
        int categoryAmount = 0;
        for(CartItemDTO item : cart) {
            int productId = item.getProductId();
            ProductDTO product = products.get(productId);
            if(isSubject(product))
                categoryAmount += item.getAmount();
        }
        return categoryAmount;
    }

    @Override
    public boolean isSubject(ProductDTO product) {
        return this.category.equals(product.getProductCategory());
    }

    @Override
    public String getSubject() {
        return category;
    }

    @Override
    public String getDesc() {
        return category;
    }

    @Override
    public int getProductId() {
        return -1;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategorySubject that = (CategorySubject) o;
        return Objects.equals(category, that.category);
    }

    @Override
    public String dataString() {
        return "C-"+category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(category);
    }
}
