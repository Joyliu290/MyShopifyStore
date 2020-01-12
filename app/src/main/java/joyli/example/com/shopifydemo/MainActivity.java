package joyli.example.com.shopifydemo;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity{
ImageView banner, album, poster;
    private ViewPager viewPager;

    private SectionPageAdapter mSectionPageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSectionPageAdapter = new SectionPageAdapter(getSupportFragmentManager());
        viewPager = (ViewPager)findViewById(R.id.pager);
        setUpViewPager(viewPager);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        mSectionPageAdapter.notifyDataSetChanged();
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSectionPageAdapter.updateTabPosition(position);
                if (position==0){

                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

        private void setUpViewPager (ViewPager viewPager) {
        SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new NewArrivalFragment(),"New Arrival");
        adapter.addFragment(new CatalogueFragment(), "Catalogue");
            adapter.addFragment(new ArtistEventsFragment(), "Events");

        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shopping_cart, menu);
        //setIconInMenu(menu, R.id.action_home, R.string.action_home, R.mipmap.home);
        //setIconInMenu(menu, R.id.action_database, R.string.action_database, R.mipmap.diskette);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.shoppingCart) {
            Log.v("clickng menu","click");
            Intent i = new Intent(MainActivity.this,StartShoppingCart.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
