package com.sadna.sadnamarket.domain.users;

import com.sadna.sadnamarket.service.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import java.util.LinkedList;
import java.util.List;

@Entity
@DiscriminatorValue("STORE_OWNER")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class StoreOwner extends UserRoleHibernate {
    private static final Logger logger = LogManager.getLogger(StoreOwner.class);

    public StoreOwner(int storeId, String apointee) {
        super(storeId);
        logger.info("Entering StoreOwner constructor with storeId={} and apointee={}", storeId, apointee);
        this.apointee = apointee;
        logger.info("Exiting StoreOwner constructor");
    }
    public StoreOwner() {
        logger.info("Entering StoreOwner constructor");
        logger.info("Exiting StoreOwner constructor");
    }

    @Override
    public boolean hasPermission(Permission permission) {
        logger.info("Entering hasPermission with permission={}", permission);
        boolean result = !(permission == Permission.REOPEN_STORE || permission == Permission.CLOSE_STORE);
        logger.info("Exiting hasPermission with result={}", result);
        return result;
    }

    public int getStoreId() {
        logger.info("Entering getStoreId");
        logger.info("Exiting getStoreId with result={}", storeId);
        return storeId;
    }

    @Override
    public String toString() {
        logger.info("Entering toString");
        String result = "store owner";
        logger.info("Exiting toString with result={}", result);
        return result;
    }

    @Override
    public void addPermission(Permission permission) {
        logger.error("Exception in addPermission: store owner has all the permissions");
        throw new IllegalStateException(Error.makeOwnerHasAllPermissionsError());
    }

    @Override
    public boolean isApointedByUser(String username) {
        logger.info("Entering isApointedByUser with username={}", username);
        boolean result = appointments.contains(username);
        logger.info("Exiting isApointedByUser with result={}", result);
        return result;
    }

    @Override
    public List<String> getAppointers() {
        logger.info("Entering getAppointers");
        logger.info("Exiting getAppointers with result={}", appointments);
        return appointments;
    }

    @Override
    public void leaveRole(UserRoleVisitor userRoleVisitor, int storeId, Member member, UserFacade userFacade) {
        logger.info("Entering leaveRole with storeId={}, member={}, and userFacade={}", storeId, member, userFacade);
        userRoleVisitor.visitStoreOwner(this, storeId, member, userFacade);
        logger.info("Exiting leaveRole");
    }

    public RequestDTO sendRequest(UserFacade userFacade, String senderName, String sentName, String reqType) {
        logger.info("Entering sendRequest with senderName={}, sentName={}, and reqType={}", senderName, sentName, reqType);
        RequestDTO requestDTO = userFacade.addRequest(senderName,sentName, storeId, reqType);
        logger.info("Exiting sendRequest");
        return requestDTO;
    }

    @Override
    public String getApointee() {
        logger.info("Entering getApointee");
        logger.info("Exiting getApointee with result={}", apointee);
        return apointee;
    }

    @Override
    public void removePermission(Permission permission) {
        logger.error("Exception in removePermission: can't remove permissions from a store owner");
        throw new IllegalStateException(Error.makeOwnerCannotRemovePermissionError());
    }

    @Override
    public List<Permission> getPermissions() {
        return new LinkedList<>();
    }

    @Override
    public void addAppointers(String apointee) {
        logger.info("Entering addAppointers with apointee={}", apointee);
        appointments.add(apointee);
        logger.info("Exiting addAppointers");
    }
}
