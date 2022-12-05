package utils;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    private static Logger log = LoggerFactory.getLogger(Utils.class.getName());

    public void printLog(String logType, String message){
        switch(logType){
            case "info":
            log.info(message);
            break;
            case "warn":
            log.warn(message);
            break;
            case "error":
            log.error(message);
            break;
        }
     }

    public int moveFile(String source, String target){
        Path temp;
        int resultCode;
        try {
           temp= Files.move(Paths.get(source),Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
           if(temp!=null){
            log.info("File moved succesfully");
            resultCode=200;
           }
           else{
            log.error("Failed to move the file");
            resultCode=500;
           }
        } catch (IOException e) {
            log.warn("Invalid path:" +e.toString());
           resultCode=400;
        }
        return resultCode;
     }

     public Boolean validateMultipart(HashMap<String, String> params, String key){
        if(params.get(key) !=null || params.get(key) !=""){
            return true;
        }
        else{
            return false;
        }
     }

     public String validteBucketType(String bucket){
        if(bucket.equals("/gray") 
        || bucket.equals("/blur") 
        || bucket.equals("/edges")){
            return "Trabajos";
        }else{
            if(bucket.equals("/procesadas")){
                return "Procesadas";
            }else{
                return "N/A";
            }
        }
     }
    
}
