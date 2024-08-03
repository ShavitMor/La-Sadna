package com.sadna.sadnamarket.domain.users;

import com.sadna.sadnamarket.service.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

@Entity
@DiscriminatorValue("STORE_MANAGER")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class StoreManager extends UserRoleHibernate {
    private List<String> appointments;
    private static final Logger logger = LogManager.getLogger(StoreManager.class);

    public StoreManager(int storeId) {
        super(storeId);
        logger.info("Entering StoreManager constructor with storeId={}", storeId);
        logger.info("Exiting StoreManager constructor");
    }
    public StoreManager() {
        logger.info("Entering StoreManager constructor");
        logger.info("Exiting StoreManager constructor");
    }

    @Override
    public void leaveRole(UserRoleVisitor userRoleVisitor, int storeId, Member member, UserFacade userFacade) {
        logger.info("Entering leaveRole with userRoleVisitor={}, storeId={}, member={}, userFacade={}", userRoleVisitor, storeId, member, userFacade);
        userRoleVisitor.visitStoreManager(this, this.getStoreId(), member);
        logger.info("Exiting leaveRole");
    }

    public void addPermission(Permission permission) {
        logger.info("Entering addPermission with permission={}", permission);
        permissions.add(permission);
        logger.info("Exiting addPermission");
    }

    public void removePermission(Permission permission) {
        logger.info("Entering removePermission with permission={}", permission);
        if (hasPermission(permission)) {
            permissions.remove(permission);
        } else {
            logger.error("user doesnt has this permission");
            throw new IllegalArgumentException(Error.makeManagerDoesntHavePermissionError());
        }
        logger.info("Exiting removePermission");
    }

    public boolean hasPermission(Permission permission) {
        logger.info("Entering hasPermission with permission={}", permission);
        boolean result = permissions.contains(permission);
        logger.info("Exiting hasPermission with result={}", result);
        return result;
    }

    public int getStoreId() {
        logger.info("Entering getStoreId");
        int storeId = super.getStoreId();
        logger.info("Exiting getStoreId with result={}", storeId);
        return storeId;
    }

    @Override
    public String toString() {
        logger.info("Entering toString");
        String result = "store Manager";
        logger.info("Exiting toString with result={}", result);
        return result;
    }

    @Override
    public boolean isApointedByUser(String username) {
        logger.info("Entering isApointedByUser with username={}", username);
        logger.info("Exiting isApointedByUser with result=false");
        return false;
    }

    @Override
    public List<String> getAppointers() {
        logger.info("Entering getAppointers");
        logger.info("Exiting getAppointers with result={}", appointments);
        return appointments;
    }

    @Override
    public RequestDTO sendRequest(UserFacade userFacade, String senderName, String sentName, String reqType) {
        logger.info("Entering sendRequest with userFacade={}, senderName={}, sentName={}, reqType={}", userFacade, senderName, sentName, reqType);
        logger.error("You are not authorized to perform this action");
        throw new IllegalAccessError(Error.makeManagerYouAreNotAuthorizedError());
    }

    @Override
    public List<Permission> getPermissions() {
        logger.info("Entering getPermissions");
        logger.info("Exiting getPermissions with result={}", permissions);
        return permissions;
    }

    @Override
    public String getApointee() {
        logger.info("Entering getApointee");
        logger.error("its not relvant who apointed you");
        throw new IllegalAccessError(Error.makeManagerNotRelevantError());
    }

    @Override
    public void addAppointers(String apointee) {
        logger.info("Entering addAppointers with apointee={}", apointee);
        logger.error("manager doesnt has apointees");
        throw new IllegalStateException(Error.makeManagerCannotAppointError());
    }
}
