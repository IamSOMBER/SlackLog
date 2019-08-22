package com.sombersoft.slacklog;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ForumFragmentAdapter extends FragmentStatePagerAdapter {

    ForumFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int item) {

        Bundle addBundle = new Bundle();

        switch (item) {
            case 0:
                addBundle.putString("fileName", "topics64.txt");
                FragmentForum ff64 = new FragmentForum();
                ff64.setArguments(addBundle);
                return ff64;

            case 1:
                addBundle.putString("fileName", "topics32.txt");
                FragmentForum ff32 = new FragmentForum();
                ff32.setArguments(addBundle);
                return ff32;

            case 2:
                addBundle.putString("fileName", "topicsGenDisc.txt");
                FragmentForum gen = new FragmentForum();
                gen.setArguments(addBundle);
                return gen;

            case 3:
                addBundle.putString("fileName", "topicsPack.txt");
                FragmentForum pack = new FragmentForum();
                pack.setArguments(addBundle);
                return pack;

            case 4:
                addBundle.putString("fileName", "freeTopics.txt");
                FragmentForum free = new FragmentForum();
                free.setArguments(addBundle);
                return free;

            case 5:
                addBundle.putString("fileName", "topicsGnome.txt");
                FragmentForum gnome = new FragmentForum();
                gnome.setArguments(addBundle);
                return gnome;

            case 6:
                addBundle.putString("fileName", "topicsWiki.txt");
                FragmentForum wiki = new FragmentForum();
                wiki.setArguments(addBundle);
                return wiki;

            case 7:
                addBundle.putString("fileName", "topicsSecurity.txt");
                FragmentForum sec = new FragmentForum();
                sec.setArguments(addBundle);
                return sec;

            case 8:
                addBundle.putString("fileName", "topicsPorting.txt");
                FragmentForum port = new FragmentForum();
                port.setArguments(addBundle);
                return port;

            case 9:
                addBundle.putString("fileName", "topicsProgram.txt");
                FragmentForum prog = new FragmentForum();
                prog.setArguments(addBundle);
                return prog;

            case 10:
                addBundle.putString("fileName", "topicsHardware.txt");
                FragmentForum hard = new FragmentForum();
                hard.setArguments(addBundle);
                return hard;

            case 11:
                addBundle.putString("fileName", "topicsLaptop.txt");
                FragmentForum lap = new FragmentForum();
                lap.setArguments(addBundle);
                return lap;

            case 12:
                addBundle.putString("fileName", "topicsMerchand.txt");
                FragmentForum merch = new FragmentForum();
                merch.setArguments(addBundle);
                return merch;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 13;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        // Causes adapter to reload all Fragments when
        // notifyDataSetChanged is called
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Slackware64";
            case 1:
                return "Slackware32";
            case 2:
                return "Gnu/Linux in genere";
            case 3:
                return "Libera";
            case 4:
                return "Packages";
            case 5:
                return "Gnome Slacky";
            case 6:
                return "Wikislacky";
            case 7:
                return "Sicurezza";
            case 8:
                return "Porting Slackware";
            case 9:
                return "Programmazione";
            case 10:
                return "Hardware";
            case 11:
                return "Laptop";
            case 12:
                return "Mercatino";
        }
        return null;
    }
}
