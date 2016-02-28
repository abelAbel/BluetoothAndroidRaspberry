package rasbteam.bluetoothandroidrasb;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothLog";
    ArrayList<BluetoothDevice> mArrayAdapter = new ArrayList<>();
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice dev1  ;

    Button startB ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startB = (Button)findViewById(R.id.bleutoothStart);

        startB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Start of bluetooth stufff........

//        Determine if Android supports Bluetooth
                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Your device does not support Bluetooth");
                } else {
                    Toast.makeText(getApplicationContext(), "Your device support Bluetooth", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Your device support Bluetooth");
                    //        Turn on Bluetooth if disabled
                    if (!mBluetoothAdapter.isEnabled()) {
                        int REQUEST_ENABLE_BT = 1;
                        //Prompt the user to enable bluetooth
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        //or just do this, which does not prompt user to enable the bluetooth it just enables it in the background -> mBluetoothAdapter.enable();
                        Log.d(TAG, "Successfully enabled Bluetooth");
                    } else {
                        Log.d(TAG, "Bluetooth is already enabled");
                        Toast.makeText(getApplicationContext(), "Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
    }


    public void discoverM(View view) {

        //Make it discoverable (Print the nearby device)
        // Create a BroadcastReceiver for ACTION_FOUND
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            int counter = 0;
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                            if(!mArrayAdapter.contains(device)){
                                mArrayAdapter.add(device);
                                Log.d(TAG, "Size: "+ mArrayAdapter.size());
                            }
                    if(device.getAddress().equals("AC:D1:B8:E1:54:E0")){
//                    if(device.getAddress().equals("FC:F8:AE:36:F4:42")){
                        mArrayAdapter.add(device);
                        dev1= device;
                    }

                    //Dean computer FC:F8:AE:36:F4:42

                }

            }

        };

        mBluetoothAdapter.startDiscovery();
        for(BluetoothDevice i: mArrayAdapter){
            Log.d(TAG,"Device: "+ i.getName() + "\n" + i.getAddress());
            Toast.makeText(getApplicationContext(),"Device: "+ i.getName() + "\n" + i.getAddress(),Toast.LENGTH_SHORT).show();
        }
        mArrayAdapter.clear();

// Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    public void connecting(View view) {
        new ConnectThread(dev1).start();
    }
//               public static final Handler mHandler = new Handler() {
//                @Override
//                public void handleMessage(Message msg) {
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    int begin = (int)msg.arg1;
//                    int end = (int)msg.arg2;
//                    switch(msg.what) {
//                        case 1:
//                            String writeMessage = new String(writeBuf);
//                            writeMessage = writeMessage.substring(begin, end);
//                            Log.d(TAG,"S:"+writeMessage+"");
//
//                            break;
//                    }
//
//                }
//            };

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        private void manageConnectedSocket(BluetoothSocket mmSocket) {
//            Toast.makeText(getApplicationContext(),"Going to send something..",Toast.LENGTH_SHORT).show();
            Log.d("BluetoothLog", "Going to Start connected thread");
            new ConnectedThread(mmSocket).start();
        }

        private class ConnectedThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;

            public ConnectedThread(BluetoothSocket socket) {
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the input and output streams, using temp objects because
                // member streams are final
                try {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) { }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }





            public void run() {
                byte[] buffer = new byte[1024];  // buffer store for the stream
//                int bytes; // bytes returned from read()
                final int MESSAGE_READ =1;
                // Keep listening to the InputStream until an exception occurs
//                while (true) {
                    try {
                        write("1".getBytes());
                        // Read from the InputStream
//                        bytes = mmInStream.read(buffer);
//                        // Send the obtained bytes to the UI activity
//                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                        Log.d(TAG, "getting inputstream.....");
                        int read = -1;
//                        byte[] bytes = new byte[2048];
                        byte[] bytes = new byte[256];
                        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
                        read = mmInStream.read(bytes);
                        baos.write(bytes, 0, read);
                        byte[] req = baos.toByteArray();
                        Log.d(TAG, "read:" + new String(req));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                }
            }

            /* Call this from the main activity to send data to the remote device */
            public void write(byte[] bytes) {
                try {
                    mmOutStream.write(bytes);
                } catch (IOException e) { }
            }

            /* Call this from the main activity to shutdown the connection */
            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) { }
            }
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
