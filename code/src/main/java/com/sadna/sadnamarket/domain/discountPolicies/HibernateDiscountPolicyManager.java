package com.sadna.sadnamarket.domain.discountPolicies;

import com.sadna.sadnamarket.HibernateUtil;
import com.sadna.sadnamarket.domain.buyPolicies.BuyPolicyFacade;
import com.sadna.sadnamarket.domain.buyPolicies.BuyPolicyManager;
import com.sadna.sadnamarket.domain.buyPolicies.HibernateBuyPolicyManager;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.Discount;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.service.Error;
import jakarta.persistence.QueryHint;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HibernateDiscountPolicyManager extends DiscountPolicyManager{
    int storeId;
    public HibernateDiscountPolicyManager(DiscountPolicyFacade facade, int storeId) {
        super(facade);
        this.storeId = storeId;
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public boolean hasDiscountPolicy(int policyId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<StoreDiscountPolicyRelation> list = session.createQuery( "select p.policyId from StoreDiscountPolicyRelation p " +
                            "WHERE p.storeId = :storeId " +
                            "AND p.policyId = :policyId" )
                    .setParameter("storeId", storeId)
                    .setParameter("policyId",policyId).list();
            return !list.isEmpty();
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<Integer> getDiscountIds() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Integer> res = session.createQuery( "select p.policyId from StoreDiscountPolicyRelation p WHERE p.storeId = :storeId" ).setParameter("storeId",storeId).list();
            return res;
        }
        catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public void addDiscountPolicy(int discountPolicyId) throws Exception {
        if (hasDiscountPolicy(discountPolicyId))
            throw new IllegalArgumentException(Error.makeDiscountPolicyAlreadyExistsError(discountPolicyId));
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            StoreDiscountPolicyRelation relation = new StoreDiscountPolicyRelation(storeId, discountPolicyId);
            session.save(relation); // Save the store and get the generated ID
            transaction.commit();
        }
        catch (Exception e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public void removeDiscountPolicy(int discountPolicyId) throws Exception {
        if (!hasDiscountPolicy(discountPolicyId)) {
            throw new IllegalArgumentException(Error.makeNoDiscountWithIdExistInStoreError(discountPolicyId));
        }
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            List<Discount> checkDefault = session.createQuery( "select p from Discount p " +
                            "WHERE p.id = :id " +
                            "AND p.isDefault = :isDefault" )
                    .setParameter("id",discountPolicyId)
                    .setParameter("isDefault",false).list();
            // checkDefault is empty if the policy is default
            if(checkDefault.isEmpty()){
                throw new IllegalArgumentException(Error.makeCannotRemoveDefaultDiscountFromStoreError(discountPolicyId));
            }

            List<StoreDiscountPolicyRelation> list = session.createQuery( "select p from StoreDiscountPolicyRelation p " +
                            "WHERE p.storeId = :storeId " +
                            "AND p.policyId = :policyId" )
                    .setParameter("storeId",storeId)
                    .setParameter("policyId",discountPolicyId).list();
            session.delete(list.get(0));
            transaction.commit();
        }
        catch (Exception e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<ProductDataPrice> giveDiscount(List<CartItemDTO> cart, Map<Integer, ProductDTO> productDTOMap) throws Exception {
        List<ProductDataPrice> listProductDataPrice = new ArrayList<>();
        //create the ProductDataPrices and add them to listProductDataPrice
        for (CartItemDTO cartItemDTO : cart) {
            ProductDTO pDTO = productDTOMap.get(cartItemDTO.getProductId());
            ProductDataPrice productDataPrice = new ProductDataPrice(cartItemDTO.getProductId(),cartItemDTO.getStoreId(), pDTO.getProductName(),
                    cartItemDTO.getAmount(),
                    pDTO.getProductPrice(), pDTO.getProductPrice());
            listProductDataPrice.add(productDataPrice);
        }
        for(Integer discountID : getDiscountIds()){
            Discount discount = discountPolicyFacade.getDiscountPolicy(discountID);
            discount.giveDiscount(productDTOMap, listProductDataPrice);
        }
        return listProductDataPrice;
    }

    @Override
    public void clear() {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery( "delete from StoreDiscountPolicyRelation WHERE storeId = :storeId ")
                    .setParameter("storeId",storeId).executeUpdate();
            transaction.commit();
        }
        catch (Exception e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }
}
