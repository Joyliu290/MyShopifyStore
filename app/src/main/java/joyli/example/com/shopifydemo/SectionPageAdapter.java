package joyli.example.com.shopifydemo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joyli on 2017-08-23.
 */

public class SectionPageAdapter extends FragmentPagerAdapter {

    public final List<Fragment> mFragmentList = new ArrayList<>();
    public final List<String>mFragmentTitleList = new ArrayList<>();
    private int mTabPosition;

    public void addFragment(Fragment fragment, String title){
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public SectionPageAdapter(FragmentManager fm) {
        super(fm);

    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                NewArrivalFragment home = new NewArrivalFragment();
                //return home;
                return home.newInstance();
            case 1:
                CatalogueFragment cat = new CatalogueFragment();
                return cat.newInstance();
            case 2:
                ArtistEventsFragment ArtistEventsFragment = new ArtistEventsFragment();
                Log.v("event","event");
                return ArtistEventsFragment.newInstance();
            default:
                return null;
        }
        //return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position){
       return mFragmentTitleList.get(position);
    }
    public void updateTabPosition(int tabPosition) {
        mTabPosition = tabPosition;
        notifyDataSetChanged();
    }

    public int getItemPosition(Object item) {
     return POSITION_NONE;
    }
}
