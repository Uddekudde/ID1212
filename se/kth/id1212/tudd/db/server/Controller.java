/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id1212.tudd.db.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id1212.tudd.db.common.*;

/**
 *
 * @author udde
 */
public class Controller extends UnicastRemoteObject implements FileServer {
    ClientManager manager = new ClientManager();
    FileDAO fileDAO;
    FileHandler handler = new FileHandler();
    
    private final String UPDATED = " updated";
    private final String DELETED = " deleted";
    private final String DOWNLOADED = " downloaded";
    
    public Controller() throws RemoteException{
        try {
            fileDAO = new FileDAO();
        } catch (Exception ex) {
            
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public synchronized long login(FileClient remoteNode, String username, String password) throws RemoteException {
        List<Client> clients = manager.getAllClients();
        for(Client client : clients)
            if(client.username.equals(username)){
                remoteNode.recieveMessage("That user is already logged in.");
                return 0;
            }
        try {
            Client client = fileDAO.findUserByName(username);
            if(client == null){
                remoteNode.recieveMessage("Wrong username or password.");
                return 0;
            }
            if(client.password.equals(password)){
                System.out.println("hello "+username+" "+password);
                remoteNode.recieveMessage("Login successful.");
                return manager.StoreClient(remoteNode, username, password);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        remoteNode.recieveMessage("Wrong username or password.");
        return 0;
    }

    @Override
    public synchronized void logout(long userId) throws RemoteException {
        Client client = manager.findClient(userId);
        manager.removeClient(userId);
    }

    @Override
    public synchronized void register(FileClient remoteNode, String username, String password) throws RemoteException {
        try {
            Client client = fileDAO.findUserByName(username);
            if(client == null){
                fileDAO.createUser(username, password);
                remoteNode.recieveMessage("Registration successful.");
            } else 
                remoteNode.recieveMessage("That username is already taken.");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public synchronized void unregister(long clientId) throws RemoteException {
        Client client = manager.findClient(clientId);
        try {
            fileDAO.deleteUser(client.username);
            client.remoteNode.recieveMessage("Unregistration successful.");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            client.remoteNode.recieveMessage("There is no such user.");
        }
    }

    @Override
    public synchronized void list(long clientId) throws RemoteException {
        Client client = manager.findClient(clientId);
        try {
            List<File> files = fileDAO.findAllFiles(client.username);
            for(File file : files){
                client.remoteNode.recieveMessage(file.toString());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public synchronized void upload(long clientId, String filename, String size, String access) throws RemoteException {
        int sizeInt = Integer.parseInt(size);
        String validAccess = validateAccess(access);
        Client client = manager.findClient(clientId);
        try {
            File file =fileDAO.findFileByName(filename);
            if(file == null){
                fileDAO.createFile(filename, sizeInt, client.username, validAccess);
                client.remoteNode.recieveMessage("File created.");
            } else {
                client.remoteNode.recieveMessage("That file already exists.");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            client.remoteNode.recieveMessage("That file already exists.");
        }
    }
    
    @Override
    public void update(long clientId, String filename, String newFilename, String newSize, String newAccess) throws RemoteException {
        int sizeInt = Integer.parseInt(newSize);
        String validAccess = validateAccess(newAccess);
        Client client = manager.findClient(clientId);
        try {
            File file =fileDAO.findFileByName(filename);
            if(file == null)
                client.remoteNode.recieveMessage("No such file exists.");
            else {
                if(file.owner.equals(client.username)){
                    fileDAO.updateFile(filename, newFilename, sizeInt, validAccess);
                    client.remoteNode.recieveMessage("File updated.");
                    handler.updateFile(filename, newFilename);
                } else if(file.access.equals("write")){
                    fileDAO.updateFile(filename, newFilename, sizeInt, validAccess);
                    client.remoteNode.recieveMessage("File updated.");
                    notify(filename, client.username+UPDATED);
                    handler.updateFile(filename, newFilename);
                } else {
                    client.remoteNode.recieveMessage("You are not the owner of this file "
                            + "and it is read only");
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            client.remoteNode.recieveMessage("Could not update file.");
        }
    }
    
    @Override
    public synchronized void delete(long clientId, String filename) throws RemoteException {
        Client client = manager.findClient(clientId);
        try {
            File file =fileDAO.findFileByName(filename);
            if(file == null)
                client.remoteNode.recieveMessage("There is no such file.");
            else {
                if(file.owner.equals(client.username)){
                    fileDAO.deleteFile(filename);
                    client.remoteNode.recieveMessage("File deleted.");
                    handler.removeFile(filename);
                } else if(file.access.equals("write")){
                    fileDAO.deleteFile(filename);
                    client.remoteNode.recieveMessage("File deleted.");
                    notify(filename, client.username+DELETED);
                    handler.removeFile(filename);
                } else {
                    client.remoteNode.recieveMessage("You are not the owner of this file "
                            + "and it is read only");
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            client.remoteNode.recieveMessage("There is no such file.");
        }
    }

    @Override
    public synchronized void download(long clientId, String filename) throws RemoteException {
        Client client = manager.findClient(clientId);
        try {
            File file =fileDAO.findFileByName(filename);
            if(file == null)
                client.remoteNode.recieveMessage("No such file exists.");
            else {
                if(file.owner.equals(client.username)){
                    client.remoteNode.recieveMessage(file.toString());
                } else if(file.access.equals("private")){
                        client.remoteNode.recieveMessage("No such file exists.");
                } else {
                    client.remoteNode.recieveMessage(file.toString());
                    notify(filename, client.username+DOWNLOADED);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            client.remoteNode.recieveMessage("Could not retrieve file.");
        }
    }

    @Override
    public synchronized void requestNotify(long clientId, String filename) throws RemoteException {
        Client client = manager.findClient(clientId);
        try {
            File file =fileDAO.findFileByName(filename);
            if(file == null)
                client.remoteNode.recieveMessage("No such file exists.");
            else {
                if(file.owner.equals(client.username)){
                    file.toNotify = client;
                    handler.StoreFile(file);
                    client.remoteNode.recieveMessage("You will be notified about changes to this file.");
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            client.remoteNode.recieveMessage("Could notify about file.");
        }
    }
    
    private void notify(String filename, String message) throws RemoteException{
        List<File> files = handler.getAllFiles();
        for(File file : files)
            if(file.filename.equals(filename)){
                file.toNotify.remoteNode.recieveMessage(message+" "+file.filename);
            }
    }
    
    private String validateAccess(String access){
        String validAccess;
        switch(access){
            case "private":
                validAccess = access;
                break;
            case "write":
                validAccess = access;
                break;
            default:
                validAccess = "read";
                break;
        }
        return validAccess;
    }
}
