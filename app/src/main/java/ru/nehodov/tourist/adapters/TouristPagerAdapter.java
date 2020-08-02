package ru.nehodov.tourist.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.nehodov.tourist.LocationListFragment;
import ru.nehodov.tourist.MapFragment;
import ru.nehodov.tourist.RouteListFragment;

public class TouristPagerAdapter extends FragmentStateAdapter {

    private static final int FIRST_TAB = 0;
    private static final int SECOND_TAB = 1;
    private static final int THIRD_TAB = 2;

    private final int[] tabs;

    public TouristPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.tabs = new int[]{FIRST_TAB, SECOND_TAB, THIRD_TAB};
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (tabs[position]) {
            case FIRST_TAB:
                return MapFragment.newInstance();
            case SECOND_TAB:
                return LocationListFragment.newInstance();
            case THIRD_TAB:
                return RouteListFragment.newInstance();
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return tabs.length;
    }
}
