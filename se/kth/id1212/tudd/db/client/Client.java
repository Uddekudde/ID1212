/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id1212.tudd.db.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import se.kth.id1212.tudd.db.common.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author udde
 */
public class Client implements Runnable {
    
    long userId;
    long responseId;
    FileServer server;
    String host = "127.0.0.1";
    boolean isLoggedIn;
    boolean isConnected;
    
    private final String LOGIN = "#login";
    private final String LOGOUT = "#logout";
    private final String REGISTER = "#register";
    private final String UNREGISTER = "#unregister";
    private final String LIST = "#list";
    private final String UPLOAD = "#upload";
    private final String DOWNLOAD = "#download";
    private final String DELETE = "#delete";
    private final String NOTIFY = "#notify";
    private final String QUIT = "#quit";
    private final String UPDATE = "#update";
    private final long ALREADY_LOGGED_IN = 0;
    
    private final String[] INSTRUCTIONS = {"The following commands are available:",
        "#login <username> <password>","#logout", "#register <username> <password>", 
        "#unregister", "#list", "#upload", "#download",
        "#update", "#delete", "#notify", "#quit", "To begin, register and then login."};
    
    private void lookupServer() throws NotBoundException, MalformedURLException,
                                                  RemoteException {
        server = (FileServer) Naming.lookup(
                "//" + host + "/" + FileServer.SERVER_NAME_IN_REGISTRY);
    }

    @Override
    public void run(){
        for(String command : INSTRUCTIONS){
            System.out.println(command);
        }
        try {
            try{
            lookupServer();
            }
            catch (MalformedURLException | NotBoundException | RemoteException ex){
                System.out.println("Error connecting to server.");
            }
        ConsoleOutput remoteObject = new ConsoleOutput();
        isConnected = true;
        Scanner input = new Scanner(System.in);
        String[] command;
        while(isConnected){
                command = parse(input.nextLine());
                switch(command[0]){
                    case LOGIN:
                        responseId = server.login(remoteObject, command[1], command[2]);
                        if(responseId != ALREADY_LOGGED_IN){
                            userId = responseId;
                            isLoggedIn = true;
                        }
                        break;
                    case LOGOUT:
                        if(!isLoggedIn)
                            System.out.println("You aren't logged in.");
                        else {
                            server.logout(userId);
                            isLoggedIn = false;
                            boolean forceUnexport = false;
                            UnicastRemoteObject.unexportObject(remoteObject, forceUnexport);
                            remoteObject = new ConsoleOutput();
                        }
                        break;
                    case REGISTER:
                        server.register(remoteObject, command[1], command[2]);
                        break;
                    case UNREGISTER:
                        if(!isLoggedIn)
                            System.out.println("You aren't logged in.");
                        else {
                            server.unregister(userId);
                            server.logout(userId);
                        }
                        break;
                    case LIST:
                        if(!isLoggedIn)
                            System.out.println("You aren't logged in.");
                        else 
                            server.list(userId);
                        break;
                    case UPLOAD:
                        if(!isLoggedIn)
                            System.out.println("You aren't logged in.");
                        else 
                            server.upload(userId, command[1], command[2], command[3]);
                        break;
                    case UPDATE:
                        if(!isLoggedIn)
                            System.out.println("You aren't logged in.");
                        else 
                            server.update(userId, command[1], command[2], command[3], command[4]);
                        break;
                    case DOWNLOAD:
                        if(!isLoggedIn)
                            System.out.println("You aren't logged in.");
                        else 
                            server.download(userId, command[1]);
                        break;
                    case DELETE:
                        if(!isLoggedIn)
                            System.out.println("You aren't logged in.");
                        else 
                            server.delete(userId, command[1]);
                        break;
                    case NOTIFY:
                        if(!isLoggedIn)
                            System.out.println("You aren't logged in.");
                        else 
                            server.requestNotify(userId, command[1]);
                        break;
                    case QUIT:
                        if(isLoggedIn) {
                            server.logout(userId);
                            isConnected = false;
                        } else {
                            isConnected = false; 
                            boolean forceUnexport = false;
                            UnicastRemoteObject.unexportObject(remoteObject, forceUnexport);
                        }
                        break;
                    default:
                        System.out.println("Malformed command.");
                        break;
                }
            }
        }
        catch (RemoteException ex) {
            ex.printStackTrace();
        }
        
        }
    
    private String[] parse(String fromConsole){
        String[] str = fromConsole.split(" ");
        if(str[0].equalsIgnoreCase(LOGIN) && str.length < 3){
            return new String[]{"Error"};
        }
        if(str[0].equalsIgnoreCase(REGISTER) && str.length < 3){
            return new String[]{"Error"};
        }
        if(str[0].equalsIgnoreCase(UPLOAD) && str.length < 4){
            return new String[]{"Error"};
        } 
        if(str[0].equalsIgnoreCase(DELETE) && str.length < 2){
            return new String[]{"Error"};
        }
        if(str[0].equalsIgnoreCase(DOWNLOAD) && str.length < 2){
            return new String[]{"Error"};
        }
        if(str[0].equalsIgnoreCase(UPDATE) && str.length < 5){
            return new String[]{"Error"};
        }
        if(str[0].equalsIgnoreCase(NOTIFY) && str.length < 2){
            return new String[]{"Error"};
        }
        
        return str;
    }
    
    private void upload(String fileName){
        
    }
    
    private class ConsoleOutput extends UnicastRemoteObject implements FileClient {

        public ConsoleOutput() throws RemoteException {
        }

        @Override
        public void recieveMessage(String msg) {
            System.out.println(msg);
        }
    } 
    
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
