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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;

import com.android.settings.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.android.settings.beanstalk.backup.Backup;
import com.android.settings.beanstalk.backup.BackupService;

/**
 * ListAdapter that provides apps and backups so that backups are grouped by app,
 * and uninstalled apps are listed seperately.
 */
public class BackupsAdapter extends BaseAdapter
        implements ListAdapter, BackupService.ListBackupsObserver {

    public static final int VIEW_TYPE_APP = 0;

    public static final int VIEW_TYPE_BACKUP = 1;

    private int mDefaultTextColor;

    /**
     * Class representing an app and all its backups.
     *
     * Note: Access by id is O(n), as each element has to be checked for the number
     * of backups. As an improvement, a map from id to App could be used.
     */
    public class App implements Comparable<App> {

        public String label;

        public String packageName;

        public boolean isInstalled;

        public ArrayList<Backup> backups = new ArrayList<Backup>();

        @Override
        public int compareTo(App other) {
            if (other == this)
                return 0;
            else if (this.isInstalled && !other.isInstalled)
                return -1;
            else if (!this.isInstalled && other.isInstalled)
                return 1;
            else
                return this.label.compareTo(other.label);
        }

    }

    private Context mContext;

    private ArrayList<App> mItems = new ArrayList<App>();

    public BackupsAdapter(Context context) {
        mContext = context;
    }

    /**
     * Populates {@link mItems} with all installed apps and their backups, and, for
     * backups of uninstalled apps, these apps with their backups.
     */
    @Override
    public void onListBackupsCompleted(final Map<String, List<Backup>> backups) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PackageManager pm = mContext.getPackageManager();
                List<PackageInfo> packages = pm.getInstalledPackages(0);
                ArrayList<String> packageNames = new ArrayList<String>();
                for (PackageInfo p : packages) {
                    packageNames.add(p.packageName);
                }

                mItems.clear();
                for (Map.Entry<String, List<Backup>> entry: backups.entrySet()) {
                    App a = new App();
                    a.packageName = entry.getKey();
                    a.isInstalled = packageNames.contains(a.packageName);
                    // Prefer data from PackageManager over backup metadata (which may be missing).
                    if (a.isInstalled) {
                        try {
                            PackageInfo pInfo = pm.getPackageInfo(a.packageName, 0);
                            a.label = (String) pm.getApplicationLabel(pInfo.applicationInfo);
                        } catch (PackageManager.NameNotFoundException e) {
                            // This should only happen if an app is uninstalled during this method,
                            // fall back on metadata then.
                            a.label = entry.getValue().get(0).label;
                        }
                    } else {
                        a.label = entry.getValue().get(0).label;
                    }

                    for (Backup b : entry.getValue()) {
                        a.backups.add(b);
                    }
                    mItems.add(a);
                }

                Collections.sort(mItems);
                notifyDataSetChanged();
            }
        }).run();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * Returns different values for apps and backups.
     */
    @Override
    public int getItemViewType(int position) {
        return (getItem(position) instanceof App)
                ? VIEW_TYPE_APP
                : VIEW_TYPE_BACKUP;
    }

    /**
     * For apps, returns an uncheckable list item showing , for backups, returns a checkable,
     * indented list item showing backup data and version.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int layout = (getItemViewType(position) == VIEW_TYPE_APP)
                    ? R.layout.app_list_item
                    : R.layout.backup_list_item;
            convertView = inflater.inflate(layout, parent, false);
        }

        CheckedTextView text = (CheckedTextView) convertView;
        text.setTypeface(null, Typeface.NORMAL);
        if (getItemViewType(position) == VIEW_TYPE_APP) {
            App a = (App) getItem(position);
            text.setText(a.label);
            text.setCheckMarkDrawable(0);
            if (!a.isInstalled) {
                text.setTypeface(null, Typeface.ITALIC);
            }
        } else {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
            Backup b = (Backup) getItem(position);
            text.setText(df.format(b.date) + " - " +
                    mContext.getString(R.string.version_text, b.versionName));
        }
        return convertView;
    }

    /**
     * Returns true, so that seperators are drawn between all items.
     */
    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    /**
     * Returns true for all installed apps and all backups, false for uninstalled apps.
     */
    public boolean isEnabled(int position) {
        return getItemViewType(position) == VIEW_TYPE_BACKUP;
    }

    /**
     * Returns the number of apps and backups combined.
     */
    @Override
    public int getCount() {
        int count = 0;
        for (App a : mItems) {
            count += 1 + a.backups.size();
        }
        return count;
    }

    /**
     * Returns the {@link App}or {@link Backup} represented by the specified item.
     */
    @Override
    public Object getItem(int position) {
        int current = 0;
        for (App a : mItems) {
            if (current == position) {
                return a;
            }
            current += 1;
            if (position - current < a.backups.size()) {
                return a.backups.get(position - current);
            }
            current += a.backups.size();
        }
        return null;
    }

    /**
     * Returns position again.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * IDs are generated by position on the fly, so not stable.
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

}
