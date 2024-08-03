package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;
import com.sadna.sadnamarket.service.Error;

import java.util.*;

public class MemoryBuyPolicyManager extends BuyPolicyManager{
    private List<Integer> buyPolicyIds;
    private List<Integer> lawsBuyPolicyIds;

    public MemoryBuyPolicyManager(BuyPolicyFacade facade) {
        super(facade);
        this.buyPolicyIds = new ArrayList<>();
        this.lawsBuyPolicyIds = new ArrayList<>();
    }

    public boolean hasPolicy(int policyId) {
        synchronized (buyPolicyIds) {
            synchronized (lawsBuyPolicyIds) {
                return buyPolicyIds.contains(policyId) || lawsBuyPolicyIds.contains(policyId);
            }
        }
    }

    public List<Integer> getAllPolicyIds(){
        List<Integer> allIds = new LinkedList<>();
        allIds.addAll(buyPolicyIds);
        allIds.addAll(lawsBuyPolicyIds);
        return allIds;
    }

    public void addBuyPolicy(int buyPolicyId) {
        synchronized (buyPolicyIds) {
            synchronized (lawsBuyPolicyIds) {
                if (hasPolicy(buyPolicyId))
                    throw new IllegalArgumentException(Error.makeBuyPolicyAlreadyExistsError(buyPolicyId));
                buyPolicyIds.add(buyPolicyId);
            }
        }
    }

    public void addLawBuyPolicy(int buyPolicyId) {
        synchronized (buyPolicyIds) {
            synchronized (lawsBuyPolicyIds) {
                if (hasPolicy(buyPolicyId))
                    throw new IllegalArgumentException(Error.makeBuyPolicyAlreadyExistsError(buyPolicyId));
                lawsBuyPolicyIds.add(buyPolicyId);
            }
        }
    }

    public void removeBuyPolicy(int buyPolicyId) {
        synchronized (buyPolicyIds) {
            synchronized (lawsBuyPolicyIds) {
                if (lawsBuyPolicyIds.contains(buyPolicyId)) {
                    throw new IllegalArgumentException(Error.makeCanNotRemoveLawBuyPolicyError(buyPolicyId));
                }
                if (!hasPolicy(buyPolicyId)) {
                    throw new IllegalArgumentException(Error.makeBuyPolicyWithIdDoesNotExistError(buyPolicyId));
                }
                buyPolicyIds.removeIf(id -> id == buyPolicyId);
            }
        }
    }

    public Set<String> canBuy(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user) {
        Set<String> error = new HashSet<>();
        // if one policy says that you cant buy return false;
        synchronized (buyPolicyIds) {
            for (Integer policyId : buyPolicyIds) {
                BuyPolicy policy = facade.getBuyPolicy(policyId);
                error.addAll(policy.canBuy(cart, products, user));
            }
        }
        synchronized (lawsBuyPolicyIds) {
            for (Integer policyId : lawsBuyPolicyIds) {
                BuyPolicy policy = facade.getBuyPolicy(policyId);
                error.addAll(policy.canBuy(cart, products, user));
            }
        }
        return error;
    }

    @Override
    public void clear() {
        this.buyPolicyIds = new ArrayList<>();
        this.lawsBuyPolicyIds = new ArrayList<>();
    }
}
