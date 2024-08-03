package com.sadna.sadnamarket.domain.buyPolicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.HibernateUtil;
import com.sadna.sadnamarket.service.Error;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.QueryHint;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HibernateBuyPolicyRepository implements IBuyPolicyRepository{
    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public BuyPolicy findBuyPolicyByID(int policyId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            BuyPolicyData policyDTO = session.get(BuyPolicyData.class, policyId);
            if (policyDTO == null) {
                throw new IllegalArgumentException(Error.makeBuyPolicyWithIdDoesNotExistError(policyId));
            }
            if(policyDTO.isComposite()){
                BuyPolicy policy1 = findBuyPolicyByID(policyDTO.getId1());
                BuyPolicy policy2 = findBuyPolicyByID(policyDTO.getId2());
                return policyDTO.toBuyPolicy(policy1, policy2);
            }else{
                return policyDTO.toBuyPolicy();
            }
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Set<Integer> getAllPolicyIds() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Integer> res = session.createQuery( "select p.policyId from BuyPolicyData p" ).list();
            return new HashSet<>(res);
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    private int addBuyPolicy(BuyPolicy buyPolicy) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            BuyPolicyData hibernateDto = buyPolicy.generateData();
            BuyPolicyData existingDto = getExistingBuyPolicy(session, hibernateDto);
            if(existingDto != null){
                transaction.commit();
                return existingDto.policyId;
            }
            session.save(hibernateDto);
            transaction.commit();
            return hibernateDto.policyId;
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            throw e;
        }
    }

    private int addCompositeBuyPolicy(int id1, int id2, String logic) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            BuyPolicyData policyDTO = session.get(BuyPolicyData.class, id1);
            if (policyDTO == null) {
                throw new IllegalArgumentException(Error.makeBuyPolicyWithIdDoesNotExistError(id1));
            }
            policyDTO = session.get(BuyPolicyData.class, id2);
            if (policyDTO == null) {
                throw new IllegalArgumentException(Error.makeBuyPolicyWithIdDoesNotExistError(id2));
            }
            BuyPolicyData hibernateDto = new CompositeBuyPolicyData(id1,id2,logic);
            BuyPolicyData existingDto = getExistingBuyPolicy(session, hibernateDto);
            if(existingDto != null){
                transaction.commit();
                return existingDto.policyId;
            }
            session.save(hibernateDto); // Save the store and get the generated ID
            transaction.commit();
            return hibernateDto.policyId;
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch(Exception e) {
            throw e;
        }
    }

    @Override
    public int addProductKgBuyPolicy(int productId, List<BuyType> buytypes, double min, double max) throws JsonProcessingException {
        KgLimitBuyPolicy policy = new KgLimitBuyPolicy(buytypes,new ProductSubject(productId),min,max);
        return addBuyPolicy(policy);
    }

    @Override
    public int addProductAmountBuyPolicy(int productId, List<BuyType> buytypes, int min, int max) throws JsonProcessingException {
        AmountBuyPolicy policy = new AmountBuyPolicy(buytypes,new ProductSubject(productId),min,max);
        return addBuyPolicy(policy);
    }

    @Override
    public int addCategoryAgeLimitBuyPolicy(String category, List<BuyType> buytypes, int min, int max) throws JsonProcessingException {
        AgeLimitBuyPolicy policy = new AgeLimitBuyPolicy(buytypes,new CategorySubject(category),min,max);
        return addBuyPolicy(policy);
    }

    @Override
    public int addCategoryHourLimitBuyPolicy(String category, List<BuyType> buytypes, LocalTime from, LocalTime to) throws JsonProcessingException {
        HourLimitBuyPolicy policy = new HourLimitBuyPolicy(buytypes,new CategorySubject(category),from,to);
        return addBuyPolicy(policy);
    }

    @Override
    public int addCategoryRoshChodeshBuyPolicy(String category, List<BuyType> buytypes) throws JsonProcessingException {
        RoshChodeshBuyPolicy policy = new RoshChodeshBuyPolicy(buytypes,new CategorySubject(category));
        return addBuyPolicy(policy);
    }

    @Override
    public int addCategoryHolidayBuyPolicy(String category, List<BuyType> buytypes) throws JsonProcessingException {
        HolidayBuyPolicy policy = new HolidayBuyPolicy(buytypes,new CategorySubject(category));
        return addBuyPolicy(policy);
    }

    @Override
    public int addCategorySpecificDateBuyPolicy(String category, List<BuyType> buytypes, int day, int month, int year) throws JsonProcessingException {
        SpecificDateBuyPolicy policy = new SpecificDateBuyPolicy(buytypes,new CategorySubject(category),day,month,year);
        return addBuyPolicy(policy);
    }

    @Override
    public int addAndBuyPolicy(int id1, int id2) throws JsonProcessingException {
        return addCompositeBuyPolicy(id1, id2, BuyPolicyTypeCodes.AND);
    }

    @Override
    public int addOrBuyPolicy(int id1, int id2) throws JsonProcessingException {
        return addCompositeBuyPolicy(id1, id2, BuyPolicyTypeCodes.OR);
    }

    @Override
    public int addConditioningBuyPolicy(int id1, int id2) throws JsonProcessingException {
        return addCompositeBuyPolicy(id1, id2, BuyPolicyTypeCodes.CONDITION);
    }

    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    private BuyPolicyData getExistingBuyPolicy(Session session, BuyPolicyData policyDTO){
        Query query = policyDTO.getUniqueQuery(session);
        List<BuyPolicyData> res = query.list();
        if(res.isEmpty()){
            return null;
        }
        return res.get(0);
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public boolean buyPolicyExists(int policyId) {
        try {
            findBuyPolicyByID(policyId);
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
    public BuyPolicyManager createManager(BuyPolicyFacade facade, int storeId) {
        return new HibernateBuyPolicyManager(facade,storeId);
    }

    @Override
    public void clear() {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery( "delete from BuyPolicyData").executeUpdate();
            session.createQuery( "delete from StoreBuyPolicyRelation").executeUpdate();
            transaction.commit();
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            throw e;
        }
    }
}
