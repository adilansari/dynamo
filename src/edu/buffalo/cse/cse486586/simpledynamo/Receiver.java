package edu.buffalo.cse.cse486586.simpledynamo;

import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

class Receiver implements Runnable {

	Socket sock= null;
	Message msg;
	private SimpleDynamoProvider provider;
	private final String TAG = "adil recvr";
	ExecutorService ex= Executors.newSingleThreadExecutor();
	private static final String AUTHORITY = "edu.buffalo.cse.cse486586.simpledynamo.provider";
	private static final String BASE_PATH = "dummy";
	public static final Uri DUP_CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/" + BASE_PATH);
	
	Receiver (Message s) {
		this.msg= s;
		provider = SimpleDynamoProvider.getInstance();
	}

	public void run() {
		Log.i("adil rcvr", "recvd msg: "+ msg.Node_id + " "+ msg.id+ " key:"+ msg.key+" value:"+msg.value+ " "+msg.version);
		
		if(msg.id.equals("insertc")) {
			ex.execute(new Send(new Message("ins_ack",msg.key,msg.value,msg.version),SimpleDynamoProvider.port_map.get(msg.Node_id)));
			ContentValues values = new ContentValues();
			values.put(myHelper.KEY_FIELD, msg.key);
			values.put(myHelper.VALUE_FIELD, msg.value);
			values.put(myHelper.VERSION_FIELD, Integer.toString(msg.version));
			SimpleDynamoActivity.mContentResolver.insert(SimpleDynamoActivity.CONTENT_URI, values);
		} else if(msg.id.equals("ins_ack")) {
			try {
				SimpleDynamoProvider.block_ins.put(msg.version);
			} catch (InterruptedException e) {
				Log.e(TAG,e.getMessage());
			}
		} else if(msg.id.equals("replica")) {
			ContentValues values = new ContentValues();
			values.put(myHelper.KEY_FIELD, msg.key);
			values.put(myHelper.VALUE_FIELD, msg.value);
			values.put(myHelper.VERSION_FIELD, Integer.toString(msg.version));
			SimpleDynamoActivity.mContentResolver.insert(DUP_CONTENT_URI, values);
		} else if(msg.id.equals("query")) {
			Cursor c= SimpleDynamoActivity.mContentResolver.query(SimpleDynamoActivity.CONTENT_URI, null, msg.key, null, "ins");
			if(c!=null) {
				String result = c.getString(c.getColumnIndex(myHelper.VALUE_FIELD));
				ex.execute(new Send(new Message("q_reply",msg.key, result, 0),(Integer.parseInt(msg.Node_id)*2)));
			}
		} else if(msg.id.equals("vote")) {
			ex.execute(new Send(new Message("ins_ack",null,null,0),SimpleDynamoProvider.port_map.get(msg.Node_id)));
		} else if(msg.id.equals("join")) {
			if(SimpleDynamoProvider.fail_map.size() == 3 ) {
				HashMap<String, String[]> map = new HashMap<String,String[]>();
				Cursor c= SimpleDynamoActivity.mContentResolver.query(SimpleDynamoActivity.CONTENT_URI, null, null, null, "local");
				if(c!= null && c.moveToFirst()) {
					while(!c.isAfterLast()) {
						int keyIndex = c.getColumnIndex("key");
		    	        int valueIndex = c.getColumnIndex("value");
		    	    	String returnKey = c.getString(keyIndex);
		    	        String returnValue = c.getString(valueIndex);
		    	        int version = c.getInt(c.getColumnIndex(myHelper.VERSION_FIELD));
		    	        String arr[] = {returnValue,Integer.toString(version)};
		    	        map.put(returnKey, arr);
		    	        c.moveToNext();
					}
					ex.execute(new Send(new Message("recover",map),(Integer.parseInt(msg.Node_id))*2));
				}
			}
		} else if(msg.id.equals("recover")) {
			provider.recovery(msg.map, msg.version);
		}
	}
}