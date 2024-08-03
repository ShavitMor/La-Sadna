package com.sadna.sadnamarket.domain.supply;

public class AddressDTO {
    public String country;
    public String city;
    public String addressLine1;
    public String addressLine2;
    public String zipCode;
    public String ordererName;
    public String contactPhone;
    public String contactEmail;
    public String ordererId;

    public AddressDTO(String country, String city, String addressLine1, String addressLine2, String zipCode, String ordererName, String contactPhone, String contactEmail) {
        this.country = country;
        this.city = city;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.zipCode = zipCode;
        this.ordererName = ordererName;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.ordererId = ordererId;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getOrdererName() {
        return ordererName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getOrdererId() {
        return ordererId;
    }
}
