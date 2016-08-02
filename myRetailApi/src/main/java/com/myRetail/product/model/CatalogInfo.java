package com.myRetail.product.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p><code>CatalogInfo</code> models the data returned by a catalog service.
 * </p>
 */
@XmlRootElement
@Data
public class CatalogInfo {

    @XmlElement
    private String id;

    @XmlElement
    private String name;

    public CatalogInfo(){}

    public CatalogInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
