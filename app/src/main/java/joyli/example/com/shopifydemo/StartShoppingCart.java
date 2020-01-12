package joyli.example.com.shopifydemo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Joyli on 2017-09-12.
 */

public class StartShoppingCart extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new ShoppingCartFragment()).commit();}
    }
}
