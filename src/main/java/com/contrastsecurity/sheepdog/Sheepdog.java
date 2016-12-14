package com.contrastsecurity.sheepdog;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Sheepdog {

    private static SecureRandom sr = new SecureRandom();

    // usage: java Sheepdog #attackThreads #duration #rate
    public static void main(String[] args) throws Exception {
        
        System.out.println( "Usage: java -jar sheepdog.jar [-t -s -d -a -p -v]");
        
        Options options = new Options();
        options.addOption("t", true, "number of concurrent threads");
        options.addOption("s", true, "duration (seconds)");
        options.addOption("d", true, "delay (milliseconds, -1 for random delay)");
        options.addOption("a", true, "attack percentage (0-100)");
        options.addOption("p", true, "server port (8080 by default)");
        options.addOption("v", false, "verbose");
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);
        
        int maxThreads = readOption(cmd, "t", 3);
        int duration = readOption(cmd, "s", 60);
        int delay = readOption(cmd, "d", -1);
        int attackPercent = readOption(cmd, "a", 50);
        int port = readOption(cmd, "p", 8080);
        String baseUrl = "http://localhost:" + port + "/WebGoat/";
        
        // Starting server threads (TBD) -- See ServerThread.java
        System.out.println( "Starting " + maxThreads + " attack threads, each with:" );
        System.out.println( "  " + duration + " seconds" );
        if(delay != -1) {
        	System.out.println( "  " + delay + "ms delay between requests" );
        } else {
        	System.out.println( "  random delay between requests" );
        }
        System.out.println( "  " + attackPercent + "% attack parameters" );
        System.out.println( "  target: " + baseUrl );
        System.out.println();
        
        List<AttackThread> threads = new ArrayList<AttackThread>();
        for(int i=0;i<maxThreads;i++) {
        	String address = getRandomAddress();
        	System.out.println( "  Starting AttackThread (" + address +")" );
        	AttackThread t = new AttackThread( baseUrl, address, duration, delay, attackPercent, cmd.hasOption("v") );
            t.start();
            threads.add(t);
        }
        
        while(threadsAreRunning(threads)) {
        	sleep(3000);
        }
        
        String outputFile = cmd.getOptionValue("o");
        if(outputFile != null) {
        	System.out.println("Writing statistics to " + outputFile);
        	writeStats(threads, outputFile);
        }
     }

	private static void writeStats(List<AttackThread> threads, String outputFile) {
		List<Map<String,Object>> stats = new ArrayList<Map<String,Object>>();
		for(int i=0;i<threads.size();i++) {
			AttackThread thread = threads.get(i);
			Map<String,Object> threadStats = new HashMap<String,Object>();
			threadStats.put("count", thread.getRequestCount());
			threadStats.put("elapsed", thread.getTotalScanTime());
			stats.add(threadStats);
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(stats);
		try {
			FileUtils.write(new File(outputFile), json);
		} catch (IOException e) {
			System.err.println("Problem writing statistics to " + outputFile);
			e.printStackTrace();
		}
	}
  
    private static boolean threadsAreRunning(List<AttackThread> threads) {
		for(AttackThread t : threads) {
			if(t.isAlive()) {
				return true;
			}
		}
		return false;
	}

	private static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) { }
	}

	private static int readOption(CommandLine cmd, String flag, int defaultValue) {
    	int rc = defaultValue;
		try {
			rc = Integer.parseInt(cmd.getOptionValue(flag));
		} catch (Exception e) {
			System.err.println("Unknown value for flag '" + flag + "', using " + defaultValue);
		}
		return rc;
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
