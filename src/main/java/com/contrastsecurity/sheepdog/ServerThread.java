package com.contrastsecurity.sheepdog;

import java.util.ArrayList;
import java.util.List;


public class ServerThread extends Thread {

    public static void main( String[] args ) throws Exception {
        
        int servers = 0;
        try {
            servers = Integer.parseInt( args[0] );
        } catch( Exception e ) {}

        List<String> urls = new ArrayList<String>();
        
        for ( int i=0; i<servers; i++ ) {
            String appname = "webgoat-" + i;
            String servername = "server-" + i;
            int httpPort = 8080 + (1000*i);
            int ajpPort = httpPort+10;
            urls.add("http://localhost:"+httpPort+"/WebGoat/");

            String cmd = "/usr/bin/java -javaagent:contrast.jar " +
                         "-Xmx2g -Dcontrast.dir=working -Dcontrast.appname=\""+appname+"\" " +
                         "-Dcontrast.server=\""+servername+"\" -Dcontrast.container=\"glassfish4\" " +
                         "-Dcsrf.allowed.urls=csrf-safelist.txt -Dcontrast.path=/\""+appname+"\" " +
                         "-jar webgoat-container-7.0.1-war-exec.jar " +
                         "-httpPort " + httpPort + " -ajpPort " + ajpPort;
            
            System.out.println( ">> " + cmd );            
            Process p = Runtime.getRuntime().exec( cmd );
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR-" + i);            
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT-"+i);
            errorGobbler.start();
            outputGobbler.start();
            while ( !errorGobbler.isFound() ) {
                System.out.println( "Waiting for " + servername+ ":" + httpPort + " to start" );
                Thread.sleep( 5000 );
            }
        }    
    }

    
}
