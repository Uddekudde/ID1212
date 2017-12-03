/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id1212.tudd.db.server;

import se.kth.id1212.tudd.db.common.FileClient;

/**
 *
 * @author udde
 */
public class Client {
    String username;
    String password;
    FileClient remoteNode;
    
    public Client(String username, String password){
        this.username = username;
        this.password = password;
    }
    public Client(FileClient remoteNode, String username, String password){
        this.username = username;
        this.password = password;
        this.remoteNode = remoteNode;
    }
    
}
