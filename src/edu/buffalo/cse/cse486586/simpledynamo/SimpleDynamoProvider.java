package edu.buffalo.cse.cse486586.simpledynamo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class SimpleDynamoProvider extends ContentProvider {
	
	private String Node_id;
	private myHelper myDb;
	private SQLiteDatabase db;
	private static LinkedList list;
	private static ExecutorService Pool = Executors.newFixedThreadPool(3);
	private static SortedMap<String, String> map = new TreeMap<String, String>();
	public String[] nodes = {"5554","5556","5558"};
	static final String TAG= "adil provider";
	private static final String AUTHORITY = "edu.buffalo.cse.cse486586.simpledynamo.provider";
	private static final String BASE_PATH = myHelper.TABLE_NAME;
	public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/" + BASE_PATH);
	
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if(Node_id == null)
			Node_id = SimpleDynamoActivity.get_node_id();
		
		return 0;
	}
	
	public String getType(Uri uri) {
		return null;
	}

	
	public Uri insert(Uri uri, ContentValues values) {
		if(Node_id == null)
			Node_id = SimpleDynamoActivity.get_node_id();
		
		
		
		
		db = myDb.getWritableDatabase();
		long rowId= db.replace(myHelper.TABLE_NAME, myHelper.VALUE_FIELD, values);
		if (rowId > 0) {
			Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(newUri, null);
			Log.i(TAG, "Insertion success # " + Long.toString(rowId));
			return newUri;
		} else {
			Log.e(TAG, "Insert to db failed");
		}
		return null;
	}
	
	public String getNode(String Key) {
		String result = null;
		try {
			String hashKey = genHash(Key);
			String leader = map.firstKey();
			for(Map.Entry<String, String> entry: map.entrySet()) {
				String node = entry.getValue();
				String hashNode = genHash(node);
				String prev = list.get(node).prev.data;
				String hashPre = genHash(prev);
				if(hashKey.compareTo(hashNode) <= 0 && hashKey.compareTo(hashPre) > 0)
	    			result = node;
	    		else if(node.equals(leader) && (hashKey.compareTo(hashNode) <= 0 || hashKey.compareTo(hashPre) > 0))
	    			result = node;
			}
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "Hash Fail");
		}
		return result;
	}

	public boolean onCreate() {
		Log.v(TAG, "provider created");
		myDb = new myHelper(getContext());
		myDb.getWritableDatabase();
		updateDataStruct();
		return true;
	}
	
	public void updateDataStruct() {
		for(String n : nodes) {
			try {
				String hash = genHash(n);
				map.put(hash, n);
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG, "No such algorithm");
			}
		}
		list = new LinkedList();
    	for(Map.Entry<String, String> entry: map.entrySet()) {
    		list.add(entry.getValue());
    	}
	}

	
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if(Node_id == null)
			Node_id = SimpleDynamoActivity.get_node_id();
		Cursor c = null;
		if(selection != null)
			c= db.rawQuery("select * from "+myHelper.TABLE_NAME+" where key like '"+selection+"'", null);
		else
			c= db.rawQuery("select * from "+myHelper.TABLE_NAME, null);
		return c;
	}

	
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		return 0;
	}

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
