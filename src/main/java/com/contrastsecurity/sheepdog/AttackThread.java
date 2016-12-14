package com.contrastsecurity.sheepdog;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.StopWatch;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class AttackThread extends Thread {

    private CloseableHttpClient httpclient;
    private CookieStore cookieStore;
    private StopWatch watch;
    
    /* Scan settings */
    private String baseUrl;
    private String address;
    private List<String> lessons;
    private int scanDuration;
    private int attackPercent;
    private long requestDelay;
	private boolean verbose;
	
	/* Statistics */
	private int requestCount;
	private long totalScanTime;
	private double highest;
	private double lowest;
	
    public AttackThread( String baseUrl, String address, int scanDuration, long requestDelay, int attackPercent, boolean verbose ) {
        this.baseUrl = baseUrl;
        this.address = address;
        this.scanDuration = scanDuration;
        this.requestDelay = requestDelay;
        this.attackPercent = attackPercent;
        this.verbose = verbose;
        
        this.highest = -1;
        this.lowest = -1;
        
        this.watch = new StopWatch();
        		
        try {
            cookieStore = new BasicCookieStore();
            RequestConfig globalConfig = RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.DEFAULT)
                    .build();
            httpclient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setDefaultRequestConfig(globalConfig)
                    .build();
            
            // login and get list of lessons
            List<NameValuePair> credentials = new ArrayList<NameValuePair>();
            credentials.add(new BasicNameValuePair("username", "guest"));
            credentials.add(new BasicNameValuePair("password", "guest"));

            sendPost( "j_spring_security_check", credentials );
            sendGet( "welcome.mvc", false );
            
            String json = sendGet( "service/lessonmenu.mvc", false );        
            lessons = parseLessons( json );
            
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }
    
    public void run() {
        long start = System.currentTimeMillis();
        while( System.currentTimeMillis() - start < scanDuration * 1000 ) {
            try {
                long delay = this.requestDelay != -1 ? this.requestDelay : RANDOM.nextInt(1000);
                if(delay > 0) {
                	Thread.sleep( delay );
                }
                String page = lessons.get( RANDOM.nextInt( lessons.size() ) );
                String[] parts = page.split( "/" );
                String lesson = "attack?Screen=" + parts[1] + "&menu=" + parts[2];
                if ( parts.length > 3 ) {
                    lesson += "&stage=" + parts[3];
                }
                String form = sendGet( lesson, false );
                scan( lesson, form, attackPercent );
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }
    
    private void scan(String lesson, String form, int attackPercent ) throws Exception {
        List<NameValuePair> fields = parseForm( form );
        boolean attack = RANDOM.nextBoolean();
        permute( fields, attack, attackPercent );
        sendPost( lesson, fields );
    }
    
    private void permute(List<NameValuePair> fields, boolean attack, int attackPercent ) {
        for ( int i=0; i<fields.size(); i++ ) {
            NameValuePair field = fields.get( i );
            String value = field.getValue();
            String newValue = value;
            newValue = getToken();
            if ( RANDOM.nextInt( 100 ) < attackPercent ) {
                newValue = getAttack();
            }
            NameValuePair newField = new BasicNameValuePair( field.getName(), newValue );
            fields.set( i, newField );
       }
    }


    // "link": "#attack/3821/2000"
    private List<String> parseLessons(String lessons) {
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("#(attack.*?)\"").matcher(lessons);
        while (m.find()) {
            String page = m.group();
            if(verbose) {
            	System.out.println(">>>>" + page );
            }
            allMatches.add(page.substring(1,page.length()-1));
        } 
        return allMatches;
    }

    
    
    public String sendGet(String url, boolean xhr ) throws Exception {
        HttpGet httpGet = new HttpGet(baseUrl + url);
//        System.out.println( "SENDING: " + httpGet.getURI() );
        httpGet.addHeader("X-Forwarded-For", address );
        if ( xhr ) {
            httpGet.addHeader("X-Requested-With","XMLHttpRequest");
        }
        watch.reset();
        watch.start();
        CloseableHttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity);
        watch.stop();
        long elapsed = watch.getTime();
        checkHighestLowest(elapsed);
		this.totalScanTime += elapsed;
        this.requestCount++;
        response.close();
        return content;
    }

    
    
    public String sendPost(String url, List<NameValuePair> fields ) throws Exception {
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(fields, Consts.UTF_8);
        HttpPost httpPost = new HttpPost(baseUrl + url);
        if(verbose) {
	        System.out.println( "POST from " + address + " to " + httpPost.getURI() );
	        System.out.println( "   " + fields );
        }
        httpPost.addHeader("X-Forwarded-For", address );
        httpPost.setEntity(entity);
        
        watch.reset();
        watch.start();
        CloseableHttpResponse response = httpclient.execute(httpPost);
        String content = EntityUtils.toString(response.getEntity());
        watch.stop();
        long elapsed = watch.getTime();
        checkHighestLowest(elapsed);
		totalScanTime += elapsed;
        response.close();
        if(verbose) {
	        System.out.println( "   " + response.getStatusLine() );
	        System.out.println();
        }
        requestCount++;
        return content;
    }
    
    private void checkHighestLowest(long elapsed) {
		if(highest == -1 || elapsed > highest) {
			highest = elapsed;
		}
		if(lowest == -1 || elapsed < lowest) {
			lowest = elapsed;
		}
	}
    
    public long getTotalScanTime() {
		return totalScanTime;
	}
    
    public int getRequestCount() {
		return requestCount;
	}
    
    private static String getAttack() {
        return FRAGS[ RANDOM.nextInt(FRAGS.length) ];
    }
    
	private static List<NameValuePair> parseForm( String content ) {
        List<NameValuePair> fields = new ArrayList<NameValuePair>();
        int formStart = content.indexOf( "<form" );
        int formStop = content.indexOf( "</form>" );
        if ( formStart != -1 && formStop != -1 ) {
            String formContent = content.substring( formStart, formStop );
            String[] tags = formContent.split( ">");
            for ( String tag: tags ) {
                tag = tag.trim();
                if ( tag.startsWith ("<input" ) && tag.endsWith( "checked" ) ) {
                    NameValuePair nvp = parseAttribute( tag );
                    fields.add( nvp );
                } else if ( tag.startsWith( "<textarea" ) || ( tag.startsWith( "<input" ) && !tag.contains( "checkbox" ) ) ) {
                    NameValuePair nvp = parseAttribute( tag );
                    fields.add( nvp );
                } else if ( tag.startsWith ("<option" ) && tag.endsWith( "selected" ) ) {
                    NameValuePair nvp = parseAttribute( tag + " name=\"vector\"" );
                    fields.add( nvp );
               } else if ( tag.startsWith( "<" ) && !tag.startsWith( "</" ) && !tag.startsWith( "<div" ) && !tag.startsWith( "<label" )
                        && !tag.startsWith( "<br") && !tag.startsWith( "<p") && !tag.startsWith( "<img") && !tag.startsWith( "<h5")  ) {
               }
            }
        }
        return fields;
    }

    private static NameValuePair parseAttribute(String tag) {
        String name = "";
        int nameStart = tag.indexOf( "name=" );
        if ( nameStart != -1 ) {
            int nameStop = tag.indexOf( "\'", nameStart+6 );
            if ( nameStop == -1 ) {
                nameStop = tag.indexOf( "\"", nameStart+6 );
            }
            if ( nameStop == -1 ) {
                nameStop = tag.indexOf( " ", nameStart+6 );
            }
            name = tag.substring(nameStart+6, nameStop);
        }
        
        String value = "default";
        int valueStart = tag.indexOf("value=" );
        if ( valueStart != -1 ) {
            int valueStop = tag.indexOf( "\'", valueStart+7 );
            if ( valueStop == -1 ) {
                valueStop = tag.indexOf( "\"", valueStart+7 );
            }
            if ( valueStop == -1 ) {
                valueStop = tag.indexOf( " ", nameStart+7 );
            }
            value = tag.substring(valueStart+7, valueStop);
        }
        return new BasicNameValuePair( name, value );
    }

    private static String getToken() {
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < 5; i++ ) {
            sb.append( (char)(RANDOM.nextInt(26) +'a' ) ); 
        }
        for ( int i = 0; i< 3; i++ ) {
            sb.append( (char)(RANDOM.nextInt(10) + '0' ) );
        }
        return sb.toString();
    }
    
    private static SecureRandom RANDOM = new SecureRandom();
    private static String[] FRAGS = {
        "' onmouseover='alert(" + getToken() + ")",
        "\" onmouseover=\"alert(" + getToken() + ")",
        "' or 112=112--",
        "' or 1+2=3 --",
        "' or '1'+'2'='12",
        "><script>alert(1)</script>",
        "../../../../../foo.bar%00",
        "..\\..\\..\\..\\..\\etc\\passwd"
    };
}
