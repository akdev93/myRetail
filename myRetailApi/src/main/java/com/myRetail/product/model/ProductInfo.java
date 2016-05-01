package com.myRetail.product.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p><code>ProductInfo</code></p> models the aggregated view of the product which includes
 * the catalog information and the pricing information.
 */
@XmlRootElement
@Data
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

}
