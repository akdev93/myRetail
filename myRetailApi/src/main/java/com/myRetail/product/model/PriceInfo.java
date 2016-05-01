package com.myRetail.product.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * <p><code>PriceInfo</code> models the data in the pricing database. Instances of this class are
 * returned by subclasses of <code>PricingDAO</code></p>
 */
@XmlRootElement
@ToString
public class PriceInfo {

    @XmlElement(name="value")
    @Getter
    @Setter
    private float price;



    @XmlElement(name="currency_code")
    @Getter
    @Setter
    private String currencyCode;

    @XmlTransient
    @Setter
    private String productId;

    public PriceInfo() {}

    public PriceInfo(String productId, float price, String currencyCode) {
        this.productId = productId;
        this.price = price;
        this.currencyCode = currencyCode;
    }

}
