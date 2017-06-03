package com.pptremote.apps.remotebluetooth;
import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalKeyListenerExample implements NativeKeyListener {
	  	OutputStream outputStream1;
	  	public GlobalKeyListenerExample(OutputStream outputStream)
		{
			outputStream1=outputStream;
			
		}
	    public void nativeKeyPressed(NativeKeyEvent e) {
                //System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                setKeyPressed(NativeKeyEvent.getKeyText(e.getKeyCode()));
              
        }
        
        public void nativeKeyReleased(NativeKeyEvent e) {
                System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        }

        public void nativeKeyTyped(NativeKeyEvent e) {
                System.out.println("Key Typed: " + e.getKeyText(e.getKeyCode()));
        }
        public void setKeyPressed(String keyPressed)
        {
        	int key=0;
        	if(keyPressed == "Right")
        		key=-1;
        	else if(keyPressed == "Left")
        		key=-2;
        	try {
        		System.out.println("key is=="+key);
        		outputStream1.write(key);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }
       
}
