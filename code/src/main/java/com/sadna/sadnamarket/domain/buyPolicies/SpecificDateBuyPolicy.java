package com.sadna.sadnamarket.domain.buyPolicies;

import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;
import com.sadna.sadnamarket.service.Error;

import java.time.LocalDate;
import java.util.*;

public class SpecificDateBuyPolicy extends SimpleBuyPolicy{
    private int day;
    private int month;
    private int year;

    SpecificDateBuyPolicy(int id, List<BuyType> buytypes, PolicySubject subject, int day, int month, int year) {
        super(id, buytypes, subject);
        if(day == -1 && month == -1 && year == -1)
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("specific date", "-"));
        if(day != -1 && (day < 1 || day > 31)) {
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("specific date", "day: " + day));
        }
        if(month != -1 && (month < 1 || month > 12)) {
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("specific date", "month: " + month));
        }
        if(year != -1 && year < LocalDate.now().getYear()) {
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("specific date", "year: " + year));
        }
        this.day = day;
        this.month = month;
        this.year = year;
    }

    SpecificDateBuyPolicy(List<BuyType> buytypes, PolicySubject subject, int day, int month, int year) {
        super(buytypes, subject);
        if(day == -1 && month == -1 && year == -1)
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("specific date", "-"));
        if(day != -1 && (day < 1 || day > 31)) {
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("specific date", "day: " + day));
        }
        if(month != -1 && (month < 1 || month > 12)) {
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("specific date", "month: " + month));
        }
        if(year != -1 && year < LocalDate.now().getYear()) {
            throw new IllegalArgumentException(Error.makeBuyPolicyParamsError("specific date", "year: " + year));
        }
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public SpecificDateBuyPolicy() {
    }

    public static LocalDate getCurrDate() {
        return LocalDate.now();
    }

    private boolean canBuyBool(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user) {
        if(policySubject.subjectAmount(cart, products) > 0) {
            LocalDate now = getCurrDate();
            if(day != -1 && now.getDayOfMonth() != day)
                return true;
            if(month != -1 && now.getMonthValue() != month)
                return true;
            if(year != -1 && now.getYear() != year)
                return true;
            return false;
        }
        return true;
    }

    @Override
    public Set<String> canBuy(List<CartItemDTO> cart, Map<Integer, ProductDTO> products, MemberDTO user) {
        Set<String> error = new HashSet<>();
        if(!canBuyBool(cart, products, user)) {
            error.add(Error.makeSpecificDateBuyPolicyError(policySubject.getSubject()));
        }
        return error;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    protected boolean dependsOnUser() {
        return false;
    }

    @Override
    public String getPolicyDesc() {
        StringBuilder date = new StringBuilder();
        if (day != -1) {
            date.append(day).append(getDaySuffix(day)).append(" ");
        }
        if (month != -1) {
            if (date.length() > 0) {
                date.append("of ");
            }
            Calendar calendar = Calendar.getInstance();
            String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            date.append(monthName + " ");
        }
        if (year != -1) {
            date.append(year);
        }

        return String.format("%s can not be bought on %s.", policySubject.getDesc(), date.toString().trim());
    }

    private static String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SpecificDateBuyPolicy that = (SpecificDateBuyPolicy) o;
        return day == that.day && month == that.month && year == that.year;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, month, year);
    }

    @Override
    public BuyPolicyData generateData() {
        return new DateBuyPolicyData(day,month,year,getPolicySubject().dataString());
    }
}
