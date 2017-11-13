package com.forstudy.pc.communicator.Activities;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.forstudy.pc.communicator.Adapters.DepartmentsAdapter;
import com.forstudy.pc.communicator.Models.Departments;
import com.forstudy.pc.communicator.Models.Employees;
import com.forstudy.pc.communicator.Services.MessageService;
import com.forstudy.pc.communicator.R;
import com.forstudy.pc.communicator.Services.ServiceCallbacks;
import com.forstudy.pc.communicator.Utilities.DepartmentsResponseList;

import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class DepartmentsActivity extends AppCompatActivity implements ServiceCallbacks {

    private Button MessagesButton;
    private Button deptButton;
    private Button storedMsgButton;
    private ArrayAdapter adapter;
    private ListView departmentsListView;
    private ArrayList<Departments> arrayOfDepartments;
    private MessageService boundService;
    private boolean serviceIsBound = false;
    private SharedPreferences prefs;
    private Editor prefsEditor;
    private List<Departments> departments;



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
            boundService.setCallbacks(DepartmentsActivity.this);
            boundService.getDepartments();
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
        departmentsListView = (ListView) findViewById(R.id.usersListView);

        departmentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), DepartmentContactsActivity.class);
                intent.putExtra("id_dept", arrayOfDepartments.get(i).getId());
                startActivity(intent);
            }
        });

        setDepartmentsList();
        MessagesButton = (Button) findViewById(R.id.Messages);

        MessagesButton.setOnClickListener(new View.OnClickListener() {
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
                Toast.makeText(getApplicationContext(),
                        "You are here!",
                        Toast.LENGTH_SHORT).show();
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

        arrayOfDepartments = new ArrayList<>();
        adapter = new DepartmentsAdapter(this, arrayOfDepartments);

        departmentsListView.setAdapter(adapter);
        if(departments != null)
            adapter.addAll(departments);
    }

    @Override
    public void taskResponseCall(ResponseEntity<?> response, int whatTask) {
        if(response.getBody().getClass().equals(DepartmentsResponseList.class)) {
            departments = (DepartmentsResponseList) response.getBody();
            setDepartmentsList();
        }
    }
}



