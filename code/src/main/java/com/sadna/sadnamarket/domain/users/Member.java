package com.sadna.sadnamarket.domain.users;


import com.sadna.sadnamarket.service.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Map;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

import javax.persistence.*;


@Entity
@Table(name = "Members")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Member extends IUser {
    
    @Id
    private String username;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private String emailAddress;
    @Column
    private String phoneNumber;
    @OneToMany
    @JoinColumn(name = "username")
    private List<UserRoleHibernate> roles;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_orders", joinColumns = @JoinColumn(name = "username"))
    @Column(name = "order_id")
    private List<Integer> orders;
    @Column
    private LocalDate birthDate;
    
    @OneToMany
    @JoinColumn(name = "username")
    private Map<Integer, Notification> notifes;
    private static final Logger logger = LogManager.getLogger(Member.class);
    @Column
    private boolean isLoggedIn;
    @Column
    private int notifyID;

    public Member(String username, String firstName, String lastName, String emailAddress, String phoneNumber,LocalDate birthDate) {
        logger.info("Entering Member constructor with parameters: username={}, firstName={}, lastName={}, emailAddress={}, phoneNumber={}, birthDate={}",
                username, firstName, lastName, emailAddress, phoneNumber,birthDate);
        roles = new ArrayList<>();
        notifes = new HashMap<>();
        orders = new ArrayList<>();
        isLoggedIn = false;
        notifyID = 0;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        logger.info("Exiting Member constructor");
    }
    public Member(){} 

    @Override
    public synchronized boolean isLoggedIn() {
        logger.info("Entering isLoggedIn");
        boolean result = isLoggedIn;
        logger.info("Exiting isLoggedIn with result={}", result);
        return result;
    }

    public void setCart(Cart cart) {
        logger.info("Entering setCart with cart={}", cart);
        if (this.cart.isEmpty())
            this.cart = cart;
        logger.info("Exiting setCart");
    }

    public void setLogin(boolean isLoggedIn) {
        logger.info("Entering setLogin with isLoggedIn={}", isLoggedIn);
        this.isLoggedIn = isLoggedIn;
        logger.info("Exiting setLogin");
    }

    public synchronized NotificationDTO addNotification(String message) {
        logger.info("Entering addNotification with message={}", message);
        notifyID++;
        Notification notification = new Notification(message,notifyID);
        notifes.put(notifyID, notification);
        logger.info("Exiting addNotification");
        return new NotificationDTO(notification);
    }

    public RequestDTO addOwnerRequest(UserFacade userFacade, String userName, int store_id) {
        logger.info("Entering addOwnerRequest with userFacade={}, userName={}, store_id={}", userFacade, userName, store_id);
        UserRole role = getRoleOfStore(store_id);
        if (role.getApointee().equals(userName)) {
            logger.error("Exception in addOwnerRequest: You disallowed appoint the one who appointed you!");
            throw new IllegalStateException(Error.makeMemberDisallowedAppointError());
        }
        RequestDTO requestDTO = role.sendRequest(userFacade, username, userName, "Owner");
        logger.info("Exiting addOwnerRequest");
        return requestDTO;
    }

    public RequestDTO addManagerRequest(UserFacade userFacade, String userName, int store_id) {
        logger.info("Entering addManagerRequest with userFacade={}, userName={}, store_id={}", userFacade, userName, store_id);
        UserRole role = getRoleOfStore(store_id);
        RequestDTO requestDTO = role.sendRequest(userFacade, username, userName, "Manager");
        logger.info("Exiting addManagerRequest");
        return requestDTO;
    }

    public UserRole getRoleOfStore(int store_id) {
        logger.info("Entering getRoleOfStore with store_id={}", store_id);
        for (UserRole role : getUserRoles()) {
            if (role.getStoreId() == store_id) {
                logger.info("Exiting getRoleOfStore with result={}", role);
                return role;
            }
        }
        logger.error("Exception in getRoleOfStore: User has no role in this store");
        throw new IllegalArgumentException(Error.makeMemberUserHasNoRoleError());
    }

    public boolean hasRoleInStore(int store_id) {
        logger.info("Entering hasRoleInStore with store_id={}", store_id);
        for (UserRole role : getUserRoles()) {
            if (role.getStoreId() == store_id) {
                logger.info("Exiting hasRoleInStore with result=true");
                return true;
            }
        }
        logger.info("Exiting hasRoleInStore with result=false");
        return false;
    }

    public synchronized void logout() {
        logger.info("Entering logout");
        if (!isLoggedIn) {
            logger.error("Exception in logout: user isn't logged in");
            throw new IllegalStateException(Error.makeMemberUserIsNotLoggedInError());
        }
        this.setLogin(false);
        logger.info("Exiting logout");
    }

    
    public synchronized void addRole(UserRoleHibernate role) {
        logger.info("Entering addRole with role={}", role);
        roles.add(role);
        logger.info("Exiting addRole");
    }
    public synchronized void addOrder(int orderId) {
        logger.info("Entering order id with order id={}", orderId);
        orders.add(orderId);
        logger.info("Exiting enter order");
    }

    public synchronized void removeRole(UserRole role) {
        logger.info("Entering removeRole with role={}", role);
        roles.remove(role);
        logger.info("Exiting removeRole");
    }

    public void addApointer(String apointed, int storeId) {
        logger.info("Entering addApointer with apointed={}, storeId={}", apointed, storeId);
        getRoleOfStore(storeId).addAppointers(apointed);
        logger.info("Exiting addApointer");
    }

    public void addPermissionToRole(Permission permission, int storeId) {
        logger.info("Entering addPermissionToRole with permission={}, storeId={}", permission, storeId);
        for (UserRole role : getUserRoles()) {
            if (role.getStoreId() == storeId) {
                role.addPermission(permission);
            }
        }
        logger.info("Exiting addPermissionToRole");
    }

    public boolean hasPermissionToRole(Permission permission, int storeId) {
        logger.info("Entering hasPermissionToRole with permission={}, storeId={}", permission, storeId);
        for (UserRole role : getUserRoles()) {
            if (role.getStoreId() == storeId && role.hasPermission(permission)) {
                logger.info("Exiting hasPermissionToRole with result=true");
                return true;
            }
        }
        logger.info("Exiting hasPermissionToRole with result=false");
        return false;
    }

    public List<Permission> getPermissions(int storeId) {
        logger.info("Entering getPermissions with storeId={}", storeId);
        List<Permission> permissions = new LinkedList<>();
        for (UserRole role : getUserRoles()) {
            if (role.getStoreId() == storeId) {
                permissions.addAll(role.getPermissions());
            }
        }
        logger.info("Exiting getPermissions");
        return permissions;
    }

    public void removePermissionFromRole(Permission permission, int storeId) {
        logger.info("Entering removePermissionFromRole with permission={}, storeId={}", permission, storeId);
        for (UserRole role : getUserRoles()) {
            if (role.getStoreId() == storeId) {
                role.removePermission(permission);
            }
        }
        logger.info("Exiting removePermissionFromRole");
    }

    public Map<Integer, Notification> getNotifications() {
        logger.info("Entering getNotifications");
        Map<Integer, Notification> result = notifes;
        logger.info("Exiting getNotifications with result={}", result);
        return result;
    }

    public String getUsername() {
        logger.info("Entering getUsername");
        String result = username;
        logger.info("Exiting getUsername with result={}", result);
        return result;
    }

    public RequestDTO getRequest(String senderName, int storeId, String reqType) {
        logger.info("Entering getRequest with senderName={}, storeId={}, reqType={}", senderName, storeId, reqType);
        if (hasRoleInStore(storeId)) {
            logger.error("Exception in getRequest: member already has role in store");
            throw new IllegalStateException(Error.makeMemberUserAlreadyHasRoleError());
        }
        notifyID++;
        Request request=new Request(senderName, "You got a request from " + senderName + " to become " + reqType + " in " + storeId, storeId,reqType,notifyID);
        notifes.put(notifyID, request);
        logger.info("Exiting getRequest");
        return new RequestDTO(request);
    }
    public RequestDTO getRequest(Request request) {
        logger.info("Entering getRequest with request={}", request);
        if (hasRoleInStore(request.getStoreId())) {
            logger.error("Exception in getRequest: member already has role in store");
            throw new IllegalStateException(Error.makeMemberUserAlreadyHasRoleError());
        }
        notifes.put(request.getId(), request);
        logger.info("Exiting getRequest");
        return new RequestDTO(request);
    }
    public NotificationDTO addNotification(Notification notification) {
        logger.info("Entering getRequest with notification={}", notification);;
        notifes.put(notification.getId(), notification);
        logger.info("Exiting getRequest");
        return notification.toDTO();
    }
    public void accept(int requestID,UserFacade userFacade) {
        logger.info("Entering accept with requestID={}", requestID);
        Notification notif = notifes.get(requestID);
        if(notif.getStoreId() > 0 && hasRoleInStore(notif.getStoreId())){
            notifes.remove(requestID);
            throw new IllegalArgumentException("User already has role in store");
        }
        notif.accept(this,userFacade);
        notifes.remove(requestID);
        logger.info("Exiting accept");
    }

    public void reject(int requestID) {
        logger.info("Entering reject with requestID={}", requestID);
        notifes.remove(requestID);
        logger.info("Exiting reject");
    }



    public Request getRequest(int request_id) {
        logger.info("Entering getRequest with request_id={}", request_id);
        Request result = (Request) notifes.get(request_id);
        logger.info("Exiting getRequest with result={}", result);
        return result;
    }

    // Getter for firstName
    public String getFirstName() {
        logger.info("Entering getFirstName");
        String result = firstName;
        logger.info("Exiting getFirstName with result={}", result);
        return result;
    }

    // Setter for firstName
    public void setFirstName(String firstName) {
        logger.info("Entering setFirstName with firstName={}", firstName);
        this.firstName = firstName;
        logger.info("Exiting setFirstName");
    }

    // Getter for lastName
    public String getLastName() {
        logger.info("Entering getLastName");
        String result = lastName;
        logger.info("Exiting getLastName with result={}", result);
        return result;
    }

    // Setter for lastName
    public void setLastName(String lastName) {
        logger.info("Entering setLastName with lastName={}", lastName);
        this.lastName = lastName;
        logger.info("Exiting setLastName");
    }

    // Getter for emailAddress
    public String getEmailAddress() {
        logger.info("Entering getEmailAddress");
        String result = emailAddress;
        logger.info("Exiting getEmailAddress with result={}", result);
        return result;
    }

    // Setter for emailAddress
    public void setEmailAddress(String emailAddress) {
        logger.info("Entering setEmailAddress with emailAddress={}", emailAddress);
        this.emailAddress = emailAddress;
        logger.info("Exiting setEmailAddress");
    }

    // Getter for phoneNumber
    public String getPhoneNumber() {
        logger.info("Entering getPhoneNumber");
        String result = phoneNumber;
        logger.info("Exiting getPhoneNumber with result={}", result);
        return result;
    }

    // Setter for phoneNumber
    public void setPhoneNumber(String phoneNumber) {
        logger.info("Entering setPhoneNumber with phoneNumber={}", phoneNumber);
        this.phoneNumber = phoneNumber;
        logger.info("Exiting setPhoneNumber");
    }

    public List<Integer> getOrdersHistory() {
        logger.info("Entering getOrdersHistory");
        List<Integer> result = orders;
        logger.info("Exiting getOrdersHistory with result={}", result);
        return result;
    }

    public List<UserRoleDTO> getUserRolesString() {
        logger.info("Entering getUserRolesString");
        List<UserRoleDTO> rolesString = new ArrayList<>();
        for (UserRole role : roles) {
            UserRoleDTO roleDTO = new UserRoleDTO(role.getStoreId(), role.toString());
            rolesString.add(roleDTO);
        }
        logger.info("Exiting getUserRolesString with result={}", rolesString);
        return rolesString;
    }

    public List<UserRoleHibernate> getUserRoles() {
        logger.info("Entering getUserRoles");
        List<UserRoleHibernate> result = roles;
        logger.info("Exiting getUserRoles with result={}", result);
        return result;
    }
    public LocalDate getBirthday() {
        logger.info("Entering getBirthday");
        LocalDate result = birthDate;
        logger.info("Exiting getBirthday with result={}", result);
        return result;
    }
    public void setBirthday(LocalDate birthDate) {
        logger.info("Entering setBirthday with birthDate={}", birthDate);
        this.birthDate = birthDate;
        logger.info("Exiting setBirthday");
    }

    public void clearCart() {
        logger.info("Entering clearCart");
        cart=new Cart();
        logger.info("Exiting clearCart");
    }
}
