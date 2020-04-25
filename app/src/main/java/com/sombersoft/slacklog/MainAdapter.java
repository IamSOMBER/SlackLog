package com.sombersoft.slacklog;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class MainAdapter extends FragmentStatePagerAdapter {
    public MainAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public Fragment getItem(int item) {
        if (item == 0) {
            return new SlackwareWebViewFragment();
        }
        return new AlienWebViewFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        /*
        Returning POSITION_NONE fixes update by instead saying,
        "This object is no longer an item I'm displaying, remove it."
        So it has the effect of removing and recreating every single item in your adapter
         */
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "GNU/Linux";
            case 1:
                return "AlienBob";
        }
        return null;
    }
}
