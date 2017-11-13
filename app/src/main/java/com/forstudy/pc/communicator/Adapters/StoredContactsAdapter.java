package com.forstudy.pc.communicator.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.forstudy.pc.communicator.Models.StoredContacts;
import com.forstudy.pc.communicator.R;

import java.util.List;

/**
 * Created by pc on 14.03.17.
 */

public class StoredContactsAdapter extends ArrayAdapter<StoredContacts> {
    public StoredContactsAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StoredContacts contacts = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.messages_list, parent, false);
        }

        TextView Name = (TextView) convertView.findViewById(R.id.Name);
        TextView Login = (TextView) convertView.findViewById(R.id.Login);
        TextView Status = (TextView) convertView.findViewById(R.id.Status);
        Name.setText(contacts.getName());
        Login.setText(contacts.getLogin());
        Status.setText("");

        return convertView;
    }
}
