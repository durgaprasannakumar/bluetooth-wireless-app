package com.pptremote.apps.remotebluetooth;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;
public class RemoteBluetoothServer{
	
	public static void main(String[] args) {
		Thread waitThread = new Thread(new WaitThread());
		
		waitThread.start();
	}
}
