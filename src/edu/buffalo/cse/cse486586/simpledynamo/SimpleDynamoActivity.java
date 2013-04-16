package edu.buffalo.cse.cse486586.simpledynamo;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class SimpleDynamoActivity extends Activity {

	private static String Node_id;
	String TAG= "adil";
	int avd_port;
	static ContentResolver mContentResolver;
	private Handler uiHandle= new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_dynamo);
    
		TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        String portStr = get_portStr();
    	Node_id = portStr;
    	
    	
	}
	
	public static String get_node_id(){
		return Node_id;
	}
	
	public void LDump(View view) {
/*    	SimpleDhtProvider.dump_flag = true;
    	Cursor resultCursor = mContentResolver.query(SimpleDhtProvider.CONTENT_URI, null, null, null, "local");
    	if (resultCursor.moveToFirst()) {
    	    while (!resultCursor.isAfterLast()) {
    	    	int keyIndex = resultCursor.getColumnIndex("key");
    	        int valueIndex = resultCursor.getColumnIndex("value");
    	    	String returnKey = resultCursor.getString(keyIndex);
    	        String returnValue = resultCursor.getString(valueIndex);
    	        updateTextView(returnKey+" "+returnValue);
    	        resultCursor.moveToNext();
    	    	}
    	    }
    	SimpleDhtProvider.dump_flag = false;
*/    }

	public void Put1(View view) {
		
	}
	
	public void Put2(View view) {
		
	}
	
	public void Put3(View view) {
		
	}
	
	public void Get(View view) {
		
	}
	
	public String get_portStr() {
    	TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    	String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
    	return portStr;
    }
    
    public void updateTextView(String message) {
    	final String msg= message;
    	uiHandle.post(new Runnable() {
    		public void run() {
    			TextView textView = (TextView)findViewById(R.id.textView1);
    			textView.setMovementMethod(new ScrollingMovementMethod());
    	    	//Log.v(TAG, "updating textview");
    	    	textView.append(msg+"\n");
    	    	//Log.v(TAG, "updated textview");
       		}
    	});
    }

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.simple_dynamo, menu);
		return true;
	}

}
