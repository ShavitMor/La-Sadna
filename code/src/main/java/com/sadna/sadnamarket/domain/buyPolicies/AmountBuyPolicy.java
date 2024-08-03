package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;
import com.sadna.sadnamarket.service.Error;

import java.util.*;


public class AmountBuyPolicy extends SimpleBuyPolicy {
    private int minValue;
    private int maxValue; // if this is equal to -1 there is no limit

    AmountBuyPolicy(int id, List<BuyType> buytypes, PolicySubject subject, int from, int to) {
        super(id, buytypes, subject);

        if((from == -1 && to == -1) || from < -1 || to < -1 || (to != -1 && to < from)) {
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("amount", String.valueOf(from), String.valueOf(to)));
        }
        this.minValue = from;
        this.maxValue = to;
    }

    AmountBuyPolicy(List<BuyType> buytypes, PolicySubject subject, int from, int to) {
        super(buytypes, subject);

        if((from == -1 && to == -1) || from < -1 || to < -1 || (to != -1 && to < from)) {
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("amount", String.valueOf(from), String.valueOf(to)));
        }
        this.minValue = from;
        this.maxValue = to;
    }

    @Override
    public Set<String> canBuy(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user) {
        Set<String> error = new HashSet<>();
        int amount = policySubject.subjectAmount(cart, products);
        if(maxValue == -1) {
            if(amount < minValue) {
                error.add(Error.makeAmountBuyPolicyError(policySubject.getSubject(), minValue, maxValue));
                return error;
            }
        }
        else if(!(amount <= maxValue && amount >= minValue)) {
            error.add(Error.makeAmountBuyPolicyError(policySubject.getSubject(), minValue, maxValue));
            return error;
        }
        return error;
    }

    public int getFrom() {
        return minValue;
    }

    public void setFrom(int from) {
        this.minValue = from;
    }

    public int getTo() {
        return maxValue;
    }

    public void setTo(int to) {
        this.maxValue = to;
    }

    @Override
    protected boolean dependsOnUser() {
        return false;
    }

    @Override
    public String getPolicyDesc() {
        if(maxValue == -1)
            return String.format("More than %d units of %s must be bought.", minValue, policySubject.getDesc());
        if(minValue == -1)
            return String.format("Less than %d units of %s must be bought.", maxValue, policySubject.getDesc());
        return String.format("%d - %d units of %s must be bought.", minValue, maxValue, policySubject.getDesc());
    }

    @Override
    public boolean equals(Object o) {
        if(!super.equals(o))
            return false;
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AmountBuyPolicy that = (AmountBuyPolicy) o;
        return minValue == that.minValue && maxValue == that.maxValue;
    }

    @Override
    public BuyPolicyData generateData() {
        return new RangedBuyPolicyData(getPolicySubject().dataString(), Double.valueOf(minValue), Double.valueOf(maxValue), BuyPolicyTypeCodes.AMOUNT);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minValue, maxValue);
    }
}
