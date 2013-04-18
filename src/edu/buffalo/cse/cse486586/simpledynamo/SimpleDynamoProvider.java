package edu.buffalo.cse.cse486586.simpledynamo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class SimpleDynamoProvider extends ContentProvider {
	
	private static String Node_id;
	private static myHelper myDb;
	private static SQLiteDatabase db;
	private static LinkedList list;
	public String[] nodes = {"5554","5556","5558"};
	static final String TAG= "adil provider";
	private static ExecutorService Pool = Executors.newFixedThreadPool(3);
	private static SortedMap<String, String> map = new TreeMap<String, String>();
	public static Map<String , Integer> port_map = new HashMap<String, Integer>();
	public static ArrayBlockingQueue<Integer> block_ins = new ArrayBlockingQueue<Integer>(1);
	private static final String AUTHORITY = "edu.buffalo.cse.cse486586.simpledynamo.provider";
	private static final String BASE_PATH = myHelper.TABLE_NAME;
	public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/" + BASE_PATH);
	private static SimpleDynamoProvider obj= new SimpleDynamoProvider();
	
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if(Node_id == null)
			Node_id = SimpleDynamoActivity.get_node_id();
		
		return 0;
	}
	
	public static SimpleDynamoProvider getInstance(){
		return obj;
	}
	
	public void insertRequest(String key, String value, int version) throws InterruptedException {
		if(Node_id == null)
			Node_id = SimpleDynamoActivity.get_node_id();
		String cord = getNode(key);
		if(cord.equals(Node_id)) {
			ContentValues _cv = new ContentValues();
			_cv.put(myHelper.KEY_FIELD, key);
			_cv.put(myHelper.VALUE_FIELD, value);
			_cv.put(myHelper.VERSION_FIELD, Integer.toString(version));
			insert(CONTENT_URI,_cv);
		} else {
			Pool.execute(new Send(new Message("insertc",key,value,version),port_map.get(cord)));
			boolean ins_suc= block_ins.poll(1200, TimeUnit.MILLISECONDS) != null;
			if(!ins_suc) {
				Log.i(TAG, "Timeout");
				replicate(cord, key, value,version);
			}
		}
		//failure/recovery
	}
	
	public void replicate(String node, String key, String value, int version) {
		Pool.execute(new Send(new Message("replica",key,value,version),port_map.get(list.get(node).next.data)));
		Pool.execute(new Send(new Message("replica",key,value,version),port_map.get(list.get(node).next.next.data)));
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
			//getContext().getContentResolver().notifyChange(newUri, null);
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
			String leader = map.get(map.firstKey());
			for(Map.Entry<String, String> entry: map.entrySet()) {
				String node = entry.getValue();
				String hashNode = genHash(node);
				String prev = list.get(node).next.data;
				String hashPre = genHash(prev);
				if(hashKey.compareTo(hashNode) <= 0 && hashKey.compareTo(hashPre) > 0){
					result = node;	break;
				} else if(node.equals(leader) && (hashKey.compareTo(hashNode) <= 0 || hashKey.compareTo(hashPre) > 0)) {
						result = node;	break;
				}
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
		ExecutorService e= Executors.newSingleThreadExecutor();
		e.execute(new Listener());
		return true;
	}
	
	public void updateDataStruct() {
		for(String n : nodes) {
			try {
				String hash = genHash(n);
				map.put(hash, n);
				port_map.put(n, Integer.parseInt(n)*2);
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
