package com.bignerdranch.nerdlauncher;

import android.support.v4.app.Fragment;

public class MainActivity extends SingleFragmentActivity {

    @Override
    Fragment createFragment() {
        return NerdLauncherFragment.newInstance();
    }
}
