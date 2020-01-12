package joyli.example.com.shopifydemo;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Joyli on 2017-08-24.
 */

public class ShoppingCartFragment extends Fragment {

    SQLDatabase myDB;
    ArrayList<ProductInCart> checkOutList;
    ProductInCart ProductInCart;

    public static ShoppingCartFragment newInstance() {
        return new ShoppingCartFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cart_fragment, container, false);
        GridLayout layout = (GridLayout)view.findViewById(R.id.gridLayoutCart);

        myDB = new SQLDatabase(getContext());
        checkOutList = new ArrayList<>();
        Cursor data = myDB.getListContents();
        Cursor totalPrice = myDB.total();
        int numRows = data.getCount();

        if (numRows==0){
            Toast.makeText(getContext(), "You have no item in bag!", Toast.LENGTH_LONG).show();
        }
        else {
            int i =0;
            Double itemPrice=0.0;
            Double total=0.0;

            while (data.moveToNext()){
                ProductInCart = new ProductInCart(data.getString(0), data.getString(1), data.getString(2));
                Log.v("data",data.getString(0));
                itemPrice = Double.parseDouble(data.getString(1).substring(data.getString(1).lastIndexOf("$")+1));
                total = total+itemPrice;

                displayItem(data.getString(0), data.getString(1), data.getString(2), layout);
                i++;
            }
            Log.v("total", Double.toString(total));
            TextView estimatedTotal = (TextView)view.findViewById(R.id.cartEstimatedTotal);
            estimatedTotal.setText("Estimated Subtotal ("+Integer.toString(i)+"): ");
            TextView estimatedTotalPrice = (TextView)view.findViewById(R.id.cartEstimatedTotalPrice);
            estimatedTotalPrice.setText("$"+Double.toString(Math.round(total*100.0)/100.0)+" CAD");
        }
        return view;
    }

    public void displayItem (String productName, String productPrice, String productImageSource, GridLayout layout){
        getActivity().runOnUiThread(() -> {
                View product = getActivity().getLayoutInflater().inflate(R.layout.item_in_cart,null);
                TextView productNameTV = (TextView)product.findViewById(R.id.individualCartItemName);
                TextView productPriceTV = (TextView)product.findViewById(R.id.individualCartItemPrice);
                TextView productStock = (TextView)product.findViewById(R.id.individualCartStock);
                Button deleteButton = (Button)product.findViewById(R.id.individualCartButton);
                productStock.setText("In Stock");
                ImageView productImage = (ImageView)product.findViewById(R.id.individualCartItemImage);

                productNameTV.setText(productName);
                productNameTV.setTypeface(null, Typeface.BOLD);
                productPriceTV.setText(productPrice);

                Picasso.with(getContext())
                        .load(productImageSource)
                        .into(productImage);

                layout.addView(product);

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       myDB.deleteCartItem(productName,productPrice, productImageSource);
                        Toast.makeText(getContext(), "Deleting "+productName+" from your cart!", Toast.LENGTH_LONG).show();
                    }
                });
            });
    }
}
