package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;
import com.sadna.sadnamarket.service.Error;

import java.util.*;

public class KgLimitBuyPolicy extends SimpleBuyPolicy{
    private double minValue;
    private double maxValue; // -1 for no limit

    KgLimitBuyPolicy(int id, List<BuyType> buytypes, PolicySubject subject, double minKg, double maxKg) {
        super(id, buytypes, subject);

        if(minKg < -1 || maxKg < -1 || (maxKg != -1 && minKg > maxKg))
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("kg limit", String.format("%.0f", minKg).trim(), String.format("%.0f", maxKg).trim()));

        this.minValue = minKg;
        this.maxValue = maxKg;
    }

    KgLimitBuyPolicy(List<BuyType> buytypes, PolicySubject subject, double minKg, double maxKg) {
        super(buytypes, subject);

        if(minKg < -1 || maxKg < -1 || (maxKg != -1 && minKg > maxKg))
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("kg limit", String.format("%.0f", minKg).trim(), String.format("%.0f", maxKg).trim()));

        this.minValue = minKg;
        this.maxValue = maxKg;
    }

    @Override
    public Set<String> canBuy(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user) {
        double totalWeight = 0;
        for(CartItemDTO item : cart) {
            int productId = item.getProductId();
            ProductDTO product = products.get(productId);
            if(policySubject.isSubject(product)) {
                totalWeight += product.getProductWeight() * item.getAmount();
            }
        }
        Set<String> error = new HashSet<>();
        if(maxValue == -1) {
            if(totalWeight < minValue) {
                error.add(Error.makeKgLimitBuyPolicyError(policySubject.getSubject(), String.valueOf(minValue), String.valueOf(maxValue)));
                return error;
            }
        }
        else if(!(totalWeight >= minValue && totalWeight <= maxValue)) {
            error.add(Error.makeKgLimitBuyPolicyError(policySubject.getSubject(), String.valueOf(minValue), String.valueOf(maxValue)));
            return error;
        }
        return error;
    }

    public double getMinKg() {
        return minValue;
    }

    public void setMinKg(double minKg) {
        this.minValue = (int)minKg;
    }

    public double getMaxKg() {
        return maxValue;
    }

    public void setMaxKg(double maxKg) {
        this.maxValue = (int)maxKg;
    }

    @Override
    protected boolean dependsOnUser() {
        return false;
    }

    @Override
    public String getPolicyDesc() {
        if(maxValue == -1)
            return String.format("More than %f Kg of %s must be bought.", minValue, policySubject.getDesc());
        if(minValue == -1)
            return String.format("You can not buy more than %f Kg of %s.", maxValue, policySubject.getDesc());
        return String.format("%f - %f Kg of %s must be bought.", minValue, maxValue, policySubject.getDesc());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        KgLimitBuyPolicy that = (KgLimitBuyPolicy) o;
        return Double.compare(that.minValue, minValue) == 0 && Double.compare(that.maxValue, maxValue) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minValue, maxValue);
    }

    @Override
    public BuyPolicyData generateData() {
        return new RangedBuyPolicyData(getPolicySubject().dataString(), minValue, maxValue, BuyPolicyTypeCodes.KG);
    }
}
