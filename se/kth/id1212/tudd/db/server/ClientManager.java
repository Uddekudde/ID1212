/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id1212.tudd.db.server;

import java.util.ArrayList;
import se.kth.id1212.tudd.db.common.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author udde
 */
public class ClientManager {
    private final Random idGenerator = new Random();
    private final Map<Long, Client> clients = Collections.synchronizedMap(new HashMap<>());
    
    public long StoreClient(FileClient remoteNode, String username, String password){
        long clientId = idGenerator.nextLong();
        clients.put(clientId, new Client(remoteNode, username, password));
        return clientId;
    }
    
    public Client findClient(long id) {
        return clients.get(id);
    }
    
    public void removeClient(long id) {
        clients.remove(id);
    }
    
    public List<Client> getAllClients(){
        List<Client> listToReturn = new ArrayList<>();
        for(Client client : clients.values()){
            listToReturn.add(client);
        }
        return listToReturn;
    }
}
