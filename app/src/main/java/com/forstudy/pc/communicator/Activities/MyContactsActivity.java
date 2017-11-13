package com.forstudy.pc.communicator.Activities;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.forstudy.pc.communicator.Adapters.StoredContactsAdapter;
import com.forstudy.pc.communicator.DAO.ContactsDAO;
import com.forstudy.pc.communicator.Models.StoredContacts;
import com.forstudy.pc.communicator.Services.MessageService;
import com.forstudy.pc.communicator.R;
import com.forstudy.pc.communicator.Services.ServiceCallbacks;

import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

public class MyContactsActivity extends AppCompatActivity {

    private Button MessagesButton;
    private Button deptButton;
    private Button storedMsgButton;
    private StoredContactsAdapter adapter;
    private ListView contactsListView;
    private ArrayList<StoredContacts> arrayOfContacts;
    private boolean serviceIsBound = false;
    private SharedPreferences prefs;
    private Editor prefsEditor;
    private MessageService boundService;

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {

        setContactsList();
        super.onResume();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    public void onDestroy() {
        //logout();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        menu.removeItem(R.id.addContact);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                prefsEditor.putString("authorization_token", null);
                prefsEditor.commit();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        Context cont = getApplicationContext();
        prefs = cont.getSharedPreferences("preferences", cont.MODE_PRIVATE);
        prefsEditor = prefs.edit();

        if(!isMyServiceRunning(MessageService.class)) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }

        contactsListView = (ListView) findViewById(R.id.usersListView);

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
                intent.putExtra("id_receiver", adapter.getItem(i).getId());
                startActivity(intent);
            }
        });

        setContactsList();

        MessagesButton = (Button) findViewById(R.id.Messages);

        MessagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewMessagesActivity.class);
                startActivity(intent);
            }
        });
        deptButton = (Button) findViewById(R.id.deptContacts);

        deptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DepartmentsActivity.class);
                startActivity(intent);
            }
        });

        storedMsgButton = (Button) findViewById(R.id.myContacts);

        storedMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "You are here!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setContactsList() {
        ContactsDAO contactsDAO = new ContactsDAO(this);

        arrayOfContacts = new ArrayList<>();
        adapter = new StoredContactsAdapter(this);
        contactsListView.setAdapter(adapter);
        adapter.addAll(contactsDAO.getAllContacts());
    }
}



