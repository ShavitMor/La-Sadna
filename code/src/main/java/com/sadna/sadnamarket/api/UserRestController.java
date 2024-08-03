package com.sadna.sadnamarket.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.domain.payment.CreditCardDTO;
import com.sadna.sadnamarket.domain.supply.AddressDTO;
import com.sadna.sadnamarket.service.MarketService;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.logging.Log;




@RestController
@Profile("default")
@RequestMapping("/api/user")
@CrossOrigin(origins = "*",allowedHeaders = "*") // Allow cross-origin requests from any source
public class UserRestController {
    @Autowired
    MarketService marketService;

    @PostMapping("/enterAsGuest")
    public Response enterAsGuest() {
        return marketService.enterAsGuest();
    }

    @PostMapping("/exitGuest")
    public Response exitGuest(@RequestParam int guestId) {
        return marketService.exitGuest(guestId);
    }


    @GetMapping("/isExist")
    public Response isExist(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.memberExists(username);
    }

    @PatchMapping("/setSystemAdminstor")
    public Response setSystemAdminstor(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.setSystemAdminstor(username);
    }

    @PostMapping("/login")
    public Response login(@RequestBody LoginRequest loginRequest) {
        return marketService.login(loginRequest.getUsername(), loginRequest.getPassword());
    }
    @PostMapping("/loginFromGuest")
    public Response login(@RequestBody LoginFromGuestRequest loginRequest) {
        return marketService.login(loginRequest.getUsername(), loginRequest.getPassword(),loginRequest.getGuestId());
    }

    @PatchMapping("/addProductToCart")
    public Response addProductToCart(@RequestParam String username,@RequestBody ChangeCartRequest storeRequest ,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.addProductToCart(username,storeRequest.storeId,storeRequest.productId,storeRequest.amount);
    }

    @PatchMapping("/guest/addProductToCart")
    public Response addProductToCartForGuest(@RequestParam int guestId,@RequestBody ChangeCartRequest storeRequest) {
        return marketService.addProductToCart(guestId,storeRequest.storeId,storeRequest.productId,storeRequest.amount);
    }
    @PatchMapping("/removeProductFromCart")
    public Response removeProductFromCart(@RequestParam String username,@RequestBody ChangeCartRequest storeRequest,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.removeProductFromCart(username,storeRequest.getStoreId(),storeRequest.getProductId());
    }

    @PatchMapping("/guest/removeProductFromCart")
    public Response removeProductFromCartForGuest(@RequestParam int guestId,@RequestBody ChangeCartRequest storeRequest) {
        return marketService.removeProductFromCart(guestId,storeRequest.getStoreId(),storeRequest.getProductId());
    }

    @PatchMapping("/changeQuantityCart")
    public Response changeQuantityCart(@RequestParam String username, @RequestBody ChangeCartRequest storeRequest,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.changeQuantityCart(username,storeRequest.getStoreId(),storeRequest.getProductId(), storeRequest.getAmount());
    }

    @PatchMapping("/guest/changeQuantityCart")
    public Response changeQuantityCartForGusts(@RequestParam int guestId, @RequestBody StoreRequest storeRequest) {
        return marketService.changeQuantityCart(guestId,storeRequest.getStoreId(),storeRequest.getProductId(), storeRequest.getAmount());
    }

    @PostMapping("/token/acceptRequest")
    public Response acceptRequestByToken(@RequestParam String newUsername,@RequestParam int storeId,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        return marketService.acceptRequest(token,newUsername,storeId);
    }

    @PostMapping("/acceptRequest")
    public Response acceptRequest(@RequestParam String acceptingName,@RequestParam int requestID,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,acceptingName);
        return marketService.acceptRequest(acceptingName,requestID);
    }

    @PostMapping("/rejectRequest")
    public Response rejectRequest(@RequestParam String acceptingName,@RequestParam int requestID,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,acceptingName);
        return marketService.rejectRequest(acceptingName,requestID);
    }

    @PostMapping("/okNotification")
    public Response okNotification(@RequestParam String username,@RequestParam int notifID,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.okNotification(username,notifID);
    }

    @PostMapping("/loginUsingJwt")
    public Response loginUsingJwt(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        return marketService.loginUsingToken(token,username);
    }
    @PostMapping("/logout")
    public Response logout(@RequestBody SetRequest request) {
        return marketService.logout(request.getField());
    }

    @PostMapping("/register")
        public Response register(@RequestBody RegisterRequest registerRequest) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate birthDate = LocalDate.parse(registerRequest.getBirthDate(), formatter);
            return marketService.register(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getEmail(),
                registerRequest.getPhoneNumber(),
                birthDate
            );
    }
    
    @PostMapping("/leaveRole")
    public Response leaveRole(@RequestParam String username,@RequestParam int firstName,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.leaveRole(username,firstName);
    }

    @PatchMapping("/setFirstName")
    public Response setFirstName(@RequestParam String username, @RequestBody SetRequest req,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.setFirstName(username, req.getField());
    }

    @PatchMapping("/setLastName")
    public Response setLastName(@RequestParam String username, @RequestBody SetRequest req,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.setLastName(username, req.getField());
    }

    @PatchMapping("/setEmailAddress")
    public Response setEmailAddress(@RequestParam String username,@RequestBody SetRequest req,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.setEmailAddress(username, req.getField());
    }

    @PatchMapping("/setPhoneNumber")
    public Response setPhoneNumber(@RequestParam String username, @RequestBody SetRequest req,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.setPhoneNumber(username, req.getField());
    }
    @PatchMapping("/setBirthday")
    public Response setBirthday(@RequestParam String username, @RequestBody SetRequest req,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(req.getField(), formatter);
        return marketService.setBirthDate(username, birthDate);
    }
    @GetMapping("/getOrderHistory")
    public Response getOrderHistory(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkTokenSystemManager(token, username);;
        return marketService.getOrderHistory(username);
    }

    @GetMapping("/getOrderDTOHistory")
    public Response getOrderDTOHistory(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.getOrderDTOHistory(username);
    }

    @GetMapping("/viewCart")
    public Response viewCart(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.viewCart(username);
    }

    @GetMapping("/guest/viewCart")
    public Response viewCartForGuest(@RequestParam int guestId) {
        return marketService.viewCart(guestId);
    }

    @PostMapping("/purchaseCart")
    public Response purchaseCart(@RequestParam String username,@RequestBody CartRequest cartRequest,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.purchaseCart(username,cartRequest.getCreditCard(),cartRequest.getAddressDTO());
    }

    @PostMapping("/guest/purchaseCart")
    public Response purchaseCartForGuest(@RequestParam int guestId,@RequestBody CartRequest cartRequest) {
        return marketService.purchaseCart(guestId,cartRequest.getCreditCard(),cartRequest.getAddressDTO());
    }

    @GetMapping("/getAllOrderDTOHistory")
    public Response getAllOrderDTOHistory(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.getAllOrderDTOHistory(username);
    }

    @GetMapping("/getUserCart")
    public Response getUserCart(@RequestParam int guestId) {
        return marketService.getUserCart(guestId);
    }
    @GetMapping("/getUserDTO")
    public Response getMemberDTO(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }   
        marketService.checkToken(token,username);
        return marketService.getMemberDto(username);
    }  
    @GetMapping("/getUserRoles")
    public Response getMemberRoles(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }   
        marketService.checkToken(token,username);
        return marketService.getUserRoles(username);
    }

    @GetMapping("/getNotifications")
    public Response getUserNotifications(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.getUserNotifications(username);
    }

    @PostMapping("/checkMemberCart")
    public Response checkMemberCart(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.checkMemberCart(username);
    }
    @PostMapping("/checkGuestCart")
    public Response checkGuestCart(@RequestParam int guestId) {
        return marketService.checkGuestCart(guestId);
    }
    @PostMapping("/checkIfSystemManager")
    public Response checkIfSystemManager(@RequestParam String username,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Skip "Bearer " prefix
        }
        marketService.checkToken(token,username);
        return marketService.checkIfSystemManager(username);
    }
    
    @PostMapping("/checkGuestExist")
    public Response checkGuestExist(@RequestParam int guestId) {
        return marketService.isGuestExist(guestId);
    }
    
}
