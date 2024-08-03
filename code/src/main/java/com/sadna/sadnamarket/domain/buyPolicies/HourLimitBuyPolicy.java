package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;
import com.sadna.sadnamarket.service.Error;

import java.time.LocalTime;
import java.util.*;

public class HourLimitBuyPolicy extends SimpleBuyPolicy{
    private LocalTime minValue;
    private LocalTime maxValue; // null if no limit

    HourLimitBuyPolicy(int id, List<BuyType> buytypes, PolicySubject subject, LocalTime from, LocalTime to) {
        super(id, buytypes, subject);
        if(to == null)
            to = LocalTime.of(23, 59, 59);
        if(from == null)
            from = LocalTime.of(0, 0);
        if(to.isBefore(from)) {
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("hour limit", from.toString(), to.toString()));
        }
        this.minValue = from;
        this.maxValue = to;
    }

    HourLimitBuyPolicy(List<BuyType> buytypes, PolicySubject subject, LocalTime from, LocalTime to) {
        super(buytypes, subject);
        if(to == null)
            to = LocalTime.of(23, 59, 59);
        if(from == null)
            from = LocalTime.of(0, 0);
        if(to.isBefore(from)) {
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("hour limit", from.toString(), to.toString()));
        }
        this.minValue = from;
        this.maxValue = to;
    }

    private int getTime(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private LocalTime getTime(int time) {
        return LocalTime.of(time / 60, time % 60);
    }

    @Override
    public Set<String> canBuy(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user) {
        Set<String> error = new HashSet<>();
        if(policySubject.subjectAmount(cart, products) > 0) {
            if(!isTimeInLimit()) {
                error.add(Error.makeHourLimitBuyPolicyError(policySubject.getSubject(), minValue, maxValue));
            }
        }
        return error;
    }

    public static LocalTime getCurrTime() {
        return LocalTime.now();
    }

    private boolean isTimeInLimit() {
        LocalTime now = getCurrTime();
        return now.isBefore(maxValue) && now.isAfter(minValue);
    }

    public LocalTime getFrom() {
        return minValue;
    }

    public void setFrom(LocalTime from) {
        this.minValue = from;
    }

    public LocalTime getTo() {
        return maxValue;
    }

    public void setTo(LocalTime to) {
        this.maxValue = to;
    }

    @Override
    protected boolean dependsOnUser() {
        return false;
    }

    @Override
    public String getPolicyDesc() {
        /*if(minValue == null)
            return String.format("%s can only be bought before %s.", policySubject.get(0).getDesc(), maxValue.toString());
        if(maxValue == null)
            return String.format("%s can only be bought after %s.", policySubject.get(0).getDesc(), minValue.toString());
        */
        return String.format("%s can only be bought at %s - %s.", policySubject.getDesc(), minValue.toString(), maxValue.toString());

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HourLimitBuyPolicy buyPolicy = (HourLimitBuyPolicy) o;
        return Objects.equals(minValue, buyPolicy.minValue) && Objects.equals(maxValue, buyPolicy.maxValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minValue, maxValue);
    }

    @Override
    public BuyPolicyData generateData() {
        return new RangedBuyPolicyData(getPolicySubject().dataString(), minValue.getHour() + ((double)minValue.getMinute())/60, maxValue.getHour() + ((double)maxValue.getMinute())/60, BuyPolicyTypeCodes.HOUR);
    }
}
