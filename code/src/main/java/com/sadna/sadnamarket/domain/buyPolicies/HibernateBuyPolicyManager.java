package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.HibernateUtil;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;
import com.sadna.sadnamarket.service.Error;
import jakarta.persistence.QueryHint;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.PersistenceException;
import java.util.*;

public class HibernateBuyPolicyManager extends BuyPolicyManager{
    private int storeId;

    public HibernateBuyPolicyManager(BuyPolicyFacade facade, int storeId) {
        super(facade);
        this.storeId = storeId;
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public boolean hasPolicy(int policyId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<StoreBuyPolicyRelation> list = session.createQuery( "select p.policyId from StoreBuyPolicyRelation p " +
                    "WHERE p.storeId = :storeId " +
                    "AND p.policyId = :policyId" )
                    .setParameter("storeId",storeId)
                    .setParameter("policyId",policyId).list();
            return !list.isEmpty();
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<Integer> getAllPolicyIds() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Integer> res = session.createQuery( "select p.policyId from StoreBuyPolicyRelation p WHERE p.storeId = :storeId" ).setParameter("storeId",storeId).list();
            return res;
        }
        catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public void addBuyPolicy(int buyPolicyId) {
        if (hasPolicy(buyPolicyId))
            throw new IllegalArgumentException(Error.makeBuyPolicyAlreadyExistsError(buyPolicyId));
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            StoreBuyPolicyRelation dto = new StoreBuyPolicyRelation(storeId, buyPolicyId, false);
            session.save(dto); // Save the store and get the generated ID
            transaction.commit();
        }
        catch (Exception e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public void addLawBuyPolicy(int buyPolicyId) {
        if (hasPolicy(buyPolicyId))
            throw new IllegalArgumentException(Error.makeBuyPolicyAlreadyExistsError(buyPolicyId));
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            StoreBuyPolicyRelation dto = new StoreBuyPolicyRelation(storeId, buyPolicyId, true);
            session.save(dto); // Save the store and get the generated ID
            transaction.commit();
        }
        catch (Exception e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public void removeBuyPolicy(int buyPolicyId) {
        if (!hasPolicy(buyPolicyId)) {
            throw new IllegalArgumentException(Error.makeBuyPolicyWithIdDoesNotExistError(buyPolicyId));
        }
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            List<StoreBuyPolicyRelation> list = session.createQuery( "select p from StoreBuyPolicyRelation p " +
                            "WHERE p.storeId = :storeId " +
                            "AND p.policyId = :policyId" )
                    .setParameter("storeId",storeId)
                    .setParameter("policyId",buyPolicyId).list();
            if(list.get(0).legal){
                throw new IllegalArgumentException(Error.makeCanNotRemoveLawBuyPolicyError(buyPolicyId));
            }
            session.delete(list.get(0));
            transaction.commit();
        }
        catch (PersistenceException e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Set<String> canBuy(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user) {
        Set<String> error = new HashSet<>();
        List<Integer> buyPolicyIds = getAllPolicyIds();
        for (Integer policyId : buyPolicyIds) {
            BuyPolicy policy = facade.getBuyPolicy(policyId);
            error.addAll(policy.canBuy(cart, products, user));
        }
        return error;
    }

    @Override
    public void clear() {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery( "delete from StoreBuyPolicyRelation WHERE store = :storeId ")
                    .setParameter("storeId",storeId).executeUpdate();
            transaction.commit();
        }
        catch (Exception e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }
}
