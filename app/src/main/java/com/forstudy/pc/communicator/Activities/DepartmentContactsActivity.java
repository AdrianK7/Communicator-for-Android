package com.forstudy.pc.communicator.Activities;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.forstudy.pc.communicator.Adapters.DepartmentContactsAdapter;
import com.forstudy.pc.communicator.DAO.ContactsDAO;
import com.forstudy.pc.communicator.Models.Employees;
import com.forstudy.pc.communicator.Models.StoredContacts;
import com.forstudy.pc.communicator.Services.MessageService;
import com.forstudy.pc.communicator.R;
import com.forstudy.pc.communicator.Services.ServiceCallbacks;
import com.forstudy.pc.communicator.Utilities.EmployeesResponseList;

import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class DepartmentContactsActivity extends AppCompatActivity implements ServiceCallbacks {


    private Intent intentForExtra;
    private boolean serviceIsBound = false;
    private MessageService boundService;
    private int position = -1;
    private DepartmentContactsAdapter adapter;
    private ListView listView;
    private Drawable defaultSelector;
    private Button messagesButton;
    private Button deptButton;
    private Button storedMsgButton;
    private SharedPreferences prefs;
    private Editor prefsEditor;

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            boundService = ((MessageService.LocalBinder)service).getService();
            boundService.setCallbacks(DepartmentContactsActivity.this);
            boundService.getDepartmentContacts(intentForExtra.getIntExtra("id_dept", 0));
        }

        public void onServiceDisconnected(ComponentName className) {
            boundService = null;

        }
    };

    void doBindService() {
        serviceIsBound = bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    void doUnbindService() {
        if (serviceIsBound) {
            boundService.setCallbacks(null);
            unbindService(serviceConnection);
            serviceIsBound = false;
        }
    }

    @Override
    public void onResume() {
        if(isMyServiceRunning(MessageService.class)) {
            doBindService();
        }
        else {
            if(prefs.getString("authorization_token", null) == null) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
            else {
                Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
                startService(serviceIntent);
                doBindService();
            }
        }
        if(boundService != null) {
            boundService.getSentMessages();
        }
        setDepartmentsList();
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (serviceIsBound) {
            doUnbindService();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
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
            case R.id.addContact:
                if(position != -1) {
                    ContactsDAO contactDAO = new ContactsDAO(getApplicationContext());

                    StoredContacts contact = new StoredContacts();
                    contact.setLogin(adapter.getItem(position).getLogin());
                    contact.setName(adapter.getItem(position).getFirstName() + " " + adapter.getItem(position).getSecondName());
                    contact.setId(adapter.getItem(position).getId());
                    long err = contactDAO.addContact(contact);
                    if(err != -1) {
                        Toast.makeText(getApplicationContext(),
                                "Contact added!",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(),
                                "Failed to add contact!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Contact not selected!",
                            Toast.LENGTH_SHORT).show();
                }
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
        intentForExtra = getIntent();

        Context cont = getApplicationContext();
        prefs = cont.getSharedPreferences("preferences", cont.MODE_PRIVATE);
        prefsEditor = prefs.edit();

        if(isMyServiceRunning(MessageService.class)) {
            doBindService();
        }
        else {
            if(prefs.getString("authorization_token", null) == null) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
            else {
                Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
                startService(serviceIntent);
                doBindService();
            }
        }

        listView = (ListView) findViewById(R.id.usersListView);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long id) {
                if(defaultSelector == null)
                    defaultSelector = listView.getSelector();
                listView.setSelector(R.color.colorSelected);
                listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                position = i;
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                if(defaultSelector != null)
                    listView.setSelector(defaultSelector);
                position = -1;
                Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
                intent.putExtra("id_receiver", adapter.getItem(i).getId());
                startActivity(intent);
            }
        });

        setDepartmentsList();
        messagesButton = (Button) findViewById(R.id.Messages);

        messagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doUnbindService();
                Intent intent = new Intent(getApplicationContext(), NewMessagesActivity.class);
                startActivity(intent);
            }
        });
        deptButton = (Button) findViewById(R.id.deptContacts);

        deptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doUnbindService();
                Intent intent = new Intent(getApplicationContext(), DepartmentsActivity.class);
                startActivity(intent);
            }
        });

        storedMsgButton = (Button) findViewById(R.id.myContacts);

        storedMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doUnbindService();
                Intent intent = new Intent(getApplicationContext(), MyContactsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setDepartmentsList() {

        adapter = new DepartmentContactsAdapter(this);
        listView.setAdapter(adapter);

        if(employees != null)
            adapter.addAll(employees);
    }


    private List<Employees> employees;
    @Override
    public void taskResponseCall(ResponseEntity<?> response, int whatTask) {
        if(response.getBody().getClass().equals(EmployeesResponseList.class)) {
            employees = (EmployeesResponseList) response.getBody();
            setDepartmentsList();
        }
    }
}



