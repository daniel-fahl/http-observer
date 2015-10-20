/*
 * Project: FortisIT Bewerbung
 * Author:  Daniel Fahl
 */

import java.io.*;
import java.util.*;
import java.text.*;

import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

/**
 * This class monitors a web server by checking the HTTP status code every
 * 30 seconds. The URL can be entered at run time. Monitoring runs
 * until any input is received / the enter key is pressed.
 * Since it uses the HTTP status codes, you could further specify the
 * logging output, e.g. distinguish 500-Server Error from 404-Not Found.
 * It is run from the command line.
 * 
 * Be careful with multithreading, since the logging method is not
 * thread-safe!
 */
public class Monitoring implements Runnable {

    volatile boolean running = true;

    static String url = "http://www.example.org/";
    static String logfile = "Monitoring.log";

    public static void main( String[] args ) {

        InputStreamReader isr = new InputStreamReader( System.in );
        BufferedReader br = new BufferedReader( isr );

        // Read the URL that needs to be monitored
        System.out.println("Bitte geben Sie die zu überwachende URL ein und bestätigen Ihre Eingabe mit der Enter-Taste!");
        try{
            url = br.readLine();
        } catch( IOException e ) {
            System.out.println("Fehler beim Einlesen der URL. Bitte versuchen Sie es erneut!");
            System.exit(1);
        }
        System.out.println( url + " wird überwacht, drücken Sie die Eingabetaste um das Programm zu beenden..." );

        // Start monitoring the URL in another thread
        Monitoring monitor = new Monitoring();
        Thread t = new Thread( monitor );
        t.start();

        // Check for user input in this thread
        Scanner scanner = new Scanner(System.in);
        while( !scanner.hasNext() ) {
            // do nothing, wait for input...
        }
        // Stop execution on input
        monitor.running = false;
        t.interrupt();

        // cleanly close all streams
        try {
            isr.close();
            br.close();
        } catch( Exception e ) {
            // DO NOTHING
        }

    }

    /**
     * This method contains the actual URL request.
     * Here you could add further control over the response codes.
     */
    public void run() {

        while( running ) {
            int statuscode =  getHttpStatusCode( url );
            String loggingLine;

            if( statuscode >= 200 && statuscode < 300 ) {
                loggingLine = getCurrentTimeString() + " : " + url + " -> erreichbar!";
            } else {
                loggingLine = getCurrentTimeString() + " : " + url + " -> nicht erreichbar!";
            }

            //System.out.println( loggingLine );
            logToFile( loggingLine, logfile );

            try {
                Thread.sleep( 30000 );
            } catch (InterruptedException e) {
                // DO NOTHING
            }

        }

    }

    /**
     * This method executes a HTTP-GET request and returns the HTTP status code
     * (like "404-Not Found" or "500-Internal Server Error") as a numeric value.
     * It does not natively support HTTP-POST or setting custom header fields.
     * If anything goes wrong with this request, it returns -1.
     * @param url - the URL that needs to be requested
     * @return the HTTP status code
     */
    public int getHttpStatusCode( String url ) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet( url );
        CloseableHttpResponse response = null;
        int statuscode = -1;

        try {
            response = httpclient.execute( httpget );
            statuscode = response.getStatusLine().getStatusCode();
        } catch( Exception e ) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
                response.close();
            } catch( Exception e ) {
                // DO NOTHING
            }
        }

        return statuscode;
    }

    /**
     * The method <code>logToFile</code> takes a String parameter containing
     * a line of logging information and appends it to a filename given as
     * the second parameter.
     * If the file does not exist, it is created in the current working
     * directory. If it does exist, the content is appended to it.
     * @param line - the text that needs to be logged
     * @param filename - the name of the log file
     */
    public void logToFile( String line, String filename ){
        BufferedWriter out = null;
        try {
            File dir = new File(".");
            String filepath = dir.getCanonicalPath() + File.separator + filename;

            FileWriter fstream = new FileWriter(filepath, true);
            out = new BufferedWriter(fstream);

            out.write( line );
            out.newLine();
        } catch( IOException e ) {
            System.out.println("Die Log-Ausgabe ist fehlgeschlagen.");
        } finally {
            //close buffer writer
            try {
                out.close();
            } catch( Exception e ) {
                // Do nothing
            }
        }
    }

    /**
     * This method returns the current time as a String of the format
     * "dd-MMM-yyyy HH:mm:ss".
     * @return a String representation of the current time
     */
    public String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        Date now = new Date();
        return sdf.format( now );
    }

}

