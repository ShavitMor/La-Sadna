package com.sadna.sadnamarket.domain.discountPolicies.Conditions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.HibernateUtil;
import com.sadna.sadnamarket.domain.stores.Store;
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

public class HibernateConditionRepository implements IConditionRespository{
    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public boolean conditionExists(int condId) {
        try {
            findConditionByID(condId);
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
    public Set<Integer> getAllConditionsIds() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Integer> res = session.createQuery( "select c.id from Condition c" ).list();
            return new HashSet<>(res);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Condition findConditionByID(int condId) throws Exception {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Condition condition = session.get(Condition.class, condId);
            if (condition == null) {
                throw new IllegalArgumentException(Error.makeNoConditionWithIdExistError(condId));
            }
            return condition;
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public int createMinBuyCondition(int minBuy) throws JsonProcessingException {
        if(minBuy < 0){
            throw new IllegalArgumentException(Error.CannotMakeNegativeMinBuyCondition(minBuy));
        }
        Condition newCondition = new MinBuyCondition(minBuy);
        return addCondition(newCondition);
    }

    @Override
    public int createMinProductCondition(int minAmount, int productID)throws JsonProcessingException {
        if(minAmount < 0){
            throw new IllegalArgumentException(Error.CannotMakeNegativeAmountCondition(minAmount));
        }
        MinProductCondition newCondition = new MinProductCondition(minAmount);
        newCondition.setOnProductName(productID);
        return addCondition(newCondition);
    }

    @Override
    public int createMinProductOnCategoryCondition(int minAmount, String categoryName)throws JsonProcessingException {
        if(minAmount < 0){
            throw new IllegalArgumentException(Error.CannotMakeNegativeAmountCondition(minAmount));
        }
        MinProductCondition newCondition = new MinProductCondition(minAmount);
        newCondition.setOnCategoryName(categoryName);
        return addCondition(newCondition);
    }

    @Override
    public int createMinProductOnStoreCondition(int minAmount)throws JsonProcessingException {
        if(minAmount < 0){
            throw new IllegalArgumentException(Error.CannotMakeNegativeAmountCondition(minAmount));
        }
        MinProductCondition newCondition = new MinProductCondition(minAmount);
        newCondition.setOnStore();
        return addCondition(newCondition);
    }

    @Override
    public int createTrueCondition() throws JsonProcessingException {
        Condition newCondition = new TrueCondition();
        return addCondition(newCondition);
    }

    @Override
    public int createXorCondition(Condition conditionA, Condition conditionB)throws JsonProcessingException {
        Condition newCondition = new XorCondition(conditionA, conditionB);
        return addCondition(newCondition);
    }

    @Override
    public int createAndCondition(Condition conditionA, Condition conditionB)throws JsonProcessingException {
        Condition newCondition = new AndCondition(conditionA, conditionB);
        return addCondition(newCondition);
    }

    @Override
    public int createOrCondition(Condition conditionA, Condition conditionB) throws JsonProcessingException{
        Condition newCondition = new OrCondition(conditionA, conditionB);
        return addCondition(newCondition);
    }

    private int addCondition(Condition condition){
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Condition existing = getExistingCondition(session, condition);
            if(existing != null){
                transaction.commit();
                return existing.getId();
            }
            session.save(condition);
            transaction.commit();
            return condition.getId();
        }
        catch (Exception e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    private Condition getExistingCondition(Session session, Condition condition){
        Query query = condition.getUniqueQuery(session);
        List<Condition> res = query.list();
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
            session.createQuery( "delete from Condition").executeUpdate();
            transaction.commit();
        }
        catch (Exception e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }
}
