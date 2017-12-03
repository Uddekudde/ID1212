/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id1212.tudd.db.server;

/**
 *
 * @author udde
 */
public class File {
    String filename;
    int size;
    String owner;
    String access;
    Client toNotify;

    public File(String filename, int size, String owner, String access){
        this.filename = filename;
        this.size = size;
        this.owner = owner;
        this.access = access;
    }
    
    public String toString(){
        return "Filename: "+this.filename+" size: "+this.size+" owner: "+this.owner
                +" access: "+ this.access;
    }
    
    public void setNotify(Client client){
        toNotify = client;
    }
}   

