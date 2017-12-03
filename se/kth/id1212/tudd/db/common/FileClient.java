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
public interface FileClient extends Remote {
    
    void recieveMessage(String msg) throws RemoteException;
}
