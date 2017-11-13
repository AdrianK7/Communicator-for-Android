package com.forstudy.pc.communicator.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.forstudy.pc.communicator.Models.Departments;
import com.forstudy.pc.communicator.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 14.03.17.
 */

public class DepartmentsAdapter  extends ArrayAdapter<Departments> {
    public DepartmentsAdapter(Context context, List<Departments> departments) {
        super(context, 0, departments);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Departments department = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.departments_list, parent, false);
        }
        TextView Name = (TextView) convertView.findViewById(R.id.Name);
        Name.setText(department.getName());
        return convertView;
    }
}
