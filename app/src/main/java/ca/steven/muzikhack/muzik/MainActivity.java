package ca.steven.muzikhack.muzik;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.muzik.accessory.MzAccessory;
import com.muzik.accessory.MzConnectionState;
import com.muzik.accessory.callback.IAccelerometerCallback;
import com.muzik.accessory.callback.IBatteryLevelCallback;
import com.muzik.accessory.callback.IMzConnectionStateCallback;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private MzAccessory mza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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

    public void onConnect() {
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
            }
        });

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
}
