package com.pptremote.apps.remotebluetooth;



import com.luugiathuy.apps.remotebluetooth.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RemoteBluetooth extends Activity {
	
	// Layout view
	private TextView mTitle;
	
	// Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
   
    // Key names received from the BluetoothCommandService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
	
	// Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for Bluetooth Command Service
    private BluetoothCommandService mCommandService = null;
    private TextView myText = null;
    private int currentSlideNo = 1;
    private int totalSlideNo = 0;
    private boolean isForwardPressed = false;
    private boolean isBackwardPressed = false;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        addListenerOnButton();
    }

	@Override
	protected void onStart() {
		super.onStart();
		
		// If BT is not on, request that it be enabled.
        // setupCommand() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		// otherwise set up the command service
		else {
			if (mCommandService==null)
				setupCommand();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (mCommandService != null) {
			if (mCommandService.getState() == BluetoothCommandService.STATE_NONE) {
				mCommandService.start();
			}
		}
	}

	private void setupCommand() {
		// Initialize the BluetoothChatService to perform bluetooth connections
        mCommandService = new BluetoothCommandService(this, mHandler);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mCommandService != null)
			mCommandService.stop();
	}
	
	private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
	
	// The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothCommandService.STATE_CONNECTED:
                	mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    break;
                case BluetoothCommandService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothCommandService.STATE_LISTEN:
                	break;
                case BluetoothCommandService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_READ:
            	
            	if(msg.arg1 == 255)
            	{	
            		//Toast.makeText(getApplicationContext(), "Forward", Toast.LENGTH_LONG).show();
            		isForwardPressed = true;
            		incrementCurrentSlideNo();
            	}
            	else if(msg.arg1 == 254)
            	{
            		//Toast.makeText(getApplicationContext(), "Backward", Toast.LENGTH_LONG).show();
            		isBackwardPressed = true;
            		decrementCurrentSlideNo();
            	}
            	if(msg.arg1 != 0 && msg.arg1 != 254 && msg.arg1 != 255){
	            	TextView textView=(TextView)findViewById(R.id.textView1);
	        		textView.setText("Total Slide No:"+msg.arg1);
	        		totalSlideNo=msg.arg1;
            	}
            	break;    
            }
        }
    };
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mCommandService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupCommand();
            } else {
                // User did not enable Bluetooth or an error occured
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
        	Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			mCommandService.write(BluetoothCommandService.VOL_UP);
			return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			mCommandService.write(BluetoothCommandService.VOL_DOWN);
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	public void incrementCurrentSlideNo()
	{
		if(currentSlideNo < totalSlideNo )
			currentSlideNo++;
		displayCurrentSlideNo();
		isForwardPressed = false;
	}
	public void decrementCurrentSlideNo()
	{
		if(currentSlideNo>1)
			currentSlideNo--;
		displayCurrentSlideNo();
		isBackwardPressed = false;
    }
	public void addListenerOnButton() {
		
        //Select a specific button to bundle it with the action you want
		Button button1;
		Button button2;
		Button button3;
		Button button4;
		Button button5;
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button4 = (Button) findViewById(R.id.button4);
		button5 = (Button) findViewById(R.id.button5);
		button1.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				mCommandService.write(BluetoothCommandService.F5);
				currentSlideNo=1;
				displayCurrentSlideNo();
			  
			}

		});
		
		button2.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				mCommandService.write(BluetoothCommandService.Forward);
			}

		});
		
button3.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				mCommandService.write(BluetoothCommandService.Backward);
			}

		});
button4.setOnClickListener(new OnClickListener() {
	
	public void onClick(View view) {
		
		EditText editText=(EditText)findViewById(R.id.enterNo);
		if(Integer.parseInt(editText.getText().toString()) <= totalSlideNo){
			mCommandService.write(BluetoothCommandService.Go_To);
			currentSlideNo=Integer.parseInt(editText.getText().toString());
			mCommandService.write(currentSlideNo);
		}
		else 
		{
			Toast.makeText(getApplicationContext(), "Please enter Valid Slide No", Toast.LENGTH_SHORT).show();
		}
		displayCurrentSlideNo();
	}

});
button5.setOnClickListener(new OnClickListener() {
	
	public void onClick(View view) {
		mCommandService.write(BluetoothCommandService.ESC);
		currentSlideNo=1;
		displayCurrentSlideNo();
	  
	}

});
	}
	public void displayCurrentSlideNo()
	{
		TextView textView=(TextView)findViewById(R.id.textView2);
		TextView textView1=(TextView)findViewById(R.id.textView1);
		textView.setText("Current Slide No="+currentSlideNo);
		textView1.setText("Total Number Of Slide ="+totalSlideNo);
	}
}