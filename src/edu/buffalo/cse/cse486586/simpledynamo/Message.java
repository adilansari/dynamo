package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.*;
import java.util.HashMap;

import android.content.ContentValues;
import android.util.Log;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	String id, Node_id, selection, sortOrder, query_reply;
	String[] nbors;
	String key,value;
	int version;
	long rowId;
	HashMap<String, String> map;
	
	//insertc  replica ins_ack query
	Message(String id, String key, String value, int version) {
		this.id = id;
		this.key =key;
		this.value = value;
		this.version = version;
		this.Node_id = SimpleDynamoActivity.get_node_id();
	}
	
}
