package org.near.kodirelayremote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final String ADDRESS = "192.168.1.54";
    private final int PORT = 10000;
    private Device device;

    private Button btnRefresh;
    private ImageButton ibtnAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRefresh = (Button)findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        final String STATUS = device.sendAction("INFO");
                        ibtnAction.post(new Runnable() {
                            public void run() {
                                changeBtnActionImage(STATUS);
                            }
                        });

                    }
                }).start();
            }
        });

        ibtnAction = (ImageButton) findViewById(R.id.ibtnAction);
        ibtnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        final String STATUS = device.sendAction("ON");// TODO STATUS could be null.. fix it
                        ibtnAction.post(new Runnable() {
                            public void run() {
                                changeBtnActionImage(STATUS);
                            }
                        });

                    }
                }).start();
            }
        });

        device = new Device(ADDRESS, PORT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshControls();
    }

    public void refreshControls() {
        new Thread(new Runnable() {
            public void run() {
                device.connect();
                final String STATUS = device.sendAction("INFO");
                ibtnAction.post(new Runnable() {
                    public void run() {
                        changeBtnActionImage(STATUS);
                    }
                });
            }
        }).start();
    }

    public void changeBtnActionImage(String status) {
        if(status.compareTo("0") == 0) {
            ibtnAction.setImageResource(R.mipmap.button_off);
            Toast.makeText(this, "Tv is OFF", Toast.LENGTH_SHORT).show();
        } else {
            ibtnAction.setImageResource(R.mipmap.button_on);
            Toast.makeText(this, "TV is ON", Toast.LENGTH_SHORT).show();
        }
    }
}
