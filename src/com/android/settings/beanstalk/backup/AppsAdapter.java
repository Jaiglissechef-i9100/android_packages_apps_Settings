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
import android.content.pm.PackageManager;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.android.settings.R;

/**
 * ArrayAdapter that shows the respective array elements as text.
 */
public class AppsAdapter extends ArrayAdapter<ApplicationInfo> {

    public AppsAdapter(Context context) {
        super(context, R.layout.app_list_item);
    }

    /**
     * Returns a view with the text being the string at position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.app_list_item, parent, false);
        }

        CheckedTextView text = (CheckedTextView) convertView;
        ApplicationInfo ai = (ApplicationInfo) getItem(position);
        text.setText(ai.loadLabel(getContext().getPackageManager()));
        return convertView;
    }

}
