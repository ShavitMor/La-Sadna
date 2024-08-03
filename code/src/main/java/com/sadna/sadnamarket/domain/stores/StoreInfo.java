package com.sadna.sadnamarket.domain.stores;

import com.sadna.sadnamarket.service.Error;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class StoreInfo {
    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_rank")
    private Double storeRank;

    @Column(name = "store_address")
    private String address;

    @Column(name = "store_email")
    private String email;

    @Column(name = "store_phone_number")
    private String phoneNumber;

    public StoreInfo(String storeName, String address, String email, String phoneNumber) {
        verify(storeName, address, email, phoneNumber);
        this.storeName = storeName;
        this.storeRank = 3.0;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public StoreInfo() {}

    public StoreInfo(StoreDTO storeDTO) {
        this.storeName = storeDTO.getStoreName();
        this.storeRank = storeDTO.getRank();
        this.address = storeDTO.getAddress();
        this.email = storeDTO.getEmail();
        this.phoneNumber = storeDTO.getPhoneNumber();
    }

    private void verify(String storeName, String address, String email, String phoneNumber) {
        if(storeName == null || storeName.trim().equals(""))
            throw new IllegalArgumentException(Error.makeStoreNotValidAspectError(storeName, "store name"));
        if(address == null || address.trim().equals(""))
            throw new IllegalArgumentException(Error.makeStoreNotValidAspectError(address, "address"));
        if(email == null || !email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"))
            throw new IllegalArgumentException(Error.makeStoreNotValidAspectError(email, "email address"));
        if(phoneNumber == null || phoneNumber.matches("^\\d{9}$"))
            throw new IllegalArgumentException(Error.makeStoreNotValidAspectError(phoneNumber, "phone number"));
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public double getRank() {
        return storeRank;
    }

    public void setRank(double rank) {
        this.storeRank = rank;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreInfo storeInfo = (StoreInfo) o;
        return Double.compare(storeInfo.storeRank, storeRank) == 0 && Objects.equals(storeName, storeInfo.storeName) && Objects.equals(address, storeInfo.address) && Objects.equals(email, storeInfo.email) && Objects.equals(phoneNumber, storeInfo.phoneNumber);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(storeName, storeRank, address, email, phoneNumber);
        return result;
    }
}
