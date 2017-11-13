package com.forstudy.pc.communicator.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.forstudy.pc.communicator.Models.Departments;
import com.forstudy.pc.communicator.Models.Employees;
import com.forstudy.pc.communicator.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 14.03.17.
 */

public class DepartmentContactsAdapter  extends ArrayAdapter<Employees> {
    public DepartmentContactsAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Employees employee = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.messages_list, parent, false);
        }
        TextView Name = (TextView) convertView.findViewById(R.id.Name);
        TextView Login = (TextView) convertView.findViewById(R.id.Login);
        TextView Status = (TextView) convertView.findViewById(R.id.Status);
        Name.setText(employee.getFirstName() + " " + employee.getSecondName());
        Login.setText(employee.getLogin());
        Status.setText(" ");
        return convertView;
    }
}
