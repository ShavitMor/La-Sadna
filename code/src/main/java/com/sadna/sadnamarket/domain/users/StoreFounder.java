package com.sadna.sadnamarket.domain.users;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("STORE_FOUNDER")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class StoreFounder extends StoreOwner implements Serializable {
    private static final Logger logger = LogManager.getLogger(StoreFounder.class);

    public StoreFounder(int storeId, String apointee) {
        super(storeId, apointee);
        logger.info("Entering StoreFounder constructor with storeId={} and apointee={}", storeId, apointee);
        logger.info("Exiting StoreFounder constructor");
    }
    public StoreFounder() {
        logger.info("Entering StoreFounder constructor");
        logger.info("Exiting StoreFounder constructor");
    }

    @Override
    public boolean hasPermission(Permission permission) {
        logger.info("Entering hasPermission with permission={}", permission);
        logger.info("Exiting hasPermission with result=true");
        return true;
    }

    @Override
    public void leaveRole(UserRoleVisitor userRoleVisitor, int storeId, Member member, UserFacade userFacade) {
        logger.info("Entering leaveRole with userRoleVisitor={}, storeId={}, member={}, and userFacade={}", userRoleVisitor, storeId, member, userFacade);
        userRoleVisitor.visitStoreFounder(this, this.getStoreId(), member);
        logger.info("Exiting leaveRole");
    }

    @Override
    public List<Permission> getPermissions() {
        return new LinkedList<>();
    }

    @Override
    public String toString() {
        logger.info("Entering toString");
        String result = "store founder";
        logger.info("Exiting toString with result={}", result);
        return result;
    }
}
