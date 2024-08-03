package com.sadna.sadnamarket.domain.users;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.persistence.QueryHint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.sadna.sadnamarket.service.Error;

import com.sadna.sadnamarket.HibernateUtil;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.PersistenceException;

public class UserHibernateRepo implements IUserRepository {

    private static final Logger logger = LogManager.getLogger(UserHibernateRepo.class);

    @Override
    public void store(String username,String firstName, String lastName,String emailAddress,String phoneNumber, LocalDate birthDate) {
        logger.info("Storing user: username={}, firstname={}, lastname={}, emailAddress={}, phone number={}, birthdate={}" ,username,firstName,lastName,emailAddress,phoneNumber,birthDate);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = new Member(username, firstName, lastName, emailAddress, phoneNumber,birthDate);
            session.save(member);
            transaction.commit();
            logger.info("User {} stored successfully",username);
        }catch (PersistenceException e){
            logger.error("database error in store {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public boolean hasMember(String name) {
        logger.info("Checking if user {} exists",name);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Member member = session.get(Member.class, name);
            if (member != null) {
                logger.info("User {} exists",name);
                return true;
            } else {
                logger.info("User {} doesn't exist",name);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(name));
            }
        }catch (PersistenceException e){
            logger.error("database error in hasMember {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public int addGuest() {
        logger.info("Adding guest");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Guest guest = new Guest();
            session.save(guest);
            transaction.commit();
            logger.info("Guest {} added successfully",guest.guestId);
            return guest.guestId;
        }catch (PersistenceException e){
            logger.error("database error in addGuest {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        } 
    }

    @Override
    public void deleteGuest(int guestID) {
        logger.info("Deleting guest {}",guestID);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Guest guest = session.get(Guest.class, guestID);
            if (guest != null) {
                session.delete(guest);
                transaction.commit();
                logger.info("Guest {} deleted successfully",guestID);
            } else {
                transaction.rollback();
                logger.error("Guest {} doesn't exist",guestID);
                throw new NoSuchElementException(Error.makeMemberGuestDoesntExistError(guestID));
            }
        }catch (PersistenceException e){
            logger.error("database error in deleteGuest {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<CartItemDTO> getUserCart(String username) {
        logger.info("Getting cart of user {}",username);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Member member = session.get(Member.class, username);
            if (member != null) {
                logger.info("Cart of user {} retrieved successfully",username);
                return member.getCartItems();
            } else {
                logger.error("User {} doesn't exist",username);
                return null;
            }
        }catch (PersistenceException e){
            logger.error("database error in getUserCart {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<CartItemDTO> getGuestCart(int guestID) {
        logger.info("Getting cart of guest {}",guestID);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Guest guest = session.get(Guest.class, guestID);
            if (guest != null) {
                logger.info("Cart of guest {} retrieved successfully",guestID);
                return guest.getCartItems();
            } else {
                logger.error("Guest {} doesn't exist",guestID);
                throw new NoSuchElementException(Error.makeMemberGuestDoesntExistError(guestID));
            }
        }catch (PersistenceException e){
            logger.error("database error in getting guest cart {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public boolean hasPermissionToRole(String userName, Permission permission, int storeId) {
        logger.info("Checking if user {} has permission to role",userName);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Member member = session.get(Member.class, userName);
            if (member != null) {   
                boolean ret= member.hasPermissionToRole(permission, storeId);
                logger.info("has Permission To Role result={}",ret);
                return ret;
            } else {
                logger.error("User {} doesn't exist",userName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error in has permission to role {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public NotificationDTO addNotification(String userName, String msg) {
        logger.info("Adding notification to user {}",userName);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, userName);
            if (member != null) {
                Notification notif=new Notification(msg);
                session.save(notif);
                NotificationDTO notification=member.addNotification(notif);
                session.update(member);
                transaction.commit();
                logger.info("Notification added to user {} successfully",userName);
                return notification;
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",userName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error in add notification {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public boolean isLoggedIn(String username) {
        logger.info("Checking if user {} is logged in",username);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Member member = session.get(Member.class, username);
            boolean res= member != null && member.isLoggedIn();
            logger.info("User {} is logged in={}",username,res);
            return res;
        }catch (PersistenceException e){
            logger.error("database error in is logged in {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void setLogin(String userName, boolean b) {
        logger.info("Setting login of user {} to {}",userName,b);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, userName);
            if (member != null) {
                member.setLogin(b);
                session.update(member);
                transaction.commit();
                logger.info("Login of user {} set to {}",userName,b);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",userName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error in set login {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void addProductToCart(String username, int storeId, int productId, int amount) {
        logger.info("Adding product to cart of user {}",username);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                member.addProductToCart(storeId, productId, amount);
                session.update(member);
                transaction.commit();
                logger.info("Product added to cart of user {} successfully",username);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",username);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error in adding product to cart {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void removeProductFromCart(String username, int storeId, int productId) {
        logger.info("Removing product from cart of user {},storeId={},productId={}",username);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                member.removeProductFromCart(storeId, productId);
                session.update(member);
                transaction.commit();
                logger.info("Product removed from cart of user {} successfully",username);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",username);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error in remove product to cart {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void removeProductFromGuestCart(int guestId, int storeId, int productId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Guest guest = session.get(Guest.class, guestId);
            if (guest != null) {
                guest.removeProductFromCart(storeId, productId);
                session.update(guest);
                transaction.commit();
            } else {
                transaction.rollback();
                throw new NoSuchElementException(Error.makeMemberGuestDoesntExistError(guestId));
            }
        }catch (PersistenceException e){
            logger.error("database error in remove product from guest cart {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void changeQuantityCart(String username, int storeId, int productId, int amount) {
        logger.info("Changing quantity of product in cart of user {},storeId={},productId={},amount={}",username,storeId,productId,amount);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                member.changeQuantityCart(storeId, productId, amount);
                session.update(member);
                transaction.commit();
                logger.info("Quantity of product in cart of user {} changed successfully",username);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",username);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error in change quantity cart {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void guestChangeQuantityCart(int guestId, int storeId, int productId, int amount) {
        logger.info("Changing quantity of product in cart of guest {},storeId={},productId={},amount={}",guestId,storeId,productId,amount);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Guest guest = session.get(Guest.class, guestId);
            if (guest != null) {
                guest.changeQuantityCart(storeId, productId, amount);
                session.update(guest);
                transaction.commit();
                logger.info("Quantity of product in cart of guest {} changed successfully",guestId);
            } else {
                transaction.rollback();
                logger.error("Guest {} doesn't exist",guestId);
                throw new NoSuchElementException(Error.makeMemberGuestDoesntExistError(guestId));
            }
        }catch (PersistenceException e){
            logger.error("database error in guest changing quantity in cart {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void logout(String userName) {
        logger.info("Logging out user {}",userName);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, userName);
            if (member != null) {
                member.logout();
                session.update(member);
                transaction.commit();
                logger.info("User {} logged out successfully",userName);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",userName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error in logout {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void setCart(String userName, List<CartItemDTO> cartLst) {
        logger.info("Setting cart of user {}",userName);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, userName);
            if (member != null) {
                Cart cart = new Cart(cartLst);
                member.setCart(cart);
                session.update(member);
                transaction.commit();
                logger.info("Cart of user {} set successfully",userName);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",userName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error in set cart {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void addRole(String username, StoreManager storeManager) {
        logger.info("Adding store manager role to user {}",username);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                member.addRole(storeManager);
                session.update(member);
                transaction.commit();
                logger.info("Store manager role added to user {} successfully",username);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",username);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error in add role {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void addRole(String username, StoreOwner storeOwner) {
        logger.info("Adding store owner role to user {}",username);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                member.addRole(storeOwner);
                session.update(member);
                transaction.commit();
                logger.info("Store owner role added to user {} successfully",username);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",username);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error in add role {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void addPermissionToRole(String username, Permission permission, int storeId) {
        logger.info("Adding permission to role of member={} in store={} of {}",username,storeId,permission);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                member.addPermissionToRole(permission, storeId);
                session.update(member);
                transaction.commit();
                logger.info("Permission added to role of member={} in store={} of {} successfully",username,storeId,permission);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",username);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error adding permission to role {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void removePermissionFromRole(String username, Permission permission, int storeId) {
        logger.info("Removing permission from role of member={} in store={} of {}",username,storeId,permission);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                member.removePermissionFromRole(permission, storeId);
                transaction.commit();
                logger.info("Permission removed from role of member={} in store={} of {} successfully",username,storeId,permission);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",username);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error in removePermissionFromRole {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<Permission> getPermissions(String userName, int storeId) {
        logger.info("Getting permissions of member={} in store={}",userName,storeId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Member member = session.get(Member.class, userName);
            if (member != null) {
                logger.info("Permissions of member={} in store={} retrieved successfully",userName,storeId);
                return member.getPermissions(storeId);
            } else {
                logger.error("User {} doesn't exist",userName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error in getPermissions {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void setFirstName(String userName, String firstName) {
        logger.info("Setting first name of user {} to {}",userName,firstName);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, userName);
            if (member != null) {
                member.setFirstName(firstName);
                session.update(member);
                transaction.commit();
                logger.info("First name of user {} set to {} successfully",userName,firstName);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",userName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error setting first Name {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void setLastName(String userName, String lastName) {
        logger.info("Setting last name of user {} to {}",userName,lastName);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, userName);
            if (member != null) {
                member.setLastName(lastName);
                session.update(member);
                transaction.commit();
                logger.info("Last name of user {} set to {} successfully",userName,lastName);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",userName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error last first Name {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void setEmailAddress(String userName, String emailAddress) {
        logger.info("Setting email address of user {} to {}",userName,emailAddress);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, userName);
            if (member != null) {
                member.setEmailAddress(emailAddress);
                session.update(member);
                transaction.commit();
                logger.info("Email address of user {} set to {} successfully",userName,emailAddress);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",userName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error setting email address {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void setPhoneNumber(String userName, String phoneNumber) {
        logger.info("Setting phone number of user {} to {}",userName,phoneNumber);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, userName);
            if (member != null) {
                member.setPhoneNumber(phoneNumber);
                session.update(member);
                transaction.commit();
                logger.info("Phone number of user {} set to {} successfully",userName,phoneNumber);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",userName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error setting phone number {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void setBirthday(String username,LocalDate birthDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                member.setBirthday(birthDate);
                session.update(member);
                transaction.commit();
            } else {
                transaction.rollback();
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error setting birthday {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public MemberDTO getMemberDTO(String userName) {
        logger.info("Getting memberDTO of user {}",userName);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Member member = session.get(Member.class, userName);
            if (member != null) {
                logger.info("MemberDTO of user {} retrieved successfully",userName);
                return new MemberDTO(member);
            } else {
                logger.error("User {} doesn't exist",userName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error getting member dto {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<Integer> getOrdersHistory(String username) {
        logger.info("Getting orders history of user {}",username);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Member member = session.get(Member.class, username);
            if (member != null) {
                logger.info("Orders history of user {} retrieved successfully",username);
                return member.getOrdersHistory();
            } else {
                logger.error("User {} doesn't exist",username);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error getting order history {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void clearCart(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                member.clearCart();
                transaction.commit();
            } else {
                transaction.rollback();
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error clear cart {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<NotificationDTO> getNotifications(String username) {
        logger.info("Getting notifications of user {}",username);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                logger.info("Notifications of user {} retrieved successfully",username);
                return member.getNotifications().values().stream().map(Notification::toDTO).toList();
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",username);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error get Notifications {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void addOrder(String username, int orderId) {
        logger.info("Adding order to user {} with orderId={}",username,orderId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                member.addOrder(orderId);
                session.update(member);
                transaction.commit();
                logger.info("Order added to user {} with orderId={} successfully",username,orderId);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",username);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error adding Order {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<UserRoleDTO> getUserRolesString(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                return member.getUserRolesString();
            } else {
                transaction.rollback();
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error get UserRolesString {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public UserRole getRoleOfStore(String userName, int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Member member = session.get(Member.class, userName);
            if (member != null) {
                return member.getRoleOfStore(storeId);
            } else {
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
            }
        }catch (PersistenceException e){
            logger.error("database error getting role of store {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<UserRoleHibernate> getUserRoles(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Member member = session.get(Member.class, username);
            if (member != null) {
                return member.getUserRoles();
            } else {
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error get user roles {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public RequestDTO addOwnerRequest(String senderName, UserFacade userFacade, String userName, int store_id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, senderName);
            if (member != null) {
                RequestDTO request = member.addOwnerRequest(userFacade, userName, store_id);
                return request;
            } else {
                transaction.rollback();
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(senderName));
            }
        }catch (PersistenceException e){
            logger.error("database error addOwnerRequest {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public RequestDTO addManagerRequest(String senderName, UserFacade userFacade, String userName, int store_id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, senderName);
            if (member != null) {
                RequestDTO request = member.addManagerRequest(userFacade, userName, store_id);
                return request;
            } else {
                transaction.rollback();
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(senderName));
            }
        }catch (PersistenceException e){
            logger.error("database error adding manager request {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Request getRequest(String acceptingName, int requestID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Member member = session.get(Member.class, acceptingName);
            if (member != null) {
                return member.getRequest(requestID);
            } else {
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(acceptingName));
            }
        }catch (PersistenceException e){
            logger.error("database error getting request {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void accept(String acceptingName, int requestID,UserFacade userFacade) {
        logger.info("Accepting request of user {} with requestID={}",acceptingName,requestID);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, acceptingName);
            if (member != null) {
                member.accept(requestID,userFacade);
                session.update(member);
                transaction.commit();
                logger.info("Request of user {} with requestID={} accepted successfully",acceptingName,requestID);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",acceptingName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(acceptingName));
            }
        }catch (PersistenceException e){
            logger.error("database error accepting request {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void addApointer(String apointer, String acceptingName, int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, apointer);
            if (member != null) {
                member.addApointer(acceptingName, storeId);
                session.update(member);
                transaction.commit();
            } else {
                transaction.rollback();
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(apointer));
            }
        }catch (PersistenceException e){
            logger.error("database error adding Apointer {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void reject(String rejectingName, int requestID) {
        logger.info("Rejecting request of user {} with requestID={}",rejectingName,requestID);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, rejectingName);
            if (member != null) {
                member.reject(requestID);
                session.update(member);
                transaction.commit();
                logger.info("Request of user {} with requestID={} rejected successfully",rejectingName,requestID);
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",rejectingName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(rejectingName));
            }
        }catch (PersistenceException e){
            logger.error("database error reject request {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void leaveRole(String username, int storeId, UserFacade userFacade) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, username);
            if (member != null) {
                UserRole role = member.getRoleOfStore(storeId);
               role.leaveRole(new UserRoleVisitor(),storeId,member,userFacade);
                 session.delete(role);
                session.update(member);
                transaction.commit();
            } else {
                transaction.rollback();
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
            }
        }catch (PersistenceException e){
            logger.error("database error leaving Role {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void removeRoleFromMember(String username, String remover, int storeId, UserFacade userFacade) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, remover);
            if (member != null) {
                UserRole role = member.getRoleOfStore(storeId);
                member.removeRole(role);
                session.update(member);
                transaction.commit();
            } else {
                transaction.rollback();
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(remover));
            }
        }catch (PersistenceException e){
            logger.error("database error in removeRoleFromMember {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public RequestDTO addRequest(String senderName, String sentName, int storeId, String reqType) {
        logger.info("Adding request to user {} from user {} in store={} with type {}",sentName,senderName,storeId,reqType);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Member member = session.get(Member.class, sentName);
            if (member != null) {
                Request req=new Request(senderName, "You got a request from " + senderName + " to become " + reqType + " in " + storeId, storeId,reqType);
                session.save(req);
                RequestDTO request = member.getRequest(req);
                session.update(member);
                transaction.commit();
                logger.info("Request added to user {} from user {} in store={} with type {} successfully",sentName,senderName,storeId,reqType);
                return request;
            } else {
                transaction.rollback();
                logger.error("User {} doesn't exist",sentName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(senderName));
            }
        }catch (PersistenceException e){
            logger.error("database error in addRequest {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public boolean isApointee(String giverUserName, String userName, int storeId) {
        logger.info("Checking if user {} is apointee of user {} in store={}",userName,giverUserName,storeId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Member member = session.get(Member.class, giverUserName);
            if (member != null) {
                UserRole role = member.getRoleOfStore(storeId);
                boolean res= role.isApointedByUser(userName);
                logger.info("User {} is apointee of user {} in store={}={}",userName,giverUserName,storeId,res);
                return res;
            } else {
                logger.error("User {} doesn't exist",giverUserName);
                throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(giverUserName));
            }
        }catch (PersistenceException e){
            logger.error("database error in isApointee {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void clearGuestCart(int guestID) {
       logger.info("Clearing cart of guest {}",guestID);
       try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Guest guest = session.get(Guest.class, guestID);
            if (guest != null) {
                guest.cart.clear();
                session.update(guest);
                transaction.commit();
                logger.info("Cart of guest {} cleared successfully",guestID);
            } else {
                transaction.rollback();
                logger.error("Guest {} doesn't exist",guestID);
                throw new NoSuchElementException(Error.makeMemberGuestDoesntExistError(guestID));
            }
        }catch (PersistenceException e){
            logger.error("database error in clearGuestCart {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void addProductToCart(int guestId, int storeId, int productId, int amount) {
        logger.info("Adding product to cart of guest {} storeId={}, productId={},amount={}",guestId,storeId,productId,amount);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Guest guest = session.get(Guest.class, guestId);
            if (guest != null) {
                guest.addProductToCart(storeId, productId, amount);
                session.update(guest);
                transaction.commit();
                logger.info("Product added to cart of guest {} successfully",guestId);
            } else {
                transaction.rollback();
                logger.error("Guest {} doesn't exist",guestId);
                throw new NoSuchElementException(Error.makeMemberGuestDoesntExistError(guestId));
            }
        }catch (PersistenceException e){
            logger.error("database error in addProductToCart {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }
    @Override
    public StoreManager createStoreManagerRole(int storeId) {
      logger.info("Creating store manager role for store {}",storeId);
       try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            StoreManager role = new StoreManager(storeId);
            session.save(role);
            transaction.commit();
            logger.info("Store manager role created for store {}",storeId);
            return role;
        }catch (PersistenceException e){
            logger.error("database error in createStoreManagerRole {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }
    @Override
    public StoreOwner createStoreOwnerRole(int storeId, String apointee) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            StoreOwner role = new StoreOwner(storeId, apointee);
            session.save(role);
            transaction.commit();
            return role;
        }catch (PersistenceException e){
            logger.error("database error in createStoreOwnerRole {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        } 
    } 
    public StoreFounder createStoreFounderRole(int storeId, String apointee) {
        logger.info("Creating store founder role for store {} with apointee {}",storeId,apointee);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            StoreFounder role = new StoreFounder(storeId, apointee);
            session.save(role);
            transaction.commit();
            return role;
        }catch (PersistenceException e){
            logger.error("database error in createStoreFounderRole {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        } 
    }  
    @Override
    public void clear() {
        logger.info("Clearing all data");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE basket_products").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE baskets").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE role_permissions").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE user_orders").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE user_roles").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE Notification").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE role_appointments").executeUpdate();
                //session.createNativeQuery("TRUNCATE TABLE user_notifications").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE carts").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE guests").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE Members").executeUpdate();
                session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
                
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                logger.error("Failed to clear data",e);
                throw e;
            }
            logger.info("Data cleared successfully");
    }catch (PersistenceException e){
        logger.error("database error in clear {}", e.getMessage());
        throw new IllegalStateException(Error.makeDBError());
    } 
}

    @Override
    public boolean isGuestExist(int guestID) {
        logger.info("Checking if guest {} exists",guestID);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Guest guest = session.get(Guest.class, guestID);
            boolean res=guest != null;
            logger.info("Guest {} exists={}",guestID,res);
            return res ;
        }catch (PersistenceException e){
            logger.error("database error in isGuestExist {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        } 
    }

    @Override
    public void logoutMembers() {
        logger.info("Logging out all members");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            List<Member> members = session.createQuery("from Member", Member.class).list();
            for (Member member : members) {
                if(member.isLoggedIn()){
                member.logout();
                session.update(member);
            }
            }
            transaction.commit();
            logger.info("All members logged out successfully");
        }catch (PersistenceException e){
            logger.error("database error in logout all members {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        } 
    }

    @Override
    public void removeGuests() {
        logger.info("Removing all guests");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            List<Guest> guests = session.createQuery("from Guest", Guest.class).list();
            for (Guest guest : guests) {
                session.delete(guest);
            }
            transaction.commit();
            logger.info("All guests removed successfully");
        }catch (PersistenceException e){
            logger.error("database error in remove all guests {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        } 
    }
    
}
