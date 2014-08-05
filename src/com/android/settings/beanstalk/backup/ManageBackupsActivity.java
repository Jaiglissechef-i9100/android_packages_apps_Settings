/*
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.android.settings.beanstalk.backup;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.settings.R;

import com.android.settings.beanstalk.backup.AppsFragment;
import com.android.settings.beanstalk.backup.BackupsFragment;
import com.android.settings.beanstalk.backup.BackupService;

/**
 * Tabbed activity that shows one tab of apps and one of backups, with options to create,
 * restore and delete backups.
 */
public class ManageBackupsActivity extends FragmentActivity implements ActionBar.TabListener {

    private static final String TAG = "ManageBackupsActivity";

    private static final String TAB_APPS = "apps";

    private static final String TAB_BACKUPS = "backups";

    private BackupService mBackupService;

    private ServiceConnection mBackupServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            mBackupService = ((BackupService.BackupServiceBinder) service).getService();
            refreshList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBackupService = null;
        }
    };

    private TabsAdapter mTabsAdapter;

    private ViewPager mViewPager;

    private AppsFragment mAppsFragment;

    private BackupsFragment mBackupsFragment;

    /**
     * Checks if user is device owner, binds BackupService, and initializes tabs and fragments.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
            Log.w(TAG, "Finishing activity as user is not device owner.");
            finish();
            return;
        }

        bindServiceAsUser(new Intent(this, BackupService.class),
                mBackupServiceConnection, Context.BIND_AUTO_CREATE,
                new UserHandle(UserHandle.myUserId()));

        setContentView(R.layout.manage_backups_activity);

        mTabsAdapter = new TabsAdapter(getSupportFragmentManager());

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        actionBar.addTab(actionBar.newTab()
                        .setText(R.string.apps_title)
                        .setTabListener(this));
        actionBar.addTab(actionBar.newTab()
                    .setText(R.string.backups_title)
                    .setTabListener(this));

        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState != null) {
            mAppsFragment = (AppsFragment) fm.getFragment(
                    savedInstanceState, AppsFragment.class.getName());
            mBackupsFragment = (BackupsFragment) fm.getFragment(
                    savedInstanceState, BackupsFragment.class.getName());
        } else {
            mAppsFragment = new AppsFragment();
            mBackupsFragment = new BackupsFragment();
        }
    }


    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private class TabsAdapter extends FragmentPagerAdapter {

        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0: return mAppsFragment;
                case 1: return mBackupsFragment;
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    /**
     * Finishes activity if 'back' was selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Unbinds the service.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mBackupServiceConnection);
    }

    /**
     * Saves the state of both fragments.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        FragmentManager fm = getSupportFragmentManager();
        fm.putFragment(outState, AppsFragment.class.getName(), mAppsFragment);
        fm.putFragment(outState, BackupsFragment.class.getName(), mBackupsFragment);
    }

    /**
     * Returns the BackupService bound to this Acitivty.
     */
    public BackupService getBackupService() {
        return mBackupService;
    }

    /**
     * Forces a reload of the list items in both fragments.
     */
    public void refreshList() {
        mAppsFragment.refreshList();
        mBackupsFragment.refreshList();
    }

    /**
     * Unchecks all items in the ListView, does nothing if the adapter is null.
     */
    public static void uncheckAll(ListView lv) {
        if (lv.getAdapter() != null) {
            for (int i = 0; i < lv.getAdapter().getCount(); i++) {
                lv.setItemChecked(i, false);
            }
        }
    }

}
