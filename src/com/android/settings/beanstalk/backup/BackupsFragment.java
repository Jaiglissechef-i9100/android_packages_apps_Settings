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
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.android.settings.beanstalk.backup.BackupService;

/**
 * ListFragment that shows a list of all backups, grouped by app.
 */
public class BackupsFragment extends ListFragment {

    private BackupsAdapter mAdapter;

    class RestoreBackupObserver implements BackupService.RestoreBackupObserver {
        @Override
        public void onRestoreBackupCompleted() {
            ManageBackupsActivity activity = (ManageBackupsActivity) getActivity();
            activity.refreshList();
        }
    }

    class DeleteBackupObserver implements BackupService.DeleteBackupObserver {
        @Override
        public void onDeleteBackupCompleted() {
            ManageBackupsActivity activity = (ManageBackupsActivity) getActivity();
            activity.refreshList();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new BackupsAdapter(getActivity());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setHasOptionsMenu(true);
        setEmptyText(getResources().getString(R.string.no_backups_found));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.backups_fragment_menu, menu);
    }

    /**
     * Restores or deletes the selected backup.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ManageBackupsActivity activity = (ManageBackupsActivity) getActivity();
        switch (item.getItemId()) {
        case R.id.restore:
            int position = getListView().getCheckedItemPosition();
            if (position == AdapterView.INVALID_POSITION) {
                Toast.makeText(getActivity(), R.string.no_backups_selected, Toast.LENGTH_SHORT).show();
            } else {
                Backup b = (Backup) mAdapter.getItem(position);
                activity.getBackupService().restoreBackup(b, null);
            }
            return true;
        case  R.id.delete:
            position = getListView().getCheckedItemPosition();
            if (position == AdapterView.INVALID_POSITION) {
                Toast.makeText(getActivity(), R.string.no_backups_selected, Toast.LENGTH_SHORT).show();
            } else {
                Backup b = (Backup) mAdapter.getItem(position);
                activity.getBackupService().deleteBackup(b, new DeleteBackupObserver());
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Updates the adapter from {@link BackupService#listBackups}.
     */
    public void refreshList() {
        ManageBackupsActivity.uncheckAll(getListView());

        ManageBackupsActivity activity = (ManageBackupsActivity) getActivity();
        activity.getBackupService().listBackups(null, mAdapter);
        setListAdapter(mAdapter);
    }

}
