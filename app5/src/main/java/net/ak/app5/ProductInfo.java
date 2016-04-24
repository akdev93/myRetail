package net.ak.app5;

/**
 * Created by koneria on 4/17/16.
 */
public class ProductInfo {
    private String id;
    private String name;

    public ProductInfo() {}

    public ProductInfo(String id, String name) {
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
