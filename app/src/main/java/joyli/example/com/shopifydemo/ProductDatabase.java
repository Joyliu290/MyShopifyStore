package joyli.example.com.shopifydemo;

/**
 * Created by Joyli on 2017-09-02.
 */

public class ProductDatabase {

    String imageSource, productName;
    Double price;

    public ProductDatabase(Double price, String imageSource, String productName){
        this.price=price;
        this.imageSource=imageSource;
        this.productName=productName;
    }

}
