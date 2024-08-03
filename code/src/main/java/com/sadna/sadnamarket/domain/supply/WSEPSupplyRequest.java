package com.sadna.sadnamarket.domain.supply;

import org.springframework.web.reactive.function.BodyInserters;

public class WSEPSupplyRequest extends WSEPRequest{
    String name;
    String address;
    String city;
    String country;
    String zip;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public BodyInserters.FormInserter getBody() {
        return BodyInserters.fromFormData("action_type","supply")
                .with("name",name)
                .with("address",address)
                .with("city",city)
                .with("country",country)
                .with("zip",zip);
    }
}
