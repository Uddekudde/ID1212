/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id1212.tudd.db.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author udde
 */
public interface FileServer extends Remote {
    
    public static final String SERVER_NAME_IN_REGISTRY = "FILE_SERVER";
    
    long login(FileClient remoteNode, String username, String password) throws RemoteException;
    
    void logout(long userId) throws RemoteException;
    
    void register(FileClient remoteNode, String username, String password) throws RemoteException;
    
    void unregister(long clientId) throws RemoteException;
    
    void list(long clientId) throws RemoteException;
    
    void upload(long clientId, String filename, String size, String access) throws RemoteException;
    
    void delete(long clientId, String filename) throws RemoteException;
    
    void update(long clientId, String  filename, String newFilename, String newSize, String newAccess) throws RemoteException;
    
    void download(long clientId, String filename) throws RemoteException;
    
    void requestNotify(long clientId, String filename) throws RemoteException;
}
