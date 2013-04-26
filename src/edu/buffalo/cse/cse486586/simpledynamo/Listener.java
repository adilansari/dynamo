package edu.buffalo.cse.cse486586.simpledynamo;

import java.util.concurrent.*;
import java.net.*;
import java.io.*;

import android.util.Log;

class Listener implements Runnable {

	static final String TAG = "adil listen";
	static final int recvPort= 10000;
	ExecutorService e= Executors.newFixedThreadPool(2);

	public void run() {
		Socket sock1= null;
		ObjectInputStream in =null;
		ServerSocket servSocket= null;
		try {
			servSocket= new ServerSocket(recvPort);
			Log.v(TAG, "Server Socket port: "+Integer.toString(servSocket.getLocalPort()));
		} catch (IOException e) {
			Log.e(TAG, ""+e.getMessage());
		}

		while(true) {
			try {
				sock1= servSocket.accept();
				in =new ObjectInputStream(sock1.getInputStream());
				Message obj;
				try {
					obj = (Message) in.readObject();
					e.execute(new Receiver(obj)); //replace where to send this object
				} catch (ClassNotFoundException e) {
					Log.e(TAG, e.getMessage());
				}
			} 

			catch (IOException e) {
				Log.e(TAG, ""+e.getMessage());
				e.printStackTrace();
			}
			finally {
				if (in!= null)
					try {
						in.close();
					} catch (IOException e) {
						Log.e(TAG, ""+e.getMessage());
					}
				if(sock1!=null)
					try {
						sock1.close();
					} catch (IOException e) {
						Log.e(TAG, ""+e.getMessage());
					}	
			}
		}
	}
}