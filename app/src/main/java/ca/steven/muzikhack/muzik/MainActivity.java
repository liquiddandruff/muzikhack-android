package ca.steven.muzikhack.muzik;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.muzik.accessory.MzAccessory;
import com.muzik.accessory.MzConnectionState;
import com.muzik.accessory.callback.IAccelerometerCallback;
import com.muzik.accessory.callback.IBatteryLevelCallback;
import com.muzik.accessory.callback.IMotionCallback;
import com.muzik.accessory.callback.IMzConnectionStateCallback;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private MzAccessory mza;

    private IntentFilter spotifyIntentFilter;
    static final class BroadcastTypes {
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
    }

    ListView listView;
    ArrayAdapter<String> adapter;

    boolean haveMetadata = false;
    String trackId;
    String artistName;
    String albumName;
    String trackName;
    ArrayList<String> songInfos;

    private BroadcastReceiver spotifyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
            String action = intent.getAction();
            if (action.equals(BroadcastTypes.METADATA_CHANGED)) {
                trackId = intent.getStringExtra("id");
                artistName = intent.getStringExtra("artist");
                albumName = intent.getStringExtra("album");
                trackName = intent.getStringExtra("track");
                haveMetadata = true;
                int trackLengthInSec = intent.getIntExtra("length", 0);
                // Do something with extracted information...
            }
        }
    };

    private static final String REDIRECT_URI = "yourcustomprotocol://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spotifyIntentFilter = new IntentFilter();
        spotifyIntentFilter.addAction("com.spotify.music.playbackstatechanged");
        spotifyIntentFilter.addAction("com.spotify.music.metadatachanged");
        spotifyIntentFilter.addAction("com.spotify.music.queuechanged");

        listView = (ListView) findViewById(R.id.listView);
        String[] values = new String[] { "Android List View",
                "Adapter implementation",
                "Simple List View In Android",
                "Create List View Android",
                "Android Example",
                "List View Source Code",
                "List View Array Adapter",
                "Android Example List View"
        };

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.i(TAG, "Device does not support bluetooth");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.i(TAG, "Bluetooth intent");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        mza = new MzAccessory();
        mza.startServer();
        Log.i(TAG, "onCreate");
        mza.registerForConnectionState(new IMzConnectionStateCallback() {
            @Override
            public void onConnectionStateChange(MzConnectionState mzConnectionState) {
                switch (mzConnectionState) {
                    case HEADPHONES_CONNECTED:
                        onConnect();
                        Log.i(TAG, "Headphones are connected!");
                        break;
                    case HEADPHONES_NOT_CONNECTED:
                        Log.i(TAG, "Headphones are not connected...");
                        break;
                    case BLUETOOTH_NOT_ENABLED:
                        Log.i(TAG, "Bluetooth not enabled.");
                        break;
                    case NO_BLUETOOTH_SUPPORT:
                        Log.i(TAG, "This is an old phone...");
                        break;
                    case INTERNAL_ERROR:
                        Log.i(TAG, "Oopsie!");
                        break;
                    default:
                        Log.i("wtf", "wtf");
                        break;
                }
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    long startTime;
    ArrayList<Float> forwardAngles;
    public void onConnect() {
        startTime = System.currentTimeMillis();
        forwardAngles = new ArrayList<>(100);
        songInfos = new ArrayList<>();

        mza.getBatteryLevel(new IBatteryLevelCallback() {
            @Override
            public void onResponseReceived(int i) {
                Log.i(TAG, "battery: " + Integer.toString(i));
            }
        });
        mza.registerForAccelerometerDataStream(new IAccelerometerCallback() {
            @Override
            public void onResponseReceived(float x, float y, float z, float norm, float forwardAngle, float sideAngle) {
                Log.i(TAG, String.format("accel X:%f Y:%f Z:%f N:%f FA:%f SA:%f", x, y, z, norm, forwardAngle, sideAngle));
                long currTime = System.currentTimeMillis();
                forwardAngles.add(forwardAngle);
                if(currTime - startTime > 5000) {
                    startTime = currTime;
                    float high = 30.0f;
                    float low = 10.0f;
                    int numHigh = 0;
                    int numLow = 0;
                    for(Float angle : forwardAngles) {
                        if(angle >= high) {
                            numHigh++;
                        } else if(angle <= low) {
                            numLow++;
                        }
                    }
                    if(Math.abs(numHigh - numLow) < 10) {
                        if(haveMetadata) {
                            // for now
                            String songInfo = artistName + " - " + trackName;
                            songInfos.add(songInfo);
                            
                            // send to spotify
                            Log.i(TAG, String.format("new Liking song %s - %s on spotify", artistName, trackName));
                        }
                    }
                    Log.i(TAG, String.format("high: %d low: %d", numHigh, numLow));
                    forwardAngles.clear();
                }
            }
        });
        mza.registerForMotions(new IMotionCallback() {
            @Override
            public void onMotionDetected(int i) {
                if(haveMetadata) {
                    // send to spotify
                    Log.i(TAG, String.format("Liking song %s - %s on spotify", artistName, trackName));
                }
            }
        }, 4); // bobbing = 4

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://ca.steven.muzikhack.muzik/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://ca.steven.muzikhack.muzik/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    protected void onResume() {
        registerReceiver(spotifyReceiver, spotifyIntentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(spotifyReceiver);
        super.onPause();
    }
}
