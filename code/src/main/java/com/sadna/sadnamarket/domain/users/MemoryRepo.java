package com.sadna.sadnamarket.domain.users;

import com.sadna.sadnamarket.service.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class MemoryRepo implements IUserRepository {
    private static HashMap<String, Member> members;
    private static HashMap<Integer, Guest> guests;
    private int guestId = 0;
    private static final Logger logger = LogManager.getLogger(MemoryRepo.class);

    public MemoryRepo() {
        logger.info("Entering MemoryRepo constructor");
        members = new HashMap<>();
        guests = new HashMap<>();
        logger.info("Exiting MemoryRepo constructor");
    }


    public Member getMember(String userName) {
        logger.info("Entering getMember with userName={}", userName);
        Member member = members.get(userName);
        if (member == null) {
            logger.error("Exception in getMember: User with userName={} does not exist", userName);
            throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));
        }
        logger.info("Exiting getMember with result={}", member);
        return member;
    }

    public boolean hasMember(String username) {
        logger.info("Entering hasMember with username={}", username);
        boolean result = members.containsKey(username);
        if (!result) {
            logger.error("Exception in hasMember: User with username={} does not exist", username);
            throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(username));
        }
        logger.info("Exiting hasMember with result={}", result);
        return result;
    }

    @Override
    public void store(String username,String firstName, String lastName,String emailAddress,String phoneNumber, LocalDate birthDate) {
        logger.info("Entering store with username={}, firstName={}, lastName={}, emailAddress={}, phoneNumber={}, birthDate={}", username, firstName, lastName, emailAddress, phoneNumber, birthDate);
        Member member = new Member(username,firstName,lastName,emailAddress,phoneNumber,birthDate);
        members.put(member.getUsername(), member);
        logger.info("Exiting store");
    }

    public int addGuest() {
        logger.info("Entering addGuest");
        guestId=guestId+1;
        guests.put(guestId, new Guest(guestId));
        logger.info("Exiting addGuest with result={}", guestId);
        return guestId;
    }

    public void deleteGuest(int guestID) {
        logger.info("Entering deleteGuest with guestID={}", guestID);
        if (hasGuest(guestID)) {
            guests.remove(guestID);
        }
        logger.info("Exiting deleteGuest");
    }

    public boolean hasGuest(int guestID) {
        logger.info("Entering hasGuest with guestID={}", guestID);
        boolean result = guests.containsKey(guestID);
        if (!result) {
            logger.error("Exception in hasGuest: Guest with guestID={} does not exist", guestID);
            throw new NoSuchElementException(Error.makeMemberGuestDoesntExistError(guestID));
        }
        logger.info("Exiting hasGuest with result={}", result);
        return result;
    }

    private Guest getGuest(int guestID) {
        logger.info("Entering getGuest with guestID={}", guestID);
        Guest guest = guests.get(guestID);
        if (guest == null) {
            logger.error("Exception in getGuest: Guest with guestID={} does not exist", guestID);
            throw new NoSuchElementException(Error.makeMemberGuestDoesntExistError(guestID));
        }
        logger.info("Exiting getGuest with result={}", guest);
        return guest;
    }

    @Override
    public List<CartItemDTO> getUserCart(String username) {
        logger.info("getting member cart for {}",username);
        List<CartItemDTO> cartItemDTOs=getMember(username).getCartItems();
        logger.info("got member cart for {}: {}",username,cartItemDTOs);
        return cartItemDTOs;
    }

    @Override
    public List<CartItemDTO> getGuestCart(int guestID) {
        logger.info("getting guest cart for {}",guestID);
        List<CartItemDTO> cartItemDTOs=getGuest(guestID).getCartItems();
        logger.info("got guest cart for {}: {}",guestID,cartItemDTOs);
        return cartItemDTOs;
    }

    @Override
    public boolean hasPermissionToRole(String userName, Permission permission, int storeId) {
        logger.info("Entering hasPermissionToRole with userName={}, permission={}, storeId={}", userName, permission, storeId);
        Member member = getMember(userName);
        boolean result = member.hasPermissionToRole(permission, storeId);
        logger.info("Exiting hasPermissionToRole with result={}", result);
        return result;
    }

    @Override
    public NotificationDTO addNotification(String userName, String msg) {
        logger.info("Entering addNotification with userName={}, msg={}", userName, msg);
        NotificationDTO notificationDTO= getMember(userName).addNotification(msg);
        logger.info("Exiting addNotification with result={}", notificationDTO);
        return notificationDTO;
    }

    @Override
    public boolean isLoggedIn(String username) {
        logger.info("Entering isLoggedIn with username={}", username);
        Member member = getMember(username);
        boolean result = member.isLoggedIn();
        logger.info("Exiting isLoggedIn with result={}", result);
        return result;
    }

    @Override
    public void setLogin(String userName, boolean isLoggedIn) {
        logger.info("Entering setLogin with userName={}, isLoggedIn={}", userName, isLoggedIn);
        Member member = getMember(userName);
        member.setLogin(isLoggedIn);
        logger.info("Exiting setLogin");
    }

    @Override
    public void addProductToCart(String username, int storeId, int productId, int amount) {
        logger.info("Entering addProductToCart with username={}, storeId={}, productId={}, amount={}", username, storeId, productId, amount);
        Member member = getMember(username);
        member.addProductToCart(storeId, productId, amount);
        logger.info("Exiting addProductToCart");
    }

    @Override
    public void removeProductFromCart(String username, int storeId, int productId) {
        logger.info("Entering removeProductFromCart with username={}, storeId={}, productId={}", username, storeId, productId);
        Member member = getMember(username);
        member.removeProductFromCart(storeId, productId);
        logger.info("Exiting removeProductFromCart");
    }

    @Override
    public void removeProductFromGuestCart(int guestId, int storeId, int productId) {
        logger.info("Entering removeProductFromGuestCart with guestId={}, storeId={}, productId={}", guestId, storeId, productId);
        Guest guest = getGuest(guestId);
        guest.removeProductFromCart(storeId, productId);
        logger.info("Exiting removeProductFromGuestCart");
    }

    @Override
    public void changeQuantityCart(String username, int storeId, int productId, int amount) {
        logger.info("Entering changeQuantityCart with username={}, storeId={}, productId={}, amount={}", username, storeId, productId, amount);
        Member member = getMember(username);
        member.changeQuantityCart(storeId, productId, amount);
        logger.info("Exiting changeQuantityCart");
    }

    @Override
    public void guestChangeQuantityCart(int guestId, int storeId, int productId, int amount) {
        logger.info("Entering guestChangeQuantityCart with guestId={}, storeId={}, productId={}, amount={}", guestId, storeId, productId, amount);
        Guest guest = getGuest(guestId);
        guest.changeQuantityCart(storeId, productId, amount);
        logger.info("Exiting guestChangeQuantityCart");
    }

    @Override
    public void logout(String userName) {
        logger.info("Entering logout with userName={}", userName);
        getMember(userName).logout();
        logger.info("Exiting logout");
    }

    @Override
    public void setCart(String userName, List<CartItemDTO> cartLst) {
        logger.info("Entering setCart with userName={}, cart={}", userName, cartLst);
        Member member = getMember(userName);
        Cart cart=new Cart(cartLst);
        member.setCart(cart);
        logger.info("Exiting setCart");
    }

    @Override
    public void addRole(String username, StoreManager storeManager) {
        logger.info("Entering addRole with username={}, storeManager={}", username, storeManager);
        Member member = getMember(username);
        member.addRole(storeManager);
        logger.info("Exiting addRole");
    }

    @Override
    public void addRole(String username, StoreOwner storeOwner) {
        logger.info("Entering addRole with username={}, storeOwner={}", username, storeOwner);
        Member member = getMember(username);
        member.addRole(storeOwner);
        logger.info("Exiting addRole");
    }

    @Override
    public void addPermissionToRole(String userName, Permission permission, int storeId) {
        logger.info("Entering addPermissionToRole with userName={}, permission={}, storeId={}", userName, permission, storeId);
        Member member = getMember(userName);
        member.addPermissionToRole(permission, storeId);
        logger.info("Exiting addPermissionToRole");
    }

    @Override
    public void removePermissionFromRole(String userName, Permission permission, int storeId) {
        logger.info("Entering removePermissionFromRole with userName={}, permission={}, storeId={}", userName, permission, storeId);
        Member member = getMember(userName);
        member.removePermissionFromRole(permission, storeId);
        logger.info("Exiting removePermissionFromRole");
    }

    @Override
    public List<Permission> getPermissions(String userName, int storeId) {
        logger.info("Entering getPermissions with userName={}, storeId={}", userName, storeId);
        Member member = getMember(userName);
        List<Permission> permissions = member.getPermissions(storeId);
        logger.info("Exiting getPermissions with result={}", permissions);
        return permissions;
    }

    @Override
    public void setFirstName(String userName, String firstName) {
        logger.info("Entering setFirstName with userName={}, firstName={}", userName, firstName);
        Member member = getMember(userName);
        member.setFirstName(firstName);
        logger.info("Exiting setFirstName");
    }

    @Override
    public void setLastName(String userName, String lastName) {
        logger.info("Entering setLastName with userName={}, lastName={}", userName, lastName);
        Member member = getMember(userName);
        member.setLastName(lastName);
        logger.info("Exiting setLastName");
    }

    @Override
    public void setEmailAddress(String userName, String emailAddress) {
        logger.info("Entering setEmailAddress with userName={}, emailAddress={}", userName, emailAddress);
        Member member = getMember(userName);
        member.setEmailAddress(emailAddress);
        logger.info("Exiting setEmailAddress");
    }

    @Override
    public void setPhoneNumber(String userName, String phoneNumber) {
        logger.info("Entering setPhoneNumber with userName={}, phoneNumber={}", userName, phoneNumber);
        Member member = getMember(userName);
        member.setPhoneNumber(phoneNumber);
        logger.info("Exiting setPhoneNumber");
    }

    @Override
    public void setBirthday(String username,LocalDate birthDate) {
        logger.info("Entering setBirthday with birthDate={}", birthDate);
        getMember(null).setBirthday(birthDate);
        logger.info("Exiting setBirthday");
    }

    @Override
    public MemberDTO getMemberDTO(String userName) {
        logger.info("Entering getMemberDTO with userName={}", userName);
        Member member = getMember(userName);
        MemberDTO memberDTO = new MemberDTO(member);
        logger.info("Exiting getMemberDTO with result={}", memberDTO);
        return memberDTO;
    }

    @Override
    public List<Integer> getOrdersHistory(String username) {
        logger.info("Entering getOrdersHistory with username={}", username);
        Member member = getMember(username);
        List<Integer> ordersHistory = member.getOrdersHistory();
        logger.info("Exiting getOrdersHistory with result={}", ordersHistory);
        return ordersHistory;
    }

    @Override
    public void clearCart(String username) {
        logger.info("Entering clearCart with username={}", username);
        Member member = getMember(username);
        member.clearCart();
        logger.info("Exiting clearCart");
    }

    @Override
    public List<NotificationDTO> getNotifications(String username) {
        logger.info("Entering getNotifications with username={}", username);
        List<NotificationDTO> notifes = new ArrayList<>(getMember(username).getNotifications().values().stream().map((Notification notification)->notification.toDTO()).toList());
        logger.info("Exiting getNotifications with result={}", notifes);
        return notifes;
    }

    @Override
    public void addOrder(String username, int orderId) {
        logger.info("Entering addOrder with username={}, orderId={}", username, orderId);
        Member member = getMember(username);
        member.addOrder(orderId);
        logger.info("Exiting addOrder");
    }

    @Override
    public List<UserRoleDTO> getUserRolesString(String userName) {
        logger.info("Entering getUserRolesString with userName={}", userName);
        Member member = getMember(userName);
        List<UserRoleDTO> userRolesString = member.getUserRolesString();
        logger.info("Exiting getUserRolesString with result={}", userRolesString);
        return userRolesString;
    }

    @Override
    public UserRole getRoleOfStore(String userName, int storeId) {
        logger.info("Entering getRoleOfStore with userName={}, storeId={}", userName, storeId);
        Member member = getMember(userName);
        UserRole roleOfStore = member.getRoleOfStore(storeId);
        logger.info("Exiting getRoleOfStore with result={}", roleOfStore);
        return roleOfStore;
    }

    @Override
    public List<UserRoleHibernate> getUserRoles(String username) {
        logger.info("Entering getUserRoles with username={}", username);
        Member member = getMember(username);
        List<UserRoleHibernate> userRoles = member.getUserRoles();
        logger.info("Exiting getUserRoles with result={}", userRoles);
        return userRoles;
    }

    @Override
    public RequestDTO addOwnerRequest(String senderName, UserFacade userFacade, String userName, int store_id) {
        logger.info("Entering addOwnerRequest with senderName={}, userFacade={}, userName={}, store_id={}", senderName, userFacade, userName, store_id);
        Member sender = getMember(senderName);
        RequestDTO request = sender.addOwnerRequest(userFacade, userName, store_id);
        logger.info("Exiting addOwnerRequest with result={}", request);
        return request;
    }

    @Override
    public RequestDTO addManagerRequest(String senderName, UserFacade userFacade, String userName, int store_id) {
        logger.info("Entering addManagerRequest with senderName={}, userFacade={}, userName={}, store_id={}", senderName, userFacade, userName, store_id);
        Member sender = getMember(senderName);
        RequestDTO request = sender.addManagerRequest(userFacade, userName, store_id);
        logger.info("Exiting addManagerRequest with result={}", request);
        return request;
    }

    @Override
    public Request getRequest(String acceptingName, int requestID) {
        logger.info("Entering getRequest with acceptingName={}, requestID={}", acceptingName, requestID);
        Member acceptingMember = getMember(acceptingName);
        Request request = acceptingMember.getRequest(requestID);
        logger.info("Exiting getRequest with result={}", request);
        return request;
    }

    @Override
    public void accept(String acceptingName, int requestID,UserFacade userFacade) {
        logger.info("Entering accept with acceptingName={}, requestID={}", acceptingName, requestID);
        Member acceptingMember = getMember(acceptingName);
        acceptingMember.accept(requestID,userFacade);
        logger.info("Exiting accept");
    }

    @Override
    public void addApointer(String apointer, String acceptingName, int storeId) {
        logger.info("Entering addApointer with apointer={}, acceptingName={}, storeId={}", apointer, acceptingName, storeId);
        Member member = getMember(apointer);
        member.addApointer(acceptingName, storeId);
        logger.info("Exiting addApointer");
    }

    @Override
    public void reject(String rejectingName, int requestID) {
        logger.info("Entering reject with rejectingName={}, requestID={}", rejectingName, requestID);
        Member rejectingMember = getMember(rejectingName);
        rejectingMember.reject(requestID);
        logger.info("Exiting reject");
    }


    @Override
    public boolean isApointee(String checker,String userName, int storeId) {
    logger.info("Entering isApointee with checker={}, userName={}, permission={}, storeId={}", checker, userName, storeId);
    boolean res= getMember(checker).getRoleOfStore(storeId).getAppointers().contains(userName);
    logger.info("Exiting isApointee with result={}", res);
    return res;
    }


    @Override
    public void leaveRole(String username, int storeId,UserFacade userFacade) {
        logger.info("Entering leaveRole with username={}, storeId={}", username, storeId);
        Member member=getMember(username);
        UserRole role=member.getRoleOfStore(storeId);
        role.leaveRole(new UserRoleVisitor(), storeId, member, userFacade);
        member.removeRole(role);
        logger.info("Exiting leaveRole");
    }


    @Override
    public void removeRoleFromMember(String username,String remover, int storeId, UserFacade userFacade) {
        logger.info("Entering removeRoleFromMember with username={}, remover={}, storeId={}", username, remover, storeId);
        List<UserRoleHibernate> roles=getUserRoles(username);
        for(UserRole role : roles){
           if(role.getStoreId()==storeId){
            if(!role.getApointee().equals(remover))
                throw new IllegalStateException("you can only remove your apointees");
            role.leaveRole(new UserRoleVisitor(), storeId, getMember(username),userFacade);;
           }
        }
        logger.info("Exiting removeRoleFromMember");
    }


    @Override
    public RequestDTO addRequest(String senderName, String sentName,int storeId, String reqType) {
       RequestDTO requestDTO= getMember(sentName).getRequest(senderName, storeId, reqType);
       return requestDTO;
    }


    @Override
    public void clearGuestCart(int guestID) {
        logger.info("Entering clearGuestCart with guestID={}", guestID);
        Guest guest = getGuest(guestID);
        guest.getCart().clear();;
        logger.info("Exiting clearGuestCart");
    }


    @Override
    public void addProductToCart(int guestId, int storeId, int productId, int amount) {
        logger.info("Entering addProductToCart with guestId={}, storeId={}, productId={}, amount={}", guestId, storeId, productId, amount);
        getGuest(guestId).addProductToCart(storeId, productId, amount);
        logger.info("Exiting addProductToCart");
    }
    @Override
    public StoreManager createStoreManagerRole(int storeId){
        return new StoreManager(storeId);
    }
    @Override
    public StoreOwner createStoreOwnerRole(int storeId,String apointee){
        return new StoreOwner(storeId,apointee);
    }
    @Override
    public StoreFounder createStoreFounderRole(int storeId, String apointee){
        return new StoreFounder(storeId,apointee);
    }
    @Override
    public void clear() {
    }


    @Override
    public boolean isGuestExist(int guestID) {
        logger.info("check if guest exist with guestID={}", guestID);
        boolean res= guests.containsKey(guestID);
        logger.info("Exiting isGuestExist with result={}", res);
        return res;
    }


    @Override
    public void logoutMembers() {
        logger.info("Entering logoutMembers");
        logger.info("Exiting logoutMembers as no need to logout in memory");
    }


    @Override
    public void removeGuests() {
        logger.info("Entering removeGuests");
        logger.info("Exiting removeGuests as no need to remove in memory");
    }
}
