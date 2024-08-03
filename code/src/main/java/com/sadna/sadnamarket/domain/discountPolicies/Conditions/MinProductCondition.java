package com.sadna.sadnamarket.domain.discountPolicies.Conditions;

import com.sadna.sadnamarket.domain.discountPolicies.Discounts.SimpleDiscount;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import org.hibernate.Session;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "MinProductCondition")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MinProductCondition extends Condition{
    @Column(name = "minAmount")
    private int minAmount;
    @Column(name = "productID")
    private Integer productID;
    @Column(name = "categoryName")
    private String categoryName;

    public MinProductCondition(int id, int minAmount){
        super(id);
        this.minAmount = minAmount;
        productID = null;
        categoryName = null;
    }
    public MinProductCondition(int minAmount){
        super();
        this.minAmount = minAmount;
        productID = null;
        categoryName = null;
    }
    public MinProductCondition(){}


    public void setOnProductName(int productID) {
        this.productID = productID;
    }

    public void setOnCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setOnStore() {
        ;
    }


    @Override
    public boolean checkCond(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice) {
        int totalAmount;
        if(productID != null){
            totalAmount = countOnlyProduct(productDTOMap, listProductsPrice);
        }
        else if(categoryName != null){
            totalAmount = countOnlyCategory(productDTOMap, listProductsPrice);
        }
        else{
            totalAmount = countAll(listProductsPrice);
        }
        return minAmount <= totalAmount;
    }
    private int countOnlyCategory(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice){
        int itemID;
        String thisCategoryName;
        int amount;
        int total = 0;
        for(ProductDataPrice item : listProductsPrice){
            itemID = item.getId();
            thisCategoryName = productDTOMap.get(itemID).getProductCategory();
            if(thisCategoryName.equals(categoryName)){
                amount = item.getAmount();
                total = total + amount;
            }
        }
        return total;
    }

    private int countOnlyProduct(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice){
        int itemID;
        int thisProductID;
        int amount;
        int total = 0;
        for(ProductDataPrice item : listProductsPrice){
            itemID = item.getId();
            thisProductID = productDTOMap.get(itemID).getProductID();
            if(thisProductID == productID){
                amount = item.getAmount();
                total = total + amount;
            }
        }
        return total;
    }

    private int countAll(List<ProductDataPrice> listProductsPrice){
        int amount;
        int total = 0;
        for(ProductDataPrice item : listProductsPrice){
            amount = item.getAmount();
            total = total + amount;
        }
        return total;
    }

    public String description() {
        String addEnding;
        if(productID != null){
            addEnding = String.format("product with ID <%d>", productID);
        }
        else if(categoryName != null){
            addEnding = "products from category: " + categoryName;
        }
        else{
            addEnding = "products";
        }
        return "the cart has at least " + minAmount + " " +addEnding;
    }

    @Override
    public Query getUniqueQuery(Session session) {
        Query query = session.createQuery("SELECT A FROM MinProductCondition A " +
                "WHERE A.minAmount = :minAmount " +
                "AND A.productID = :productID " +
                "AND A.categoryName = :categoryName " );
        query.setParameter("minAmount", minAmount);
        query.setParameter("productID", productID);
        query.setParameter("categoryName", categoryName);

        return query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinProductCondition that = (MinProductCondition) o;
        boolean sameCategory = Objects.equals(categoryName, that.categoryName);
        boolean sameProduct = Objects.equals(productID, that.productID);
        boolean sameMinAmount = Objects.equals(minAmount, that.minAmount);
        return sameCategory && sameProduct && sameMinAmount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minAmount, productID, categoryName);
    }
}
