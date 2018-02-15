/*
@author Vishal Govindraddi Yarabandi
        CMPE-206-Network Design Course Project under Dr.Juan Gomez
        San Jose Sate University
        MasterBot

@usage java MasterBot -p <Port>
*/

import java.net.*;
import java.io.*;
import java.lang.*;

class Listener implements Runnable { //Class to connect to Host Multithreaded
    private int listenerPort;
    private Thread th;
    private String threadName = "masterListenerThread";
    Listener(int Port) {
        listenerPort = Port;
        threadName = "masterListenerThread";
    }

    @Override
    public void run(){
        try(ServerSocket masterSock = new ServerSocket(listenerPort);) {
            while(true) {
                try(Socket slave = masterSock.accept(); //create new socket for the incoming connection
                    BufferedReader  in = new BufferedReader(new InputStreamReader(slave.getInputStream())); //read data fom slave
                    FileWriter fw = new FileWriter("slaves.txt",true);) {  //open file to store slave details

                    String line;
                    while((line = in.readLine()) != null ) {                                
                        String[] split = line.split("\\s+");
                        fw.write(line);    //write line at the end of the file
                        fw.write(" \n");
                        fw.close();
                    }
                    slave.close(); //job done for this slave, close socket
                }
                catch(IOException ex) {
                    System.err.println("Run-while: " + ex.getMessage());
                    System.exit(-1);
                }
            }
        }
        catch(IOException ex) {
            System.err.println("Run: " + ex.getMessage());
            System.exit(-1);
        }
    }

    public void start() {
        if( th == null) {
            th = new Thread(this,threadName);
            th.start();
        }
    }
}

public class MasterBot {
    private static void listSlaves() { //Displays the slave details
        File slavesFile = new File("slaves.txt"); //Check if slaves.txt is not present
        if(!slavesFile.exists()) {
            System.out.println("No Slaves Registered!");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader("slaves.txt"))) {
            String line; 
            System.out.println("SlaveHostName        IP Address    SourcePortNumber     RegistrationDate");
            while ((line = br.readLine()) != null) {
       		String[] details = line.split("\\s+");   //split the data by spaces
       		System.out.println(details[0] + "               " + details[1] + "           " + details[2] + "           " + details[3]);
            }
            br.close();
	}
	catch(IOException ex) {
            System.err.println("listSlaves: " + ex.getMessage());
        }
    }

    private static void connect(String slaveIP, String targetHost, String targetPort, String connections) {
    	try(BufferedReader br = new BufferedReader(new FileReader("slaves.txt"));) { //loop over all slaves in the record
            String line;
            while ((line = br.readLine()) != null) {
                String[] slaveDetails = line.split("\\s+");
                if ((slaveDetails[1].equals(slaveIP)) || (slaveDetails[0].equals(slaveIP)) || "all".equals(slaveIP)) {
                    int slavePort = Integer.parseInt(slaveDetails[2]);
                    try {
                        Socket slave = new Socket(slaveDetails[1], slavePort);
                        PrintWriter pw = new PrintWriter(slave.getOutputStream(),true);
                        pw.println("connect " + targetHost + " " + targetPort  + " " + connections); //Sending command to slave
                    }
                    catch(IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
            br.close();
        }
        catch(IOException ex) {
              System.err.println("connect: " + ex.getMessage());
        }
    }

    private static void disconnect(String slaveIP, String targetHost, String targetPort) {
    	try(BufferedReader br = new BufferedReader(new FileReader("slaves.txt"));) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] slaveDetails = line.split("\\s+");
                if ( ( slaveDetails[1].equals(slaveIP) ) || ( slaveDetails[0].equals(slaveIP) ) || "all".equals(slaveIP) ) {
                    int slavePort = Integer.parseInt(slaveDetails[2]);
                    try {
                        Socket slaveSock = new Socket(slaveDetails[1], slavePort);
                        PrintWriter pout = new PrintWriter(slaveSock.getOutputStream(),true);
                        pout.println("disconnect " + targetHost + " " + targetPort ); //sending command to slave
                    }
                    catch(IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
            br.close();
        }
        catch(IOException ex) {
            System.err.println("disconnect " + ex.getMessage());
        }
    }
 
    public static void main(String[] args) {
        if(args.length != 2 ) {
            System.out.println("Port missing!");
            System.exit(-1);
        }
        if( !("-p".equals(args[0])) ) {
            System.out.println("Port missing!");
            System.exit(-1);
        }

        File oldSlaveList = new File("slaves.txt");
        oldSlaveList.delete();                   //Deleting slaves.txt before starting new registration

        int Port = Integer.parseInt(args[1]);
        Listener listen = new Listener(Port);
        listen.start();

        while (true) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String cmdStr;
                String numberOfConnections = "1";   //Default number of connections set to 1
                System.out.printf(">");
                cmdStr = br.readLine();
                args = cmdStr.split("\\s+");
 
                if("list".equals(args[0])) {
                    listSlaves();
                }
 
                if("connect".equals(args[0])) {
                    if(args.length >= 4) {
                        if(args.length == 5)
                            numberOfConnections = args[4];

                        connect(args[1], args[2], args[3], numberOfConnections);
                    }
                    else
                        System.out.println("Parameters Missing!");
                }

                if("disconnect".equals(args[0])) {
                    if(args.length == 4)
                        disconnect(args[1], args[2], args[3]);
                    else
                        System.out.println("Parameters Missing!");
                }
            }
            catch(IOException ex) { 
                System.err.println("Main: " + ex.getMessage());
                System.exit(-1);
            }
        }
    }
}
