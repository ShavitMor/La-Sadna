package com.sadna.sadnamarket.domain.users;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@DiscriminatorValue("Request")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Request extends Notification {
    @Column
    private String senderName;
    @Column
    private int storeId;
    @Column
    private String role;
    private static final Logger logger = LogManager.getLogger(Request.class);

    public Request(String senderName, String msg, int storeId, String role,int id) {
        super(msg,id);
        logger.info("Entering Request constructor with senderName={}, msg={}, storeId={}, role={}", senderName, msg, storeId, role);
        this.senderName = senderName;
        this.storeId = storeId;
        this.role = role;
        logger.info("Exiting Request constructor");
    }
    public Request(String senderName, String msg, int storeId, String role) {
        super(msg);
        logger.info("Entering Request constructor with senderName={}, msg={}, storeId={}, role={}", senderName, msg, storeId, role);
        this.senderName = senderName;
        this.storeId = storeId;
        this.role = role;
        logger.info("Exiting Request constructor");
    }
    public Request(){
        logger.info("Entering Request constructor");
        logger.info("Exiting Request constructor");
    }

    private void acceptOwner(Member accepting,UserFacade userFacade) {
        logger.info("Entering acceptOwner with accepting={}", accepting);
        userFacade.addStoreOwner(accepting.getUsername(), senderName, storeId);
        logger.info("Exiting acceptOwner");
    }

    private void acceptManager(Member accepting,UserFacade userFacade) {
        logger.info("Entering acceptManager with accepting={}", accepting);
         userFacade.addStoreManager(accepting.getUsername(),storeId);
        logger.info("Exiting acceptManager");
    }

    public void accept(Member accepting,UserFacade userFacade) {
        logger.info("Entering accept with accepting={}", accepting);
        if (role.equals("Manager"))
            acceptManager(accepting,userFacade);
        else
            acceptOwner(accepting,userFacade);
        logger.info("Exiting accept");
    }

    public int getStoreId() {
        logger.info("Entering getStoreId");
        logger.info("Exiting getStoreId with result={}", storeId);
        return storeId;
    }

    public String getRole() {
        logger.info("Entering getRole");
        logger.info("Exiting getRole with result={}", role);
        return role;
    }

    public String getSender() {
        logger.info("Entering getSender");
        logger.info("Exiting getSender with result={}", senderName);
        return senderName;
    }

    @Override
    public NotificationDTO toDTO(){
        return new RequestDTO(this);
    }

    @Override
    public String toString() {
        logger.info("Entering toString");
        String result = super.toString() + " from store " + storeId + " from user: " + senderName + " as " + role;
        logger.info("Exiting toString with result={}", result);
        return result;
    }
}
