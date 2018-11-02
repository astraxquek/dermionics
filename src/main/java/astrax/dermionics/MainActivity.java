package astrax.dermionics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    int red = Color.parseColor("#FF0000");
    int green = Color.parseColor("#008000");

    Button btnOnOff,btnDiscover,btnSend,btnWV;
    ListView listView;
    TextView wifiStatus,connection;
    EditText webLink;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int  MESSAGE_READ = 1;

//    ServerClass serverClass;
//    CilentClass cilentClass;
//    SendReceive sendReceive;

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initialise();
        executeListener();

    }
//    android.os.Handler handler = new android.os.Handler(new android.os.Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            switch (msg.what){
//                case MESSAGE_READ:
//                    byte[] readBuff = (byte[]) msg.obj;
//                    String tempMsg = new String(readBuff,0,msg.arg1);
//                    read_msg_box.setText(tempMsg);
//                    break;
//            }
//            return true;
//        }
//    });


    private void executeListener() {
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //read_msg_box.setText(getWFDMacAddress());
                if(wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    btnOnOff.setText("ON");
                    btnOnOff.setTextColor(green);
                    wifiStatus.setText("WIFI STATUS: OFF");
                }else {
                    wifiManager.setWifiEnabled(true);
                    btnOnOff.setText("OFF");
                    btnOnOff.setTextColor(red);
                    wifiStatus.setText("WIFI STATUS: ON");
                }

            }
        }); //End of btnOnOff Listener

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connection.setText("Discovery commenced.");
                    }

                    @Override
                    public void onFailure(int reason) {
                        connection.setText("Discovery failed");
                    }
                });
            }
        }); //End of btnDiscover Listener

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = deviceArray[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"Connected to " + device.deviceName + device.deviceAddress,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(),"Not Connected" ,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }); //End of listView Listener

//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String  msg = write_msg.getText().toString();
//                sendReceive.write(msg.getBytes());
//            }
//        });

        btnWV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebView.class);
                intent.putExtra("link", webLink.getText().toString());
                startActivity(intent);
            }
        });
    }

    private void initialise() {
        btnOnOff =(Button) findViewById(R.id.onOff);
        btnDiscover =(Button) findViewById(R.id.discover);
        btnSend =(Button) findViewById(R.id.sendButton);
        btnWV = (Button) findViewById(R.id.btnWV) ;
        listView =(ListView) findViewById(R.id.peerListView);
        wifiStatus = (TextView) findViewById(R.id.wifiStatus);
        connection =(TextView) findViewById(R.id.connectionStatus);
        webLink =(EditText) findViewById(R.id.webLink);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        mChannel = mManager.initialize(this,getMainLooper(),null);

        mReceiver = new WiFiDirect(mManager,mChannel,this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        if(wifiManager.isWifiEnabled()) {
            btnOnOff.setText("OFF");
            btnOnOff.setTextColor(red);
            wifiStatus.setText("WIFI STATUS: ON");
        }else {
            btnOnOff.setText("ON");
            btnOnOff.setTextColor(green);
            wifiStatus.setText("WIFI STATUS: OFF");
        }

    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray     = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index       = 0;
                for(WifiP2pDevice device : peerList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index]     = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);
            }
            if(peers.size() == 0){
                Toast.makeText(getApplicationContext(),"No Device Found..",Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };


    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if(info.groupFormed && info.isGroupOwner){
                connection.setText("Host");
//                serverClass=new ServerClass();
//                serverClass.start();
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        // Do something after 5s = 5000ms
//                        String link = "http://192.168.49.50:8080";
//                        Intent intent = new Intent(MainActivity.this, WebView.class);
//                        intent.putExtra("link", link);
//                        startActivity(intent);
//                    }
//                }, 3000);

            }else if(info.groupFormed){
                connection.setText("Cilent");
//                cilentClass= new CilentClass(groupOwnerAddress);
//                cilentClass.start();
            }
        }
    };



    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

//    public class ServerClass extends Thread{
//        Socket socket;
//        ServerSocket serverSocket;
//
//        @Override
//        public void run() {
//            super.run();
//            try {
//                serverSocket = new ServerSocket(8888);
//                socket = serverSocket.accept();
//                sendReceive = new SendReceive(socket);
//                sendReceive.start();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    private class SendReceive extends Thread{
//        private Socket socket;
//        private InputStream inputStream;
//        private OutputStream outputStream;
//
//        public SendReceive(Socket skt){
//            socket = skt;
//            try {
//                inputStream = socket.getInputStream();
//                outputStream = socket.getOutputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void run() {
//            byte[] buffer = new byte[1024];
//            int bytes;
//
//            while(socket != null){
//                try {
//                    bytes = inputStream.read(buffer);
//                    if(bytes > 0){
//                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        public void write(byte[] bytes){
//            try {
//                outputStream.write(bytes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    public class CilentClass extends Thread{
//        Socket socket;
//        String hostAdd;
//
//        public CilentClass(InetAddress hostAddress){
//            hostAdd = hostAddress.getHostAddress();
//            socket = new Socket();
//        }
//
//        @Override
//        public void run() {
//            try {
//                socket.connect(new InetSocketAddress(hostAdd,8888),500);
//                sendReceive=new SendReceive(socket);
//                sendReceive.start();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public String getWFDMacAddress(){
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ntwInterface : interfaces) {

                if (ntwInterface.getName().equalsIgnoreCase("p2p1")) {
                    byte[] byteMac = ntwInterface.getHardwareAddress();
                    if (byteMac==null){
                        return null;
                    }
                    StringBuilder strBuilder = new StringBuilder();
                    for (int i=0; i<byteMac.length; i++) {
                        strBuilder.append(String.format("%02X:", byteMac[i]));
                    }

                    if (strBuilder.length()>0){
                        strBuilder.deleteCharAt(strBuilder.length()-1);
                    }

                    return strBuilder.toString();
                }

            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    public void disconnect() {

        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null
                            && group.isGroupOwner()) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "removeGroup onSuccess -");
                                //read_msg_box.setText("SUCCESS");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "removeGroup onFailure -" + reason);
                                //read_msg_box.setText("FAIL");
                            }
                        });
                    }
                }
            });
        }
    }
}
