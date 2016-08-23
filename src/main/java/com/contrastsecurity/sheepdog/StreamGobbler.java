package com.contrastsecurity.sheepdog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StreamGobbler extends Thread {
    InputStream is;
    String type;
    boolean found = false;
    
    StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if ( line.startsWith( "INFO: Starting ProtocolHandler [\"http-bio-" ) ) {
                    found = true;
                }
                System.out.println(type + ">" + line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public boolean isFound() {
        return found;
    }
}