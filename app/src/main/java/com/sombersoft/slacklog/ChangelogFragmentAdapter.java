package com.sombersoft.slacklog;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ChangelogFragmentAdapter extends FragmentStatePagerAdapter {

    ChangelogFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int item) {
        Bundle addBundle = new Bundle();
        switch (item) {
            case 0:
                addBundle.putString("fileName", "Changelog64Bit.txt");
                FragmentChangelog fragmentChangelog64Bit = new FragmentChangelog();
                fragmentChangelog64Bit.setArguments(addBundle);
                return fragmentChangelog64Bit;

            case 1:
                addBundle.putString("fileName", "Changelog32Bit.txt");
                FragmentChangelog fragmentChangelog32Bit = new FragmentChangelog();
                fragmentChangelog32Bit.setArguments(addBundle);
                return fragmentChangelog32Bit;

            case 2:
                addBundle.putString("fileName", "ChangelogARM.txt");
                FragmentChangelog fragmentChangelogArm = new FragmentChangelog();
                fragmentChangelogArm.setArguments(addBundle);
                return fragmentChangelogArm;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "64 Bit";

            case 1:
                return "32 Bit";

            case 2:
                return "Arm";
        }
        return null;
    }
}
