package edu.buffalo.cse.cse486586.simpledynamo;

import java.util.concurrent.Executors;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class SimpleDynamoActivity extends Activity {

	private static String Node_id;
	String TAG= "adil";
	int avd_port;
	static ContentResolver mContentResolver;
	private Handler uiHandle= new Handler();
	private static final String AUTHORITY = "edu.buffalo.cse.cse486586.simpledynamo.provider";
	private static final String BASE_PATH = myHelper.TABLE_NAME;
	public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/" + BASE_PATH);
	private SimpleDynamoProvider obj;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_dynamo);
		mContentResolver = getContentResolver();
		obj = SimpleDynamoProvider.getInstance();
		Node_id = get_portStr();
		TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        for(String n: SimpleDynamoProvider.nodes) {
        	Executors.newSingleThreadExecutor().execute(new Send(new Message("join",Node_id),Integer.parseInt(n)*2));
		}
	}
	
	public static String get_node_id(){
		return Node_id;
	}
	
	public void LDump(View view) {
    	Cursor resultCursor = mContentResolver.query(CONTENT_URI, null, null, null, "local");
    	//updateTextView("Partition Empty" , true);
    	if (resultCursor.getCount()!=0 && resultCursor.moveToFirst()) {
    	    while (!resultCursor.isAfterLast()) {
    	    	int keyIndex = resultCursor.getColumnIndex("key");
    	        int valueIndex = resultCursor.getColumnIndex("value");
    	    	String returnKey = resultCursor.getString(keyIndex);
    	        String returnValue = resultCursor.getString(valueIndex);
    	        updateTextView(returnKey+" "+returnValue,false);
    	        resultCursor.moveToNext();
    	    	}
    	    }
    	else {
    		updateTextView("Partition Empty",false);
    	}
    }
	
	private void insertValues(String j) {
		int version = ++SimpleDynamoProvider.maxVersion;
		for(int i=0 ; i<20 ; i++) {
			try {
				
				obj.insertRequest(Integer.toString(i),j+Integer.toString(i),version);
				Thread.sleep(1200);
			} catch (InterruptedException e) {
				Log.e(TAG, "Put Sleep fail");
			}
		}
    }

	public void Put1(View view) {
		insertValues("Put1");
		
	}
	
	public void Put2(View view) {
		insertValues("Put2");
	}
	
	public void Put3(View view) {
		insertValues("Put3");
	}
	
	public void Get(View view) {
		//updateTextView("Partition Empty" , true);
		new Thread(
				new Runnable() {
					public void run() {
						for (int i = 0; i < 20; i++) {
							Cursor resultCursor = mContentResolver.query(CONTENT_URI,
							null, Integer.toString(i), null, "ins");
							if (resultCursor != null && resultCursor.moveToFirst()) {
								int keyIndex = resultCursor.getColumnIndex("key");
								int valueIndex = resultCursor.getColumnIndex("value");
								String returnKey = resultCursor.getString(keyIndex);
								String returnValue = resultCursor.getString(valueIndex);
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								updateTextView(returnKey + " " + returnValue, false);
							} else {
								updateTextView("Partition Empty", false);
							}
						}
					}
				}).start();
	}
	
	public String get_portStr() {
    	TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    	String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
    	return portStr;
    }
    
    public void updateTextView(String message, final boolean set) {
    	final String msg= message;
    	uiHandle.post(new Runnable() {
    		public void run() {
    			TextView textView = (TextView)findViewById(R.id.textView1);
    			textView.setMovementMethod(new ScrollingMovementMethod());
    	    	if (!set)
    	    		textView.append(msg+"\n");
    	    	else
    	    		textView.setText(" ");
       		}
    	});
    }
	
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.simple_dynamo, menu);
		return true;
	}
}
