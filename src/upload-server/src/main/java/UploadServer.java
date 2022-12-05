import java.util.Arrays;

import cs.edu.uv.http.config.Configurator;
import cs.edu.uv.http.dynamicresponse.Server;
import upload.UploadServerEvents;

public class UploadServer {
   public static void main(String[] args) {
      Configurator config = new Configurator(
            Arrays.asList("PORT", "NTHREADS", "STORAGE_DIR","UPLOAD_SERVER_URL", "DOWNLOAD_SERVER_URL", "BASE_PATH"), "config.ini", null);

      // Default values
      int nThreads = config.getIntProperty("NTHREADS", 50);
      int port = config.getIntProperty("PORT", 8080);

      // The URL path that triggers the execution 
      String PATH_REQUEST = config.getRequiredProperty("BASE_PATH");
     
      // The URL path that triggers the execution 
      String UPLOAD_SERVER_URL = config.getRequiredProperty("UPLOAD_SERVER_URL");
     
      // The URL path that triggers the execution 
      String DOWNLOAD_SERVER_URL = config.getRequiredProperty("DOWNLOAD_SERVER_URL");
     

      String STORAGE_DIR = config.getProperty("STORAGE_DIR", "/data");
      
      Server.SERVER_INFO = "Custom Dynamic HTTP Server";
      
      Server server = new Server(nThreads, port);
      server.startHTTPEndPoint(new UploadServerEvents(STORAGE_DIR, DOWNLOAD_SERVER_URL, UPLOAD_SERVER_URL), PATH_REQUEST);
   } 
}
