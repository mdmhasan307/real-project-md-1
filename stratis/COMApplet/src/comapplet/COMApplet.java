package comapplet;


// import java.awt.PrintJob;
// import java.awt.print.PrinterJob;
// import com.sun.comm.Win32Driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;


import java.net.InetAddress;

import java.net.MalformedURLException;
import java.net.Socket;

import java.net.URL;

import java.net.URLClassLoader;

import javax.comm.CommDriver;
import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.SimpleDoc;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.standard.PrinterName;
import javax.swing.JApplet;

public class COMApplet extends JApplet {

    private String portName = "";
    private String strToPrint = "";
    private boolean ready = true;
    private boolean debug = false;
    private CommDriver commDriver;
    private String cubiscanLine = "";
    private String PARITY_STR = "";
    private int PARITY = SerialPort.PARITY_NONE;
    private int DATABITS = SerialPort.DATABITS_8;
    private int DEFAULT_DATABITS = SerialPort.DATABITS_8;
    private int STOPBITS = SerialPort.STOPBITS_1;
    private int DEFAULT_STOPBITS = SerialPort.STOPBITS_1;

    //For SCALE --
    private final int STX = 2;
    private final int ETX = 3;
    private final int CR = 13;
    private final int LF = 10;
    private final int M = 77; //For MEASURE
    private final int D = 68; //For CALIBRATE
    //---
    private SerialPort myPort;
    private Runtime rt = null;
    private Process p = null;

    /**
     * How long to wait for the open to finish up.
     */
    private int TIMEOUTSECONDS = 30;

    /**
     * The baud rate to use.
     */
    private int BAUD = 19200;

    /**
     * The chosen Port Identifier
     */
    private CommPortIdentifier thePortID;

    CommPortIdentifier commPortIdentifierClazz;


    /**
     * The chosen Port itself
     */
    private CommPort thePort;

    private PrintStream os;

    // private String DLL_FILE = "win32com.dll";
    // private String DRIVER_NAME = "com.sun.comm.Win32Driver";

    URL urls[] = {};
    JarFileLoader clazzLoader;


    public COMApplet() {
    }

    private void jbInit() throws Exception {
        getContentPane().setLayout(null);
    }

    public void init() {

        try {

            debug = getParameter("debug").equalsIgnoreCase("true");
            showDebug("init");

            try {
                //UNCOMMENT THE FOLLOWING AT RUNTIME LOCAL OC4J OTHER WISE COMENT IT
                System.setSecurityManager(null);
                String driverName = "com.sun.comm.Win32Driver";

                showDebug("calling load class for driver: " + driverName);
                Class clazz = this.getClass().getClassLoader().loadClass(driverName);
                commDriver = (CommDriver) clazz.newInstance();
                commDriver.initialize();
                if (commDriver != null) {
                    showDebug("class loaded " + commDriver.toString());
                } else {
                    showDebug("class load failed.");
                }
                ready = true;
            } catch (Exception e1) {
                ready = false;
            }

            jbInit(); // initialize the content pane

            ready = true;

        } catch (Exception e) {
            System.out.println("initialize exception");
            System.out.println(">>>");
            e.printStackTrace();
            System.out.println("<<<");
            System.out.flush();
            ready = false;
        }
    }

    private void showDebug(String str) {
        if (debug) {
            System.out.println("COMApplet::" + str);
            System.out.flush();
        }
    }

    private String getNameWithClassPath(String fileName) {
        StringBuilder buf = new StringBuilder(320);  // 16 * 20
        buf.append(System.getProperty("java.io.tmpdir"));
        buf.append("/");
        buf.append(fileName);
        return buf.toString();
    }

    private boolean isNotEmpty(String str) {
        if (str == null)
            return false;
        if (str.trim().equals(""))
            return false;
        return true;
    }

    public void start() {

        //* initialize the parameters
        debug = getParameter("debug").equalsIgnoreCase("true");
        String strPram = getParameter("str");
        String hostStr = "";
        String path = "";
        String soapStr = "";
        int soapStrLen = 0;

        showDebug("strart()");
        showDebug("strPram value ->" + strPram + "<-");


        if (strPram.contains("MEASURE") || strPram.contains("CALIBRATE")) {
            strToPrint = strPram.split(",")[0] + "," + strPram.split(",")[1];
            hostStr = strPram.split(",")[2];
            path = strPram.split(",")[3];
        } else {
            strToPrint = getParameter("str");
        }

        portName = getParameter("port");

        showDebug("port parameter: " + portName);

        if (strPram.contains("MEASURE") || strPram.contains("CALIBRATE")) {
            cubiscanLine = "";
            soapStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "    <soap:Header></soap:Header>\n" +
                    "    <soap:Body xmlns:ns1=\"http://comws.server.webservice/types/\">\n" +
                    "        <ns1:cubiScanDataElement>\n" +
                    "            <ns1:documentNumber>"
                    + "CUBI-" + strToPrint.split(",")[1].toString()
                    + "</ns1:documentNumber>\n" +
                    "            <ns1:strData>"
                    + cubiscanLine.trim()
                    + "</ns1:strData>\n" +
                    "        </ns1:cubiScanDataElement>\n" +
                    "    </soap:Body>\n" +
                    "</soap:Envelope>\n";
            soapStrLen = soapStr.length() - 4;
        } else if (portName.contains("PRINT:")) {
            PrintService psZebra = null;
            String sPrinterName = null;
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            String printerName = portName.substring(6);
            printerName = printerName.toUpperCase().trim();
            showDebug("Looking for printer named " + printerName);

            for (int i = 0; i < services.length; i++) {

                PrintServiceAttribute attr = services[i].getAttribute(PrinterName.class);
                sPrinterName = ((PrinterName) attr).getValue();
                sPrinterName = sPrinterName.toUpperCase().trim();
                if (sPrinterName.indexOf(printerName) >= 0) {
                    psZebra = services[i];
                    break;
                }
            }

            if (psZebra != null) {
                System.out.println("Printer found: Printing");
                DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
                DocPrintJob job = psZebra.createPrintJob();
                byte[] byteArray = strToPrint.getBytes();
                Doc mydoc = new SimpleDoc(byteArray, flavor, null);
                try {
                    job.print(mydoc, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        try {
            if (isNotEmpty(getParameter("timeoutseconds"))) {
                this.TIMEOUTSECONDS = Integer.parseInt(getParameter("timeoutseconds"));
                if (TIMEOUTSECONDS < 15) TIMEOUTSECONDS = 15;
            }
        } catch (NumberFormatException nfe2) {
            //* set to default
            TIMEOUTSECONDS = 30;
        }

        try {
            if (isNotEmpty(getParameter("baud"))) {
                BAUD = Integer.parseInt(getParameter("baud"));
                if (BAUD < 1000) BAUD = 1000;
            }
        } catch (NumberFormatException nfe2) {
            //* set to default
            BAUD = 19200;
        }

        //* convert stopbits parameter
        try {
            if (isNotEmpty(getParameter("stopbits"))) {
                STOPBITS = Integer.parseInt(getParameter("stopbits"));
            }
        } catch (NumberFormatException nfe1) {
            //* set to default
            STOPBITS = DEFAULT_STOPBITS;
        }

        switch (STOPBITS) {
            case SerialPort.STOPBITS_1:
                break;
            case SerialPort.STOPBITS_1_5:
                break;
            case SerialPort.STOPBITS_2:
                break;
            default:
                STOPBITS = SerialPort.STOPBITS_1;
        }


        // convert databits parameter
        try {
            if (isNotEmpty(getParameter("databits"))) {
                this.DATABITS = Integer.parseInt(getParameter("databits"));
            }
        } catch (NumberFormatException nfe2) {
            // set to default
            DATABITS = DEFAULT_DATABITS;
        }

        switch (DATABITS) {
            case SerialPort.DATABITS_5:
                break;
            case SerialPort.DATABITS_6:
                break;
            case SerialPort.DATABITS_7:
                break;
            case SerialPort.DATABITS_8:
                break;
            default:
                DATABITS = SerialPort.DATABITS_8;
        }

        // convert parity parameter
        try {
            if (isNotEmpty(getParameter("parity"))) {
                this.PARITY_STR = getParameter("parity");
            }
        } catch (Exception e2) {
            // set to default
            PARITY_STR = String.valueOf(SerialPort.PARITY_NONE);
        }

        if (PARITY_STR.equalsIgnoreCase("EVEN")) {
            PARITY = SerialPort.PARITY_EVEN;
        } else if (PARITY_STR.equalsIgnoreCase("MARK")) {
            PARITY = SerialPort.PARITY_MARK;
        } else if (PARITY_STR.equalsIgnoreCase("ODD")) {
            PARITY = SerialPort.PARITY_ODD;
        } else if (PARITY_STR.equalsIgnoreCase("SPACE")) {
            PARITY = SerialPort.PARITY_SPACE;
        } else {
            PARITY = SerialPort.PARITY_NONE;
        }

        if (ready && debug) System.out.print("ready.. ");
        if (ready && isNotEmpty(strToPrint) && isNotEmpty(portName)) {

            if (debug) System.out.println("set... go...." + portName);

            try {
                //* Get the CommPortIdentifier (e.g. LPT1)

                System.out.println("Before getPortIdentifier");

                try {
                    thePortID = CommPortIdentifier.getPortIdentifier(portName);
                } catch (Exception e) {
                    showDebug("CommPortIdentifier: " + e.getLocalizedMessage());
                    showDebug(">>>");
                    e.printStackTrace();
                    showDebug("<<<");
                }


                // thePortID = commPortIdentifierClazz.getPortIdentifier( portName );

                showDebug("After getPortIdetntifier");

                showDebug("thePortID: " + thePortID);
                showDebug("thePortID.getName(): " + thePortID.getName());
                showDebug("thePortID.getPortType(): " + thePortID.getPortType());

                // Now actually open the port.
                // This form of openPort takes an Application Name and a timeout.
                showDebug("Trying to open " + thePortID.getName() +
                        "..." + thePortID.getPortType());

                //Set the values for serial port used by CUBISCAN
                String cubiScanSet[] = {"cmd.exe", "/c", "start", "/min", "/B", "mode.com", "COM1", "baud=9600", "parity=0", "data=8", "stop=1",};

                String comCmdStr = "";

                switch (thePortID.getPortType()) {

                    case CommPortIdentifier.PORT_SERIAL:
                        showDebug("PORT_SERIAL");
                        if (!thePortID.isCurrentlyOwned()) {
                            if (debug) System.out.println("serial going (COM1) ...");
                            thePort = thePortID.open("COM1", 10 * 1000); //time out ten secs
                        }
                        //* works for carousels strToPrint = String.valueOf((char)STX) + strToPrint + String.valueOf((char)ETX) + '\0';
                        if (debug) System.out.println("Command string ..." + strToPrint);
                        myPort = (SerialPort) thePort;
                        //* set up the serial port and command
                        if (this.strToPrint.contains("MEASURE")
                                || this.strToPrint.contains("CALIBRATE")) {
                            rt = Runtime.getRuntime();
                            p = rt.exec(cubiScanSet);
                            if (debug) System.out.println("Executed the serial port settings ..." + strToPrint);
                            if ((debug) && (myPort != null))
                                System.out.println("Port is not nULL ..." + myPort.toString());
                            if (p != null && p.waitFor() != 0) {
                                System.out.println("Error executing command: " + cubiScanSet);
                                System.exit(-1);
                            }
                            if (this.strToPrint.contains("CALIBRATE"))
                                comCmdStr = String.valueOf((char) STX) + String.valueOf((char) D) + String.valueOf((char) ETX) + String.valueOf((char) CR) + String.valueOf((char) LF);
                            else
                                comCmdStr = String.valueOf((char) STX) + String.valueOf((char) M) + String.valueOf((char) ETX) + String.valueOf((char) CR) + String.valueOf((char) LF);

                        } else {
                            myPort.setSerialPortParams(BAUD, DATABITS, STOPBITS, PARITY);
                        }

                        break;

                    case CommPortIdentifier.PORT_PARALLEL:
                        System.out.println("PORT_PARALLEL");
                        if (!thePortID.isCurrentlyOwned()) {
                            if (debug) System.out.println("parallel going (LPT1) ...");
                            thePort = thePortID.open("LPT1", TIMEOUTSECONDS * 1000);
                        }
                        break;

                    default: // Neither parallel nor serial??
                        throw new IllegalStateException("Unknown port type " +
                                thePortID);
                }

                showDebug("opened port ..." + this.portName);

                if (myPort != null) myPort.enableReceiveTimeout(8000); //Time out in 3 secs
                cubiscanLine = "";
                byte data[] = comCmdStr.getBytes();
                //---------------------------------------------------------------
                //writing from port
                if ((thePortID.getPortType() == CommPortIdentifier.PORT_SERIAL) &&
                        (this.strToPrint.contains("MEASURE") || this.strToPrint.contains("CALIBRATE"))) {
                    if (debug) System.out.println("writing to port ..." + this.portName);
                    try (OutputStream out = myPort.getOutputStream()) {
                        out.write(data, 0, data.length);
                    }
                    if (debug) System.out.println("writing to port completed ..." + this.portName);
                    Thread.currentThread().sleep(6000);
                    try (BufferedReader is = new BufferedReader(new InputStreamReader(myPort.getInputStream()))) {
                        cubiscanLine = is.readLine();
                    }
                    String outputStr = "";
                    if (debug) System.out.println("CUBISCAN -- " + cubiscanLine);
                    int noData = -1;
                    if (cubiscanLine == null || (cubiscanLine != null && cubiscanLine.contains("MN"))) noData = 1;
                    else if (noData <= -1) {
                        if ((cubiscanLine.contains("L") && cubiscanLine.split("L").length > 1) &&
                                (Double.parseDouble(cubiscanLine.split("L")[1].split(",")[0]) <= 0)) noData = 2;
                        else if ((cubiscanLine.contains("W") && cubiscanLine.split("W").length > 1) &&
                                (Double.parseDouble(cubiscanLine.split("W")[1].split(",")[0]) <= 0)) noData = 2;
                        else if ((cubiscanLine.contains(",H") && cubiscanLine.split(",H").length > 1) &&
                                (Double.parseDouble(cubiscanLine.split(",H")[1].split(",")[0]) <= 0)) noData = 2;
                        else if ((cubiscanLine.contains("K") && cubiscanLine.split("K").length > 1) &&
                                (Double.parseDouble(cubiscanLine.split("K")[1].split(",")[0]) <= 0)) noData = 2;
                    }
                    if ((strPram.contains("MEASURE") || strPram.contains("CALIBRATE")) &&
                            (noData <= -1)) {
                        outputStr = cubiscanLine.substring(1, 59);
                        soapStrLen = outputStr.trim().length();
                        soapStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                                "    <soap:Header></soap:Header>\n" +
                                "    <soap:Body xmlns:ns1=\"http://comws/COMWebServiceServer.wsdl\">\n" +
                                "        <ns1:cubiScanDataElement>\n" +
                                "            <ns1:documentNumber>"
                                + "CUBI-" + this.strToPrint.split(",")[1].toString()
                                + "</ns1:documentNumber>\n" +
                                "            <ns1:strData>"
                                + cubiscanLine.trim()
                                + "</ns1:strData>\n" +
                                "        </ns1:cubiScanDataElement>\n" +
                                "    </soap:Body>\n" +
                                "</soap:Envelope>\n";
                        soapStrLen = soapStr.length();
                        int port = Integer.valueOf(hostStr.split(":")[2]); // 8990;
                        String hostname = (hostStr.split("//")[1]).split(":")[0];
                        String httpStr = hostStr.split("://")[0];
                        InetAddress addr = InetAddress.getByName(hostname);
                        if (debug) System.out.println("http " + httpStr + "Address " + addr + " port  " + port);
                        if (debug) System.out.println("Address2 " + addr + " port  " + port);

                        //Create socket
                        try (Socket sock = new Socket(addr, port)) {
                            //Send header
                            try (
                                    OutputStreamWriter osw = new OutputStreamWriter(sock.getOutputStream(), "UTF-8");
                                    BufferedWriter wr = new BufferedWriter(osw)) {
                                // You can use "UTF8" for compatibility with the Microsoft virtual machine.
                                if (httpStr.equalsIgnoreCase("http")) {
                                    wr.write("POST " + path + " HTTP/1.0\r\n");
                                } else {
                                    wr.write("POST " + path + " HTTPS/1.0\r\n");
                                }
                                wr.write("Host: " + hostname + "\r\n");
                                wr.write("Content-Length: " + soapStrLen + "\r\n");
                                wr.write("Content-Type: text/xml; charset=\"utf-8\"\r\n");
                                wr.write("SOAPAction: \"" + httpStr + "://comws/server/webservice/COMWebServiceServer.wsdl/cubiScanData\"\r\n");
                                wr.write("\r\n");

                                if (debug) {
                                    System.out.println("Soap str" + soapStr);
                                }
                                //Send data
                                wr.write(soapStr);
                                wr.flush();
                            }
                            // Response
                            try (BufferedReader rd = new BufferedReader(new InputStreamReader(sock.getInputStream()))) {
                                // Not sure what this is doing it just seems to loop and print if debugging is on
                                String line;
                                while ((line = rd.readLine()) != null) {
                                    if (debug) {
                                        System.out.println(line);
                                    }
                                }
                            }
                        }
                        cubiscanLine = "CUBISCAN READINGS: " + cubiscanLine;
                        getAppletContext().showDocument(new URL("javascript:alert(\"" + cubiscanLine + "\")"));
                    } else {
                        if (noData > 1)
                            cubiscanLine = cubiscanLine + "Either there is NO ITEM on CUBISCAN OR Some of the dimensions are zero. ";
                        else
                            cubiscanLine = cubiscanLine + " If the CUBISCAN readings are not showing up check device and retry.";
                        getAppletContext().showDocument(new URL("javascript:alert(\"" + cubiscanLine + "\")"));
                        if (debug) System.out.println("Was in pop up window.");
                    }

                } else {
                    // Get the input and output streams
                    // Printers can be write-only
                    //os = new PrintStream(thePort.getOutputStream(), true);
                    //os.println(strToPrint);
                    //os.flush();
                    //if (debug) System.out.println("Other type of print.");


                }
                //---------------------------------
                if (debug) System.out.println("Clean up.");
                if (os != null) os.close();
                if (myPort != null) myPort.close();
                if (p != null) p.destroy();

                //* must close the port, otherwise multiple prints will fail due to currently in use
                thePort.close();
                thePortID = null;
            } catch (Exception e) {

                System.out.println("WTF exception: " + e.getLocalizedMessage());

                try {
                    cubiscanLine = cubiscanLine + " -- Please resolve CUBISCAN issues, You may have to logout STRATIS and close all IE Browsers and come back and try.";
                    if (strPram.contains("MEASURE") || strPram.contains("CALIBRATE"))
                        getAppletContext().showDocument(new URL("javascript:alert(\"" + cubiscanLine + "\")"));
                    if (debug) System.out.println("Got exception.");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                if (debug) System.out.println("Clean up even in exception.");
                if (os != null) os.close();
                if (myPort != null) myPort.close();
                if (p != null) p.destroy();

                e.printStackTrace();
                System.out.println("problem with STRATIS COMApplet " + e.getMessage());
            }
        }
    }

    protected class JarFileLoader extends URLClassLoader {

        public JarFileLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        public JarFileLoader(URL[] urls) {
            super(urls);
        }

        public void addFile(String path) throws MalformedURLException {
            String urlPath = "jar:file://" + path + "!/";
            addURL(new URL(urlPath));
        }
    }


}
