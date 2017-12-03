/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id1212.tudd.db.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.kth.id1212.tudd.db.common.FileClient;

/**
 *
 * @author udde
 */
public class FileHandler {
    private final Map<String, File> files = Collections.synchronizedMap(new HashMap<>());
    
    public void StoreFile(File file){
        files.put(file.filename, file);
    }
    
    public void removeFile(String filename) {
        files.remove(filename);
    }
    
    public File findFile(String filename) {
        return files.get(filename);
    }
    
    public void updateFile(String filename, String newFilename) {
        File file = files.get(filename);
        if(file != null)
            file.filename = newFilename;
    }
    
    public List<File> getAllFiles(){
        List<File> listToReturn = new ArrayList<>();
        for(File file : files.values()){
            listToReturn.add(file);
        }
        return listToReturn;
    }
}
