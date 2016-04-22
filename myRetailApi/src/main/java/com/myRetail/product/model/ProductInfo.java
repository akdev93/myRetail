package com.myRetail.product.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ProductInfo {
    @XmlElement
    private String id;

    @XmlElement
    private String name;

    @XmlElement(name="current_price")
    private PriceInfo priceInfo;

    public ProductInfo() {

    }

    public ProductInfo(String id, String name, PriceInfo priceInfo){
        this.id = id;
        this.name = name;
        this.priceInfo = priceInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PriceInfo getPriceInfo() {
        return priceInfo;
    }

    public void setPriceInfo(PriceInfo priceInfo) {
        this.priceInfo = priceInfo;
    }
}
