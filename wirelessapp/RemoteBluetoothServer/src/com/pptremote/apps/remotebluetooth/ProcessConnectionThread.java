package com.pptremote.apps.remotebluetooth;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.microedition.io.StreamConnection;
import org.apache.poi.hssf.*;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;


public class ProcessConnectionThread implements Runnable{

	private StreamConnection mConnection;
	
	// Constant that indicate command from devices
	private static final int EXIT_CMD = -1;
	private static final int KEY_RIGHT = 1;
	private static final int KEY_LEFT = 2;
	private static final int F5 = 4;
	private static final int Go_To = 5;
	private static final int ESC = 6;
	
	public ProcessConnectionThread(StreamConnection connection)
	{
		mConnection = connection;
	}
	/*public ProcessConnectionThread()
	{
		
	}*/
	public int getTotalSlideNo()
	{
		File file= new File("C:/Users/Durga Prasanna Kumar/Downloads/iteration2_team4 (2)/iteration2_team4.ppt");
		//File file1 = new File("C:/Users/NANI/Desktop/Lab-Team22.ppt");
	    FileInputStream f1;
	    //FileInputStream is1;
		try {
			f1 = new FileInputStream(file);
			SlideShow ppt = new SlideShow(f1);
		    f1.close();
		    Slide[] slide = ppt.getSlides();
		  
		    return(slide.length);
		} catch (FileNotFoundException e) {
			// Todo Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return(0);
	}
	public void callGlobalKeyListener(OutputStream outputStream)
	{
		try {
            GlobalScreen.registerNativeHook();
    }
    catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
    }

    //Construct the example object and initialze native hook.
    GlobalScreen.getInstance().addNativeKeyListener(new GlobalKeyListenerExample(outputStream));
	}
	
	@Override
	public void run() {
		try {
			
			// prepare to receive data
			InputStream inputStream = mConnection.openInputStream();
			OutputStream outputStream=mConnection.openOutputStream();
	        
			System.out.println("waiting for input");
			callGlobalKeyListener(outputStream);
			outputStream.write(getTotalSlideNo());
			boolean isGoTo = false;
	        while (true) {
	        	
	        	int command = inputStream.read();
	        	
	        	//GlobalKeyListenerExample globalKeyListenerExample = new GlobalKeyListenerExample();
	        	
	        	//globalKeyListenerExample.na
	        	if (command == EXIT_CMD)
	        	{	
	        		System.out.println("finish process");
	        		break;
	        	}
	        	if(isGoTo)
	        	{
	        		goToSlide(command);
	        		isGoTo=false;
	        	}
	        	else if(command == Go_To)
	        	{
	        		isGoTo=true;
	        	}
	        	else processCommand(command);
        	}
	        outputStream.close();
        } catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	public void goToSlide(int command) throws AWTException
	{
		HashMap<Integer,Integer> digitKey= new HashMap();
		digitKey.put(0, KeyEvent.VK_0);
		digitKey.put(1, KeyEvent.VK_1);
		digitKey.put(2, KeyEvent.VK_2);
		digitKey.put(3, KeyEvent.VK_3);
		digitKey.put(4, KeyEvent.VK_4);
		digitKey.put(5, KeyEvent.VK_5);
		digitKey.put(6, KeyEvent.VK_6);
		digitKey.put(7, KeyEvent.VK_7);
		digitKey.put(8, KeyEvent.VK_8);
		digitKey.put(9, KeyEvent.VK_9);
		System.out.println("command is"+command);
		String slideNo=String.valueOf(command);
		String[] digits = slideNo.split("(?<=.)");
		System.out.println("slide No is=="+slideNo);
		Robot robot = new Robot();
		for(int digitIndex=0;digitIndex<digits.length;digitIndex++)
		{
			
			System.out.println("key digit="+digitKey.get(Integer.parseInt(digits[digitIndex])));
			robot.keyPress(digitKey.get(Integer.parseInt(digits[digitIndex])));
		}
		robot.keyPress(KeyEvent.VK_ENTER);
		// release the key after it is pressed. Otherwise the event just keeps getting trigged	    		
		for(int digitIndex=0;digitIndex<digits.length;digitIndex++)
		{
			
			robot.keyRelease(digitKey.get(Integer.parseInt(digits[digitIndex])));
		}
		robot.keyRelease(KeyEvent.VK_ENTER);
	}
	/**
	 * Process the command from client
	 * @param command the command code
	 */
	private void processCommand(int command) {
		try {
			Robot robot = new Robot();
			System.out.println("command is :"+command);
			switch (command) {
	    	case KEY_RIGHT:
	    		robot.keyPress(KeyEvent.VK_RIGHT);
	    		System.out.println("Right");
	    		
	    		// release the key after it is pressed. Otherwise the event just keeps getting trigged	    		
	    		robot.keyRelease(KeyEvent.VK_RIGHT);
	    		break;
	    	case KEY_LEFT:
	    		robot.keyPress(KeyEvent.VK_LEFT);
	    		System.out.println("Left");
	    		// release the key after it is pressed. Otherwise the event just keeps getting trigged	    		
	    		robot.keyRelease(KeyEvent.VK_LEFT);
	    		break;
	    	case F5:
	    		robot.keyPress(KeyEvent.VK_F5);
	    		System.out.println("F5");
	    		// release the key after it is pressed. Otherwise the event just keeps getting trigged	    		
	    		robot.keyRelease(KeyEvent.VK_F5);
	    		break;
	    	case Go_To:
	    		/*robot.keyPress(KeyEvent.VK_1);
	    		robot.keyPress(KeyEvent.VK_1);
	    		robot.keyPress(KeyEvent.VK_ENTER);
	    		// release the key after it is pressed. Otherwise the event just keeps getting trigged	    		
	    		robot.keyRelease(KeyEvent.VK_3);
	    		robot.keyRelease(KeyEvent.VK_ENTER);*/
	    		break;
	    	case ESC:
	    		robot.keyPress(KeyEvent.VK_ESCAPE);
	    		// release the key after it is pressed. Otherwise the event just keeps getting trigged	    		
	    		robot.keyRelease(KeyEvent.VK_ESCAPE);
	    		break;
			}
		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}
	}
}
