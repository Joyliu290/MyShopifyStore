package joyli.example.com.shopifydemo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shopify.buy3.GraphCall;
import com.shopify.buy3.GraphClient;
import com.shopify.buy3.GraphError;
import com.shopify.buy3.GraphResponse;
import com.shopify.buy3.Storefront;
import com.shopify.graphql.support.ID;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joyli on 2017-08-24.
 */

public class NewArrivalFragment extends Fragment {
    private static final String TAG = "Tab1 Fragment";
    ImageView banner, newestAlbumImage1, newestAlbumImage2, newestAlbumImage3, newestAlbumImage4;
    TextView newestAlbumTitle1, newestAlbumTitle2, newestAlbumTitle3, newestAlbumTitle4;
    GraphClient graphClient;
    String DOMAIN = "joys-kpop-store.myshopify.com";
    String API_KEY = "8ea2534c037699904b9087085c8dda70";
    Context mContext;
    SQLDatabase myDB;

    public static NewArrivalFragment newInstance(){
        return new NewArrivalFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_page, container, false);
        setHasOptionsMenu(true);

        GridLayout layout = (GridLayout)view.findViewById(R.id.gridLayout);
        GridLayout layoutPoster = (GridLayout)view.findViewById(R.id.gridLayout2);
        List<String>individualAlbumID = new ArrayList<>();
        List<String>individualPosterID = new ArrayList<>();
        List<String>imageSrc = new ArrayList<>();
        List<String>productName = new ArrayList<>();

        graphClient=GraphClient.builder(getContext())
                .shopDomain(DOMAIN)
                .accessToken(API_KEY)
                .build();

        Storefront.QueryRootQuery query = Storefront.query(rootQuery -> rootQuery
                .shop(shopQuery -> shopQuery
                        .collections(10, collectionConnectionQuery -> collectionConnectionQuery
                                .edges(collectionEdgeQuery -> collectionEdgeQuery
                                        .node(collectionQuery -> collectionQuery
                                                .title()
                                                .products(10, productConnectionQuery -> productConnectionQuery
                                                        .edges(productEdgeQuery -> productEdgeQuery
                                                                .node(productQuery -> productQuery
                                                                        .title()
                                                                        .productType()
                                                                        .description()
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        graphClient.queryGraph(query).enqueue(new GraphCall.Callback<Storefront.QueryRoot>() {
            @Override
            public void onResponse(@NonNull GraphResponse<Storefront.QueryRoot> response) {
                List<Storefront.Collection> collections = new ArrayList<>();
                for (Storefront.CollectionEdge collectionEdge : response.data().getShop().getCollections().getEdges()) {
                    collections.add(collectionEdge.getNode());
                    List<Storefront.Product> products = new ArrayList<>();

                    for (Storefront.ProductEdge productEdge : collectionEdge.getNode().getProducts().getEdges()) {
                        products.add(productEdge.getNode());

                        if (collectionEdge.getNode().getTitle().equals("Albums")){
                            individualAlbumID.add(productEdge.getNode().getId().toString());
                            Log.v("album product name", productEdge.getNode().getTitle());
                            productName.add(productEdge.getNode().getTitle());
                        }
                        else if (collectionEdge.getNode().getTitle().equals("Poster")){
                            individualPosterID.add(productEdge.getNode().getId().toString());
                        }
                    }
                }
                int albumSize = individualAlbumID.size();
                int posterSize = individualPosterID.size();
                for (int i =0; i<albumSize;i++){
                    int finalI = i;
                    Storefront.QueryRootQuery imageQuery = Storefront.query(rootQuery -> rootQuery
                            .node(new ID(individualAlbumID.get(finalI)), nodeQuery -> nodeQuery
                                    .onProduct(productQuery -> productQuery
                                            .title()
                                            .description()
                                            .images(1, imageConnectionQuery -> imageConnectionQuery
                                                    .edges(imageEdgeQuery -> imageEdgeQuery
                                                            .node(imageNode -> imageNode
                                                                    .src())))
                                            .variants(10, variantConnectionQuery -> variantConnectionQuery
                                                    .edges(variantEdugeQuery -> variantEdugeQuery
                                                            .node(productVariantQuery -> productVariantQuery
                                                                    .price()))))));

                    graphClient.queryGraph(imageQuery).enqueue(new GraphCall.Callback<Storefront.QueryRoot>() {
                        @Override
                        public void onResponse(@NonNull GraphResponse<Storefront.QueryRoot> response) {
                            Storefront.Product imageProduct = (Storefront.Product) response.data().getNode();
                            List <Storefront.Image> productImages = new ArrayList<Storefront.Image>();
                            for (final Storefront.ImageEdge imageEdge : imageProduct.getImages().getEdges()) {
                                productImages.add(imageEdge.getNode());
                                for (final Storefront.ProductVariantEdge productPrice : imageProduct.getVariants().getEdges()) {
                                    if (finalI < 4) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                addProductTitle(imageProduct.getTitle(), imageEdge.getNode().getSrc(), productPrice.getNode().getPrice().toString(), layout);
                                                imageSrc.add(imageEdge.getNode().getSrc());
                                            }
                                        });
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull GraphError error) {
                        }
                    });
                }
                for (int i =0; i<posterSize;i++){
                    int finalI = i;
                    Storefront.QueryRootQuery imageQuery = Storefront.query(rootQuery -> rootQuery
                            .node(new ID(individualPosterID.get(finalI)), nodeQuery -> nodeQuery
                                    .onProduct(productQuery -> productQuery
                                            .title()
                                            .images(1, imageConnectionQuery -> imageConnectionQuery
                                                    .edges(imageEdgeQuery -> imageEdgeQuery
                                                            .node(imageNode -> imageNode
                                                                    .src())))
                                    .variants(10, variantConnectionQuery -> variantConnectionQuery
                                    .edges(variantEdgeQuery -> variantEdgeQuery
                                    .node (productVariantQuery -> productVariantQuery
                                    .price()))))));

                    graphClient.queryGraph(imageQuery).enqueue(new GraphCall.Callback<Storefront.QueryRoot>() {
                        @Override
                        public void onResponse(@NonNull GraphResponse<Storefront.QueryRoot> response) {
                            Storefront.Product imageProduct = (Storefront.Product) response.data().getNode();
                            List <Storefront.Image> productImages = new ArrayList<Storefront.Image>();
                            for (final Storefront.ImageEdge imageEdge : imageProduct.getImages().getEdges()) {
                                productImages.add(imageEdge.getNode());
                                for (final Storefront.ProductVariantEdge productPrice : imageProduct.getVariants().getEdges()) {
                                    if (finalI < 4) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                addProductTitle(imageProduct.getTitle(), imageEdge.getNode().getSrc(), productPrice.getNode().getPrice().toString(), layoutPoster);
                                                imageSrc.add(imageEdge.getNode().getSrc());
                                            }
                                        });
                                    }
                                }

                            }

                        }

                        @Override
                        public void onFailure(@NonNull GraphError error) {

                        }
                    });

                }
            }

            @Override
            public void onFailure(@NonNull GraphError error) {
                Log.v("Failed",error.getMessage());
            }
        });
        return view;
    }

    public void addProductTitle (String productActualName, String imageSource, String productPrice, GridLayout layout){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
        View product = getActivity().getLayoutInflater().inflate(R.layout.product,null);
        TextView productName = (TextView)product.findViewById(R.id.productTitle);
        productName.setText(productActualName);

              TextView productPriceTV = (TextView)product.findViewById(R.id.productPrice);
                productPriceTV.setText("$"+productPrice);
                ImageView img = (ImageView)product.findViewById(R.id.productImage);
                Picasso.with(getContext())
                        .load(imageSource)
                        .into(img);
                layout.addView(product);

                // if user clicks on the image of the product, it will lead to a new activity that shows details of the product
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                ImageView cart = (ImageView)product.findViewById(R.id.cart);
                cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String productActualName = productName.getText().toString();
                        String productActualPrice = productPriceTV.getText().toString();
                        String imageSRC = imageSource;
                        myDB = new SQLDatabase(getContext());
                        AddData(productActualName,productActualPrice, imageSRC);
                    }
                });
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.shoppingCart) {
            ShoppingCartFragment.newInstance();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void AddData (String productName, String productPrice, String productImage) {
        boolean insertData = myDB.addData(productName, productPrice, productImage);

        if (insertData ==true) {
            Toast.makeText(getContext(), "Item "+productName+"  successfully added to cart!", Toast.LENGTH_LONG).show();
        }
        else {

            Toast.makeText(getContext(), "Oh no something went wrong!",Toast.LENGTH_LONG).show();
        }
    }

}
