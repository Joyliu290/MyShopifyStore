package joyli.example.com.shopifydemo;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
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
import java.util.Collections;
import java.util.List;

/**
 * Created by Joyli on 2017-08-24.
 */

public class CatalogueFragment extends Fragment {
    GraphClient graphClient;
    String DOMAIN = "joys-kpop-store.myshopify.com";
    String API_KEY = "8ea2534c037699904b9087085c8dda70";
    ProgressBar layoutProgress;
    SQLDatabase myDB;

    public static CatalogueFragment newInstance () {
        return new CatalogueFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.catalogue_fragment, container, false);
        GridLayout gridLayoutCat = (GridLayout)view.findViewById(R.id.gridLayoutCatalogue);
        List <String> prices = new ArrayList<>();
        List <String> imageURL = new ArrayList<>();
        List <String> productTitleFinal = new ArrayList<>();
        ArrayList<ProductDatabase> ProductDatabase =new ArrayList<joyli.example.com.shopifydemo.ProductDatabase>();
        layoutProgress=(ProgressBar)view.findViewById(R.id.layoutProgress);
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                view.findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                ((MenuItem item) -> {
                        switch (item.getItemId()) {
                            case R.id.browse:
                                Log.v("browse","browse");
                                layoutProgress.setVisibility(View.VISIBLE);
                                gridLayoutCat.removeAllViews();
                                prices.clear();
                                ProductDatabase.clear();
                                imageURL.clear();
                                productTitleFinal.clear();
                                graphClient=GraphClient.builder(getContext())
                                        .shopDomain(DOMAIN)
                                        .accessToken(API_KEY)
                                        .build();

                                List<String>individualAlbumID = new ArrayList<>();

                                List<String>imageSrc = new ArrayList<>();
                                List<String>productName = new ArrayList<>();

                                Storefront.QueryRootQuery query = Storefront.query(rootQuery -> rootQuery
                                        .shop(shopQuery -> shopQuery
                                                .collections(10, collectionConnectionQuery -> collectionConnectionQuery
                                                        .edges(collectionEdgeQuery -> collectionEdgeQuery
                                                                .node(collectionQuery -> collectionQuery
                                                                        .title()
                                                                        .products(100, productConnectionQuery -> productConnectionQuery
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

                                                if (collectionEdge.getNode().getTitle().equals("Albums")||collectionEdge.getNode().getTitle().equals("Poster")){
                                                    individualAlbumID.add(productEdge.getNode().getId().toString());
                                                    Log.v("album product name", productEdge.getNode().getTitle());
                                                    productName.add(productEdge.getNode().getTitle());
                                                }

                                            }
                                        }
                                        int albumSize = individualAlbumID.size();
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
                                                            getActivity().runOnUiThread(() -> {
                                                                    addProductTitle(imageProduct.getTitle(), imageEdge.getNode().getSrc(), productPrice.getNode().getPrice().toString(), gridLayoutCat);
                                                                    prices.add(productPrice.getNode().getPrice().toString());
                                                                    ProductDatabase.add(new ProductDatabase(productPrice.getNode().getPrice().doubleValue(), imageEdge.getNode().getSrc(), imageProduct.getTitle()));
                                                                    layoutProgress.setVisibility(View.INVISIBLE);
                                                                    imageSrc.add(imageEdge.getNode().getSrc());
                                                                });
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onFailure(@NonNull GraphError error) {
                                                    Log.v("Failed",error.getMessage());
                                                }
                                            });

                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull GraphError error) {
                                        Log.v("Failed",error.getMessage());
                                    }
                                });

                                break;
                            case R.id.filter:
                                Log.v("filter","filter");
                                Dialog dialog = new Dialog(getContext());
                                dialog.setContentView(R.layout.filter_dialogue);
                                dialog.setCancelable(true);
                                TextView title = (TextView)dialog.findViewById(R.id.filterDialogueTitle);
                                title.setTypeface(null, Typeface.BOLD);

                                RadioButton filterAlbum = (RadioButton) dialog.findViewById(R.id.filterAlbumRadio);
                                RadioButton filterPoster = (RadioButton) dialog.findViewById(R.id.filterPosterRadio);
                                RadioButton filterName = (RadioButton)dialog.findViewById(R.id.filterByArtistName);

                                filterAlbum.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        gridLayoutCat.removeAllViews();
                                        ProductDatabase.clear();
                                        prices.clear();
                                        graphClient=GraphClient.builder(getContext())
                                                .shopDomain(DOMAIN)
                                                .accessToken(API_KEY)
                                                .build();

                                        List<String>individualAlbumID = new ArrayList<>();
                                        List<String>individualPosterID = new ArrayList<>();

                                        List<String>imageSrc = new ArrayList<>();
                                        List<String>productName = new ArrayList<>();

                                        Storefront.QueryRootQuery query = Storefront.query(rootQuery -> rootQuery
                                                .shop(shopQuery -> shopQuery
                                                        .collections(10, collectionConnectionQuery -> collectionConnectionQuery
                                                                .edges(collectionEdgeQuery -> collectionEdgeQuery
                                                                        .node(collectionQuery -> collectionQuery
                                                                                .title()
                                                                                .products(100, productConnectionQuery -> productConnectionQuery
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

                                                                    getActivity().runOnUiThread(()-> {
                                                                            addProductTitle(imageProduct.getTitle(), imageEdge.getNode().getSrc(), productPrice.getNode().getPrice().toString(), gridLayoutCat);
                                                                            layoutProgress.setVisibility(View.INVISIBLE);
                                                                            prices.add(productPrice.getNode().getPrice().toString());
                                                                            ProductDatabase.add(new ProductDatabase(productPrice.getNode().getPrice().doubleValue(), imageEdge.getNode().getSrc(), imageProduct.getTitle()));
                                                                            imageSrc.add(imageEdge.getNode().getSrc());
                                                                            dialog.dismiss();
                                                                        });
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(@NonNull GraphError error) {
                                                            Log.v("Failed",error.getMessage());
                                                        }
                                                    });
                                                }
                                            }
                                            @Override
                                            public void onFailure(@NonNull GraphError error) {
                                                Log.v("Failed",error.getMessage());
                                            }
                                        });

                                    }
                                });

                                filterPoster.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Log.v("poster","poster");
                                        gridLayoutCat.removeAllViews();
                                        ProductDatabase.clear();
                                        prices.clear();
                                        graphClient=GraphClient.builder(getContext())
                                                .shopDomain(DOMAIN)
                                                .accessToken(API_KEY)
                                                .build();

                                        List<String>individualAlbumID = new ArrayList<>();
                                        List<String>individualPosterID = new ArrayList<>();

                                        List<String>imageSrc = new ArrayList<>();
                                        List<String>productName = new ArrayList<>();

                                        Storefront.QueryRootQuery query = Storefront.query(rootQuery -> rootQuery
                                                .shop(shopQuery -> shopQuery
                                                        .collections(10, collectionConnectionQuery -> collectionConnectionQuery
                                                                .edges(collectionEdgeQuery -> collectionEdgeQuery
                                                                        .node(collectionQuery -> collectionQuery
                                                                                .title()
                                                                                .products(100, productConnectionQuery -> productConnectionQuery
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

                                                        if (collectionEdge.getNode().getTitle().equals("Poster")){
                                                            individualAlbumID.add(productEdge.getNode().getId().toString());
                                                            Log.v("album product name", productEdge.getNode().getTitle());
                                                            productName.add(productEdge.getNode().getTitle());
                                                        }

                                                    }
                                                }
                                                int albumSize = individualAlbumID.size();
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
                                                                    Log.v("price", productPrice.getNode().getPrice().toString());
                                                                    Log.v("image title", imageProduct.getTitle());
                                                                    Log.v("image cat", imageEdge.getNode().getSrc());

                                                                    getActivity().runOnUiThread(()->{
                                                                            addProductTitle(imageProduct.getTitle(), imageEdge.getNode().getSrc(), productPrice.getNode().getPrice().toString(), gridLayoutCat);
                                                                            layoutProgress.setVisibility(View.INVISIBLE);
                                                                            prices.add(productPrice.getNode().getPrice().toString());
                                                                            imageSrc.add(imageEdge.getNode().getSrc());
                                                                            ProductDatabase.add(new ProductDatabase(productPrice.getNode().getPrice().doubleValue(), imageEdge.getNode().getSrc(), imageProduct.getTitle()));
                                                                            dialog.dismiss();
                                                                        });
                                                                }
                                                            }
                                                        }
                                                        @Override
                                                        public void onFailure(@NonNull GraphError error) {
                                                            Log.v("Failed",error.getMessage());
                                                        }
                                                    });

                                                }
                                            }

                                            @Override
                                            public void onFailure(@NonNull GraphError error) {
                                                Log.v("Failed",error.getMessage());
                                            }
                                        });
                                    }
                                });

                                filterName.setOnClickListener((View currentView)-> {
                                        dialog.dismiss();
                                        Dialog dialog2 = new Dialog(getContext());
                                        dialog2.setContentView(R.layout.artist_name);
                                        dialog2.setCancelable(true);
                                        dialog2.show();
                                        prices.clear();
                                        ProductDatabase.clear();
                                        EditText name = (EditText)dialog2.findViewById(R.id.enterName);
                                        Button enter = (Button)dialog2.findViewById(R.id.nameButton);

                                            enter.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    String artistName = name.getText().toString();
                                                    Log.v("artist name", artistName);
                                                    gridLayoutCat.removeAllViews();
                                                    graphClient=GraphClient.builder(getContext())
                                                    .shopDomain(DOMAIN)
                                                    .accessToken(API_KEY)
                                                    .build();

                                            List<String>individualAlbumID = new ArrayList<>();

                                            List<String>imageSrc = new ArrayList<>();
                                            List<String>productName = new ArrayList<>();

                                            Storefront.QueryRootQuery query = Storefront.query(rootQuery -> rootQuery
                                                    .shop(shopQuery -> shopQuery
                                                            .collections(10, collectionConnectionQuery -> collectionConnectionQuery
                                                                    .edges(collectionEdgeQuery -> collectionEdgeQuery
                                                                            .node(collectionQuery -> collectionQuery
                                                                                    .title()
                                                                                    .products(100, productConnectionQuery -> productConnectionQuery
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

                                                            if (collectionEdge.getNode().getTitle().equals("Poster")||collectionEdge.getNode().getTitle().equals("Albums")) {

                                                                if (productEdge.getNode().getTitle().toLowerCase().indexOf(artistName.toLowerCase())!=-1){
                                                                    individualAlbumID.add(productEdge.getNode().getId().toString());
                                                                    Log.v("album product name", productEdge.getNode().getTitle());
                                                                    String albumID = productEdge.getNode().getId().toString();
                                                                    productName.add(productEdge.getNode().getTitle());

                                                                        Storefront.QueryRootQuery imageQuery = Storefront.query(rootQuery -> rootQuery
                                                                                .node(new ID(albumID), nodeQuery -> nodeQuery
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
                                                                                List<Storefront.Image> productImages = new ArrayList<Storefront.Image>();
                                                                                for (final Storefront.ImageEdge imageEdge : imageProduct.getImages().getEdges()) {
                                                                                    productImages.add(imageEdge.getNode());
                                                                                    for (final Storefront.ProductVariantEdge productPrice : imageProduct.getVariants().getEdges()) {

                                                                                        getActivity().runOnUiThread(()-> {

                                                                                                addProductTitle(imageProduct.getTitle(), imageEdge.getNode().getSrc(), productPrice.getNode().getPrice().toString(), gridLayoutCat);

                                                                                                prices.add(productPrice.getNode().getPrice().toString());
                                                                                                layoutProgress.setVisibility(View.INVISIBLE);
                                                                                                ProductDatabase.add(new ProductDatabase(productPrice.getNode().getPrice().doubleValue(), imageEdge.getNode().getSrc(), imageProduct.getTitle()));
                                                                                                imageSrc.add(imageEdge.getNode().getSrc());
                                                                                                dialog2.dismiss();
                                                                                            });
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onFailure(@NonNull GraphError error) {
                                                                                Log.v("Failed",error.getMessage());
                                                                            }
                                                                        });
                                                                }

                                                                else {
                                                                    Log.v("cannot find name","cannot find");

                                                                }
                                                            }

                                                        }
                                                    }

                                                }
                                                @Override
                                                public void onFailure(@NonNull GraphError error) {
                                                    Log.v("Failed", error.getMessage());
                                                }
                                            });
                                                }
                                            });

                                    });

                                dialog.show();
                                break;
                            case R.id.sort:
                                Log.v("sort","sort");
                                //gridLayoutCat.removeAllViews();

                                Dialog dialogSort = new Dialog(getContext());
                                dialogSort.setContentView(R.layout.sort_dialogue);
                                dialogSort.setCancelable(true);
                                dialogSort.show();
                                TextView sortTitle = (TextView)dialogSort.findViewById(R.id.sortDialogueTitle);
                                sortTitle.setTypeface(null, Typeface.BOLD);

                                RadioButton sortHighToLowPrice = (RadioButton) dialogSort.findViewById(R.id.sortHighToLowPrice);
                                RadioButton sortLowToHighPrice = (RadioButton) dialogSort.findViewById(R.id.sortLowToHighPrice);
                                RadioButton sortAlphabet = (RadioButton)dialogSort.findViewById(R.id.sortAlphabet);

                                sortLowToHighPrice.setOnClickListener((View currentView) -> {
                                        gridLayoutCat.removeAllViews();
                                        Collections.sort(ProductDatabase, (ProductDatabase firstProduct, ProductDatabase secondProduct) -> {
                                            double delta = firstProduct.price - secondProduct.price;
                                            if(delta > 0.00001) return 1;
                                            if(delta < -0.00001) return -1;
                                            return 0;
                                        });

                                        layoutProgress.setVisibility(View.INVISIBLE);
                                        for (int i =0; i<ProductDatabase.size(); i++){
                                            addProductTitle(ProductDatabase.get(i).productName,ProductDatabase.get(i).imageSource, ProductDatabase.get(i).price.toString(), gridLayoutCat);
                                            Log.v("sorting","sorting");
                                        }
                                        dialogSort.dismiss();

                                    });

                                sortHighToLowPrice.setOnClickListener((View currentView)-> {
                                        gridLayoutCat.removeAllViews();
                                        Collections.sort(ProductDatabase, Collections.reverseOrder((ProductDatabase firstProduct, ProductDatabase secondProduct) -> {
                                            double delta = firstProduct.price - secondProduct.price;
                                            if(delta > 0.00001) return 1;
                                            if(delta < -0.00001) return -1;
                                            return 0;
                                        }));
                                        layoutProgress.setVisibility(View.INVISIBLE);
                                        for (int i =0; i<ProductDatabase.size(); i++){
                                            addProductTitle(ProductDatabase.get(i).productName,ProductDatabase.get(i).imageSource, ProductDatabase.get(i).price.toString(), gridLayoutCat);
                                        }
                                        dialogSort.dismiss();
                                    });

                                sortAlphabet.setOnClickListener((View currentView) -> {
                                        gridLayoutCat.removeAllViews();
                                        Collections.sort(ProductDatabase, (ProductDatabase name1, ProductDatabase name2) -> name1.productName.compareTo(name2.productName));
                                        layoutProgress.setVisibility(View.INVISIBLE);
                                        for (int i =0; i<ProductDatabase.size(); i++){
                                            addProductTitle(ProductDatabase.get(i).productName,ProductDatabase.get(i).imageSource, ProductDatabase.get(i).price.toString(), gridLayoutCat);
                                        }
                                        dialogSort.dismiss();
                                    });
                                break;
                        }
                        return true;
                    });

        return view;
    }

    public void addProductTitle (String productActualName, String imageSource, String productPrice, GridLayout layout){
        getActivity().runOnUiThread(() -> {
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

                ImageView cart = (ImageView)product.findViewById(R.id.cart);
                cart.setOnClickListener((View currentView) -> {
                        String nameOfProduct = productName.getText().toString();
                        String priceOfProduct = productPriceTV.getText().toString();
                        String imageSRC = imageSource;
                        myDB = new SQLDatabase(getContext());
                        AddData(nameOfProduct,priceOfProduct, imageSRC);
                    });
            });
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
