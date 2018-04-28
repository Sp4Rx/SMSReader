package io.github.sp4rx.smsreader;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import io.github.sp4rx.smsreader.adapter.MessagesAdapter;
import io.github.sp4rx.smsreader.model.Message;
import io.github.sp4rx.smsreader.view.RecyclerViewAdvance;

public class MainActivity extends AppCompatActivity {
    private static final int HOUR_MILLIS = 3600000;
    public static final int MAX_RESULTS_PER_PAGE = 10;
    private static final long LOADING_TIME = 1500;
    private MessagesAdapter messagesAdapter;
    private List<Message> messages;
    private SpinKitView spinKitView;
    private Button btReqPermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinKitView = findViewById(R.id.spin_kit);

        btReqPermission = findViewById(R.id.btReqPermission);
        btReqPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
        requestPermission();
    }

    /**
     * Request sms permission
     */
    private void requestPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_SMS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        btReqPermission.setVisibility(View.GONE);
                        initSMS();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            showSnackBar();
                        } else {
                            btReqPermission.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * Initialization
     */
    private void initSMS() {
        messages = getSmsByPage(this, 1);
        if (messages == null || messages.isEmpty()) {
            Toast.makeText(MainActivity.this, "No message", Toast.LENGTH_SHORT).show();
            return;
        }
        final RecyclerViewAdvance rvMessages = findViewById(R.id.messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        messagesAdapter = new MessagesAdapter(messages);
        rvMessages.setAdapter(messagesAdapter);
        rvMessages.setInitPageNumber(2);

        rvMessages.setOnBottomReached(new RecyclerViewAdvance.OnBottomReached() {
            @Override
            public void onBottomReached(int page) {
                List<Message> messages = getSmsByPage(MainActivity.this, page);
                if (messages == null || messages.isEmpty()) {
                    Toast.makeText(MainActivity.this, "End of the world!", Toast.LENGTH_SHORT).show();
                    rvMessages.reachedEnd();
                } else {
                    fakeLoading();
                    messagesAdapter.addAll(messages);
                }
            }
        });
    }

    /**
     * Returns sms based on page numbers
     *
     * @param context Context
     * @param page    integer value , starts from 1
     * @return List of messages
     */
    public List<Message> getSmsByPage(Context context, int page) {
        List<Message> messages = new ArrayList<>();

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE + " DESC LIMIT " +
                ((page - 1) * MAX_RESULTS_PER_PAGE) + "," + MAX_RESULTS_PER_PAGE);
        long lastHour = 0;
        if (c != null) {
            int totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    Message message = new Message();
                    message.setBody(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY)));
                    message.setAddress(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)));
                    message.setDate(c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE)));

                    if (j == 0) {
                        Message headerMessage = new Message();
                        headerMessage.setType(Message.HEADER);
                        headerMessage.setDate(message.getDate());
                        messages.add(headerMessage);
                        message.setType(Message.MESSAGE);
                        messages.add(message);
                        lastHour = (System.currentTimeMillis() - message.getDate()) / HOUR_MILLIS;
                    } else {
                        long currentHour = (System.currentTimeMillis() - message.getDate()) / HOUR_MILLIS;
                        if (currentHour == lastHour) {
                            message.setType(Message.MESSAGE);
                            messages.add(message);
                            lastHour = currentHour;
                        } else {
                            Message headerMessage = new Message();
                            headerMessage.setType(Message.HEADER);
                            headerMessage.setDate(message.getDate());
                            messages.add(headerMessage);
                            message.setType(Message.MESSAGE);
                            messages.add(message);
                            lastHour = currentHour;
                        }
                    }
                    c.moveToNext();
                }
            }
            c.close();
            return messages;

        } else {
            return null;
        }
    }


    private Handler loadingHandler = new Handler();
    private Runnable loadingRunnable = new Runnable() {
        @Override
        public void run() {
            spinKitView.setVisibility(View.GONE);
        }
    };

    /**
     * This is a fake loading of 1.5 sec to give a feel of pagination loading
     * as pagination loads immediately
     */
    private void fakeLoading() {
        spinKitView.setVisibility(View.VISIBLE);
        loadingHandler.postDelayed(loadingRunnable, LOADING_TIME);
    }

    /**
     * Snack bar if permission is denied permanently
     */
    private void showSnackBar() {
        btReqPermission.setVisibility(View.GONE);
        Snackbar.make(findViewById(R.id.mainActivity), "Please enable permission from settings.", BaseTransientBottomBar.LENGTH_INDEFINITE)
                .setAction("Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Build intent that displays the App settings screen.
                        Intent intent = new Intent();
                        intent.setAction(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",
                                BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).show();
    }
}
