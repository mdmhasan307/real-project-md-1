package comapplet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JApplet;

public class SetupCOMApplet extends JApplet {
    private int serverPort = 80;
    private String serverHost = "10.1.1.1";
    private String serverProtocol = "http";
    private String serverContextPath = "/Stratis";
    private boolean debug = true;

    public SetupCOMApplet() {
    }

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(null);
    }
    
    public void init() {

        showDebug( "init()" );
    
        String path = null;
        
        try {
            //* initialize the parameters
            debug = getParameter("debug").equalsIgnoreCase("true");
            serverProtocol = getParameter("serverProtocol").startsWith("https") ? "https" : "http";
            serverPort = Integer.parseInt(getParameter("serverPort"));
            serverHost = getParameter("serverHost");
            serverContextPath = getParameter("serverContextPath");
            
            // We are going to try and copy them to both locations.
            
            path = System.getProperty( "java.home" );
            
            showDebug( "calling copyURL with: " + path );
            copyURL(path, "bin/", "win32com.dll");
            copyURL(path, "lib/", "javax.comm.properties");
            copyURL(path, "lib/ext/", "comm.jar");
            
            path = System.getProperty( "java.io.tmpdir" );
            if( path == null ) {
                path = System.getProperty( "user.home" );
            }
            
            showDebug( "calling copyURL with: " + path );
            copyURL(path,"", "win32com.dll");
            copyURL(path,"", "javax.comm.properties");
            copyURL(path,"", "comm.jar");
            
            //* initialize the content pane
            jbInit();
        } catch (Exception e) {
            System.out.println("initialize exception");
            e.printStackTrace();
        }
    }
    
    private void showDebug( String str ) {
        
        if( debug ) {
            System.out.println( "SetupCOMApplet::" + str );
            System.out.flush();
        }
    }
    
    private void copyURL( String basePath, String filepath, String filename) {
    
        showDebug( "copyURL()" );
        boolean fileExists = false;
                 
        // Check if the file is already on the target system.
        
        fileExists = false;
        
        try {
            File f = new File( basePath + "/" + filepath + filename);
            if (f.exists()) fileExists = true;
            System.out.println( "fileExists: " + fileExists );
        } catch (Exception fnfe) {
            fileExists = false;
        }        
        
        try {
            if (!fileExists) {    
                String path = serverContextPath + "/applet/" + filename;
 
                if (debug) {
                    showDebug( "serverContextPath: " + serverContextPath );
                    showDebug( "server host : " + serverHost );
                    showDebug( "server port : " + serverPort );
                    showDebug( "path        : " + path );
                    showDebug( "server proto: " + serverProtocol );
                }
                        
                URL url = new URL(serverProtocol, serverHost, serverPort, path);    
                
                if( debug ) {
                    showDebug( "url host: " + url.getHost());
                    showDebug( "url path: " + url.getPath() );                    
                    showDebug( "url getFile: " + url.getFile() );
                    showDebug( "url getUserInfo: " + url.getUserInfo() );
                }
                
                URLConnection urlC = url.openConnection();
                
                // Copy resource to local file, use remote file
                // if no local file name specified
                InputStream is = url.openStream();
    
                String fileSpec = basePath + "/" + filepath + filename;
                
                if( debug ) {
                    showDebug( "Copying resource (type: " + 
                        urlC.getContentType() + " )");
                    showDebug( "File Spec: "+ fileSpec );                
                    showDebug( "Base Path: " + basePath );                
                    
                }
                
                
                int oneChar, count = 0;
                if( debug ) {
                    System.out.println( "Entering copy loop >>" );
                }
                try (FileOutputStream fos = new FileOutputStream( fileSpec )) {
                    while ((oneChar = is.read()) != -1) {
                        fos.write(oneChar);
                        count++;
                    }
                }
                is.close();
                System.out.println(count + " byte(s) copied");
                if( debug ) {
                    System.out.println( "<< After copy loop" );
                }
            }
        } catch (MalformedURLException e) {
            System.err.println(e.toString());
        } catch (IOException ie) {
            System.err.println(ie.toString());
        }
    }

}
