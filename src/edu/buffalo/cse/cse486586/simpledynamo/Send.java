package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import android.util.Log;

public class Send implements Runnable {
	Socket sock= null;
	Message o;
	int port;
	static final String TAG="adil send";
	ObjectOutputStream out;
	
	Send(Message m, int port) {
		this.o= m;
		this.port = port;
	}
	
	public void run() {
		try {
			this.sock= new Socket("10.0.2.2",port);
			out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(o);
		} catch (IOException ioe) {
			Log.w(TAG, "io exception");
		} finally {
			if(out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					Log.w(TAG, "Output Stream null");
				}
			}
			if (sock != null) {
				try {
					sock.close();
				} catch (IOException e) {
					Log.w(TAG, "Socket is null");
				}
			}
		}
	}
}