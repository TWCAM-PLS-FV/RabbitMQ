package upload;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.Instant;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import rabbit.ConexionRabbitMQ;
import rabbit.RabbitMQConfig;

import cs.edu.uv.http.dynamicresponse.ThingsAboutRequest;
import cs.edu.uv.http.dynamicresponse.ThingsAboutResponse;
import cs.edu.uv.http.dynamicresponse.WebResponse;
import event.ImageEvent;


public class UploadServerEvents extends WebResponse {
   private static Logger log = LoggerFactory.getLogger(UploadServerEvents.class.getName());
   // Servidor Nginx para descarga (info necesaria para el evento)
   private String downloadServerURL;

   // El servidor de upload (info necesaria para el evento)
   private String uploadServerURL;

   // Directorios donde se guardarán las imágenes
   private String storagePath;
   private String grayDir;
   private String blurDir;
   private String edgeDir;
   private String procDir;

   // Bucket e Imagen
   private String bucket;
   private String img;

   //Lanzamiento del RbMQ
   private static boolean startEvent;

   Utils utils;

   public UploadServerEvents(String storagePath, String downloadServerURL, String uploadServerURL) {
      this.downloadServerURL=downloadServerURL;
      this.uploadServerURL=uploadServerURL;
      this.storagePath = storagePath;
      utils=new Utils();

      // Crea los 4 subdirectorios para almacenar las imágenes:
      grayDir=storagePath+"/gray";
      File f1 = new File(grayDir);
      f1.mkdir();

      blurDir=storagePath+"/blur";
      File f2 = new File(blurDir);
      f2.mkdir();

      edgeDir=storagePath+"/edges";
      File f3 = new File(edgeDir);
      f3.mkdir();

      procDir=storagePath+"/procesadas";
      File f4 = new File(procDir);
      f4.mkdir();
   }

   public static void moveFile(String source, String target){
      Path temp;
      try {
         temp= Files.move(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
         if(temp!=null){
            log.info("File moved succesfully");
            startEvent=true;
         }
         else{
            log.warn("Failed to move the file");
            startEvent=false;
         }
      } catch (IOException e) {
         log.error("Source is not a valid path", e);
         startEvent=false;
      }
   }

   public void ifPost(ThingsAboutRequest req, ThingsAboutResponse resp) throws Exception {
      int resultCode;
      String bucketType;
      
      if (req.isMultipart()) {
         // Los parámetros del cuerpo de la petición multipart se guardan
         // en params
         HashMap<String, String> params = new HashMap<String, String>();
         // La información sobre los ficheros del cuerpo de la petición multipart
         // se guardan en files
         HashMap<String, String> files = new HashMap<String, String>();

         // Los ficheros se guardan en /tmp
         String path = "/tmp/";

         req.parseMultipart(params, files, path, false);

         // Obtener el parámetro bucket (de params)
         // Mover el fichero al directorio apropiado en función del valor de bucket
         // Si bucket contiene procesadas no lanzaremos el evento
         if(utils.validateMultipart(params, "bucket") && utils.validateMultipart(params, "IMAGE")){
               bucket=params.get("bucket");
               img=files.get("IMAGE");
               log.info("Bucket: "+bucket);
               log.info("IMG: "+img);

               bucketType=utils.validteBucketType(bucket);

               if(!bucketType.equals("procesadas")||
               !bucketType.equals("N/A")){
                  
                  // Crear una instancia del tipo ImageEvent y asignar la información
                  long unixTime = Instant.now().getEpochSecond();
                  moveFile(path+img, "/data"+bucket+"/"+img);
                  ImageEvent event = new ImageEvent();
                  event.setDownloadURL(downloadServerURL);
                  event.setAction(bucket);
                  event.setUploadServer(uploadServerURL);
                  event.setUploadBucket(bucket);
                  event.setTsCreationMessage(unixTime);
                  event.setImage(img);
                  // Serialización de la instancia del tipo ImageEvent a JSON (String)
                  Gson gson = new Gson();
                  String json = gson.toJson(event);

                  // Obtención del cuerpo del mensaje
                  byte[] messageBody = json.getBytes();

                  // Usamos el canal para definir: el exchange, la cola y la asociación
                  // exchange-cola
                  
                  Channel c = ConexionRabbitMQ.getChannel();
                  c.exchangeDeclare(RabbitMQConfig.EXCHANGE, "direct", true);
                  c.queueDeclare(RabbitMQConfig.COLA_TRABAJOS, true, false, false, null);
                  c.queueBind(RabbitMQConfig.COLA_TRABAJOS, RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_TRABAJOS);
                  
                  // Publicar el mensaje con el trabajo a realizar
                  c.basicPublish(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_TRABAJOS, null, messageBody);
                  
                  // Generar una respuesta con código 200,
                  // Añadir los campos de cabecera Content-Length y Content-Type
                  // El cuerpo de la respuesta se enviará el evento en formato JSON
                  PrintWriter pw = resp.getWriter();
                  log.info("200:OK - Petición realizada");
                  resp.setStatus(200);
                  resp.setResponseHeader("Content-Type", "text/html; charset=utf-8");
                  pw.println("<h1>Código de respuesta: 200</h1>");
                  pw.println("<p>200:OK - Petición realizada</p>");
               }
            }else{
               PrintWriter pw = resp.getWriter();
               log.warn("400: BadRequest - Parámetros inválidos");
               resp.setStatus(400);
               resp.setResponseHeader("Content-Type", "text/html; charset=utf-8");       
               pw.println("<h1>Código de respuesta: 400</h1>");
               pw.println("<p>Error: Petición inválida.</p>");
            }         
      }else{
         PrintWriter pw = resp.getWriter();
         log.warn("400: BadRequest - Petición no es multipart");
         resp.setStatus(400);
         resp.setResponseHeader("Content-Type", "text/html; charset=utf-8");       
         pw.println("<h1>Código de respuesta: 400</h1>");
         pw.println("<p>Error: Petición inválida.</p>");
      }
   }
}
