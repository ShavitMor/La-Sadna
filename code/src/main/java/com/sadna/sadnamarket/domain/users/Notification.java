package com.sadna.sadnamarket.domain.users;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;

import javax.persistence.*;


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "notifications_type", discriminatorType = DiscriminatorType.STRING)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column
    private String message;
    @Column
    private LocalDateTime date;
    @Column
    private String username;
    private static final Logger logger = LogManager.getLogger(Notification.class);

    public Notification(String msg,int id) {
        logger.info("Entering Notification constructor with msg={}", msg);
        this.message = msg;
        this.id=id;
        this.date = LocalDateTime.now();
        logger.info("Exiting Notification constructor");
    }
    public Notification(String msg) {
        logger.info("Entering Notification constructor with msg={}", msg);
        this.message = msg;
        this.date = LocalDateTime.now();
        logger.info("Exiting Notification constructor");
    }
    public Notification(String msg,int id,String username) {
        logger.info("Entering Notification constructor with msg={}", msg);
        this.message = msg;
        this.id=id;
        this.username=username;
        this.date = LocalDateTime.now();
        logger.info("Exiting Notification constructor");
    }
    public Notification() {}
    public void accept(Member member,UserFacade userFacade) {
        logger.info("Entering accept with member={}", member);
        // No specific implementation for accept in Notification
        logger.info("Exiting accept");
    }

    public int getStoreId(){
        return -1;
    }



    @Override
    public String toString() {
        logger.info("Entering toString");
        String result = "got message: " + message + " on: " + date.toString();
        logger.info("Exiting toString with result={}", result);
        return result;
    }

    public String getMessage() {
        logger.info("Entering getMessage");
        logger.info("Exiting getMessage with result={}", message);
        return message;
    }

    public LocalDateTime getDate() {
        logger.info("Entering getDate");
        logger.info("Exiting getDate with result={}", date);
        return date;
    }

    public NotificationDTO toDTO(){
        return new NotificationDTO(this);
    }

    public int getId() {
        return id;
    }

}
