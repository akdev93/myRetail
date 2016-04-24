package com.myRetail.product.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@XmlRootElement
public class PriceInfo {

    @XmlElement(name="value")
    private float price;



    @XmlElement(name="currency_code")
    private String currencyCode;

    private String productId;

    public PriceInfo() {}

    public PriceInfo(String productId, float price, String currencyCode) {
        this.productId = productId;
        this.price = price;
        this.currencyCode = currencyCode;
    }

    @XmlTransient
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public String toString() {
        return "PriceInfo{" +
                "price=" + price +
                ", currencyCode='" + currencyCode + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}
