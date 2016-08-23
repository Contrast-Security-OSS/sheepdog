package com.contrastsecurity.sheepdog;

import java.security.SecureRandom;


public class Sheepdog {

    private static SecureRandom sr = new SecureRandom();

    // usage: java Sheepdog #attackThreads #duration #rate
    public static void main(String[] args) throws Exception {
        
        System.out.println( "Usage: java -jar sheepdog.jar #threads #durationMins #ratePerMin #attackPercent port");
        
        int threads = 3;
        try {
            threads = Integer.parseInt( args[0] );
        } catch( Exception e ) {}

        int duration = 60;
        try {
            duration = Integer.parseInt( args[1] );
        } catch( Exception e ) {}

        int rate = 30;
        try {
            rate = Integer.parseInt( args[2] );
        } catch( Exception e ) {}

        int attackPercent = 50;
        try {
            attackPercent = Integer.parseInt( args[3] );
        } catch( Exception e ) {}
        
        String baseUrl = "http://localhost:";
        int port = 8080;
        try {
            port = Integer.parseInt( args[4] );
        } catch( Exception e ) {}
        baseUrl += port;
        baseUrl += "/WebGoat/";
        
        
        // Starting server threads (TBD) -- See ServerThread.java
        

        
        System.out.println( "Starting "+ threads +" attack threads, each with:" );
        System.out.println( "  " + duration + " minute duration" );
        System.out.println( "  " + rate + " requests per minute" );
        System.out.println( "  " + attackPercent + "% attack parameters" );
        System.out.println( "  " + "target: " + baseUrl );
        System.out.println();
        
        String[] addresses = new String[threads];
        for ( int i=0; i<threads; i++ ) {
            addresses[i] = getRandomAddress();
            System.out.println( "  Starting AttackThread (" + addresses[i] +")" );
        }
        System.out.println();
        
        for ( String address : addresses ) {
            AttackThread t = new AttackThread( baseUrl, address, duration, rate, attackPercent );
            t.start();
        }           
     }
  
    private static String getRandomAddress() {
        StringBuilder sb=new StringBuilder();
        sb.append( sr.nextInt(256) );
        sb.append( "." );
        sb.append( sr.nextInt(256) );
        sb.append( "." );
        sb.append( sr.nextInt(256) );
        sb.append( "." );
        sb.append( sr.nextInt(256) );
        return sb.toString();
    }

    
}
