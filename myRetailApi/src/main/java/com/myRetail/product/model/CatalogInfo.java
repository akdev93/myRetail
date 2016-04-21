package com.myRetail.product.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by koneria on 4/17/16.
 */

@XmlRootElement
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
}
