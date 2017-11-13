package com.forstudy.pc.communicator.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.forstudy.pc.communicator.Models.MessagesForLocalDB;
import com.forstudy.pc.communicator.R;

import java.util.List;

/**
 * Created by pc on 09.03.17.
 */

public class MessagesAdapter extends ArrayAdapter<MessagesForLocalDB> {
    public MessagesAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessagesForLocalDB sender = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.messages_list, parent, false);
        }

        TextView Name = (TextView) convertView.findViewById(R.id.Name);
        TextView Login = (TextView) convertView.findViewById(R.id.Login);
        TextView Status = (TextView) convertView.findViewById(R.id.Status);

        Name.setText(sender.getFirstName() + " " + sender.getSecondName());
        Login.setText(sender.getSender());
        if(sender.getReceived() == 0) {
            Status.setText("New message!");
        }
        else {
            Status.setText(" ");
        }

        return convertView;
    }
}

