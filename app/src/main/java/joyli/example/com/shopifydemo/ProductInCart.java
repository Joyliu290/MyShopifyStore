package joyli.example.com.shopifydemo;

/**
 * Created by Joyli on 2017-09-05.
 */

public class ProductInCart {

    private String productName, productPrice, productImageSource;

    public ProductInCart(String productName, String productPrice, String productImageSource){
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImageSource = productImageSource;
    }

    public String getProductImageSource() {
        return productImageSource;
    }

    public void setProductImageSource(String productImageSource) {
        this.productImageSource = productImageSource;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }
}
