package com.forstudy.pc.communicator.Activities;

import android.app.ActivityManager;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.forstudy.pc.communicator.Adapters.MessagingAdapter;
import com.forstudy.pc.communicator.DAO.MessagesDAO;
import com.forstudy.pc.communicator.Models.MessagesForLocalDB;
import com.forstudy.pc.communicator.R;
import com.forstudy.pc.communicator.Services.MessageService;
import com.forstudy.pc.communicator.Services.ServiceCallbacks;

import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class MessagingActivity extends AppCompatActivity  implements ServiceCallbacks {

    private ListView messagesListView;
    private String privateKeyEncrypted;
    private String signingKeySender;
    private Intent intentForExtra;
    private boolean serviceIsBound = false;
    private SharedPreferences prefs;
    private Editor prefsEditor;
    private MessageService boundService;
    private MessagingAdapter messagingAdapter;
    private EditText messageEditText;
    private MessagesDAO messagesDAO;

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
            boundService.setCallbacks(MessagingActivity.this);
            boundService.getPrivateKey();
            boundService.getSigningKey(intentForExtra.getIntExtra("id_receiver", 0));
        }

        public void onServiceDisconnected(ComponentName className) {
            privateKeyEncrypted = null;
            messagingAdapter.setPrivateKeyToNull();
        }
    };

    void doBindService() {
        serviceIsBound =  true;
        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);
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
        if(privateKeyEncrypted != null) {
            getMessages();
        }

        super.onResume();
    }

    @Override
    protected void onStop() {
        if (serviceIsBound) {
            doUnbindService();
        }
        //privateKeyEncrypted = null;
        messagingAdapter.setPrivateKeyToNull();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        doUnbindService();
        privateKeyEncrypted = null;
        unregisterReceiver(messagesBReceiver);
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
        setContentView(R.layout.messaging);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        intentForExtra = getIntent();
        setSupportActionBar(myToolbar);
        Context cont = getApplicationContext();
        prefs = cont.getSharedPreferences("preferences", cont.MODE_PRIVATE);
        prefsEditor = prefs.edit();
        messagesListView = (ListView) findViewById(R.id.listMessages);
        //getMessages();
        doBindService();

        registerReceiver(messagesBReceiver, new IntentFilter(MessageService.INTENT_FILTER_UPDATE_MESSAGE));

        MessagesDAO messagesDAO = new MessagesDAO(this);

        List<MessagesForLocalDB> messages = messagesDAO.getAllMessages("receiver == " + prefs.getInt("id", 0) +
                " AND sender_id == " + intentForExtra.getIntExtra("id_receiver", 0) +
                " AND received == 0 ORDER BY sent DESC");
        for(MessagesForLocalDB message : messages) {
            message.setReceived(System.currentTimeMillis());
            messagesDAO.updateMessage(message);
        }

        messageEditText = (EditText) findViewById(R.id.messageBodyField);

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(messageEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "Message is empty!",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Sending...",
                            Toast.LENGTH_SHORT).show();
                    boundService.sendMessage(messageEditText.getText().toString(), intentForExtra.getIntExtra("id_receiver", 0), null);
                }
            }
        });
    }

    private void getMessages() {
        messagesDAO = new MessagesDAO(this);
        messagingAdapter = new MessagingAdapter(this, this.getLayoutInflater(), privateKeyEncrypted, signingKeySender);
        messagesListView.setAdapter(messagingAdapter);
        messagingAdapter.addAll(messagesDAO.getAllMessages("receiver == " + intentForExtra.getIntExtra("id_receiver", 0) +
                " OR (receiver == " + prefs.getInt("id", 0) +
                " AND sender_id == " + intentForExtra.getIntExtra("id_receiver", 0) +
                ") ORDER BY sent ASC"));
    }

    @Override
    public void taskResponseCall(ResponseEntity<?> response, int whatTask) {
        if(whatTask == MessageService.GET_PRIVATE_KEY) {
            privateKeyEncrypted = (String) response.getBody();
            if(privateKeyEncrypted != null && signingKeySender != null) {
                getMessages();
                messagingAdapter.notifyDataSetChanged();
            }
        }
        if(whatTask == MessageService.GET_SIGN_KEY) {
            signingKeySender = (String) response.getBody();
            if(privateKeyEncrypted != null && signingKeySender != null) {
                getMessages();
                messagingAdapter.notifyDataSetChanged();
            }
        }
    }

    private BroadcastReceiver messagesBReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if(privateKeyEncrypted != null && signingKeySender != null) {
                    getMessages();
                    messagingAdapter.notifyDataSetChanged();
                }
            }
            catch(RuntimeException e) {
                Log.d("MessagingActivity: ", "Failed to recive messages");
            }
        }
    };
}