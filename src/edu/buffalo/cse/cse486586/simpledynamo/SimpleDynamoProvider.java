package edu.buffalo.cse.cse486586.simpledynamo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class SimpleDynamoProvider extends ContentProvider {
	
	private String Node_id;
	private myHelper myDb;
	private SQLiteDatabase db;
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
		
		return null;
	}

	
	public boolean onCreate() {
		if(Node_id == null)
			Node_id = SimpleDynamoActivity.get_node_id();
		
		return false;
	}

	
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if(Node_id == null)
			Node_id = SimpleDynamoActivity.get_node_id();
		
		return null;
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
