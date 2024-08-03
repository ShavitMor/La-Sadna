package com.sadna.sadnamarket.domain.discountPolicies.Discounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.HibernateUtil;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.Condition;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyFacade;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyManager;
import com.sadna.sadnamarket.domain.discountPolicies.HibernateDiscountPolicyManager;
import com.sadna.sadnamarket.service.Error;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.QueryHint;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HibernateDiscountPolicyRepository implements IDiscountPolicyRepository{

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public boolean discountPolicyExists(int policyId) {
        try {
            findDiscountPolicyByID(policyId);
            return true;
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Set<Integer> getAllPolicyIds() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Integer> res = session.createQuery( "select d.id from Discount d" ).list();
            return new HashSet<>(res);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Discount findDiscountPolicyByID(int policyId) throws Exception {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Discount discount = session.get(Discount.class, policyId);
            if (discount == null) {
                throw new IllegalArgumentException(Error.makeNoDiscountWithIdExistError(policyId));
            }
            return discount;
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public int addMaximumDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        Discount newDiscountPolicy = new MaximumDiscount(discountA, discountB);
        return addDiscountPolicy(newDiscountPolicy);
    }

    @Override
    public int addOrDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        Discount newDiscountPolicy = new OrDiscount(discountA, discountB);
        return addDiscountPolicy(newDiscountPolicy);
    }

    @Override
    public int addOnCategorySimpleDiscount(double percentage, String categoryName, Condition condition) throws JsonProcessingException {
        if(percentage >100 || percentage <0){
            throw new IllegalArgumentException(Error.percentageForDiscountIsNotInRange(percentage));
        }
        SimpleDiscount newDiscountPolicy = new SimpleDiscount(percentage, condition);
        newDiscountPolicy.setOnCategoryName(categoryName);
        return addDiscountPolicy(newDiscountPolicy);
    }

    @Override
    public int addOnProductSimpleDiscount(double percentage, int productID, Condition condition) throws JsonProcessingException {
        if(percentage >100 || percentage <0){
            throw new IllegalArgumentException(Error.percentageForDiscountIsNotInRange(percentage));
        }
        SimpleDiscount newDiscountPolicy = new SimpleDiscount(percentage, condition);
        newDiscountPolicy.setOnProductID(productID);
        return addDiscountPolicy(newDiscountPolicy);
    }

    @Override
    public int addOnStoreSimpleDiscount(double percentage, Condition condition) throws JsonProcessingException {
        if(percentage >100 || percentage <0){
            throw new IllegalArgumentException(Error.percentageForDiscountIsNotInRange(percentage));
        }
        SimpleDiscount newDiscountPolicy = new SimpleDiscount(percentage, condition);
        newDiscountPolicy.setOnStore();
        return addDiscountPolicy(newDiscountPolicy);
    }

    @Override
    public int addTakeMaxXorDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        XorDiscount newDiscountPolicy = new XorDiscount(discountA, discountB);
        newDiscountPolicy.setMax();
        return addDiscountPolicy(newDiscountPolicy);
    }

    @Override
    public int addTakeMinXorDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        XorDiscount newDiscountPolicy = new XorDiscount(discountA, discountB);
        newDiscountPolicy.setMin();
        return addDiscountPolicy(newDiscountPolicy);
    }

    @Override
    public int addAdditionDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        Discount newDiscountPolicy = new AdditionDiscount(discountA, discountB);
        return addDiscountPolicy(newDiscountPolicy);
    }

    @Override
    public int addAndDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        Discount newDiscountPolicy = new AndDiscount(discountA, discountB);
        return addDiscountPolicy(newDiscountPolicy);
    }

    @Override
    public int addDefaultDiscount(double percentage, Condition condition) throws JsonProcessingException {
        if(percentage >100 || percentage <0){
            throw new IllegalArgumentException(Error.percentageForDiscountIsNotInRange(percentage));
        }
        SimpleDiscount newDiscountPolicy = new SimpleDiscount(percentage, condition);
        newDiscountPolicy.setOnStore();
        newDiscountPolicy.setDefault();
        return addDiscountPolicy(newDiscountPolicy);
    }



    private int addDiscountPolicy(Discount discount){
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Discount existing = getExistingDiscount(session, discount);
            if(existing != null){
                transaction.commit();
                return existing.getId();
            }
            session.save(discount);
            transaction.commit();
            return discount.getId();
        }
        catch (Exception e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public DiscountPolicyManager createManager(DiscountPolicyFacade facade, int storeId) {
        return new HibernateDiscountPolicyManager(facade,storeId);
    }

    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    private Discount getExistingDiscount(Session session, Discount discount){
        Query query = discount.getUniqueQuery(session);
        List<Discount> res = query.list();
        if(res.isEmpty()){
            return null;
        }
        return res.get(0);
    }

    @Override
    public void clear() {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery( "delete from Discount").executeUpdate();
            session.createQuery( "delete from StoreDiscountPolicyRelation").executeUpdate();
            transaction.commit();
        }
        catch (Exception e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }
}
