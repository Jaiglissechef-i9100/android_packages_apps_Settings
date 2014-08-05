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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.settings.beanstalk.backup.AppsAdapter;
import com.android.settings.beanstalk.backup.BackupService;
import com.android.settings.beanstalk.backup.ManageBackupsActivity;

/**
 * ListFragment that shows a list of all installed apps, with radio buttons to select.
 */
public class AppsFragment extends ListFragment {

    private AppsAdapter mAdapter;

    class CreateBackupObserver implements BackupService.CreateBackupObserver {
        @Override
        public void onCreateBackupCompleted() {
            ManageBackupsActivity activity = (ManageBackupsActivity) getActivity();
            activity.refreshList();
        }
    }

    /**
     * Compares ApplicationInfo items by localized label.
     */
    private class AppInfoComparator implements Comparator<ApplicationInfo> {
        public int compare(ApplicationInfo lhs, ApplicationInfo rhs) {
            PackageManager pm = getActivity().getPackageManager();
            String lhsLabel = (String) lhs.loadLabel(pm);
            return lhsLabel.compareTo((String) rhs.loadLabel(pm));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new AppsAdapter(getActivity());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.apps_fragment_menu, menu);
    }

    /**
     * Creates a backup of the selected app.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.backup:
            int position = getListView().getCheckedItemPosition();
            if (position == AdapterView.INVALID_POSITION) {
                Toast.makeText(getActivity(), R.string.no_apps_selected, Toast.LENGTH_SHORT)
                        .show();
            } else {
                ApplicationInfo ai = (ApplicationInfo) mAdapter.getItem(position);
                ManageBackupsActivity activity = (ManageBackupsActivity) getActivity();
                activity.getBackupService().createBackup(ai.packageName,
                        new CreateBackupObserver());
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Updates the ListView contents with alphabetically sorted app labels from
     * {@link PackageManager}.
     */
    public void refreshList() {
        ManageBackupsActivity.uncheckAll(getListView());

        PackageManager pm = getActivity().getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        List<ApplicationInfo> appInfo = new ArrayList<ApplicationInfo>(packages.size());
        for (PackageInfo p : packages) {
            appInfo.add(p.applicationInfo);
        }

        Collections.sort(appInfo, new AppInfoComparator());
        mAdapter.clear();
        mAdapter.addAll(appInfo);
        setListAdapter(mAdapter);
    }

}
