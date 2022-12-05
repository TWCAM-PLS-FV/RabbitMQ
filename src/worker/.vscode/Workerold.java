import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import opencv.OpenCVUtils;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import event.ImageEvent;
import event.JobCompletion;
import http.Http;
import rabbit.ConexionRabbitMQ;
import rabbit.RabbitMQConfig;

public class Worker {
   // Concatena una ruta con el nombre de fichero
   // Si es necesario pone el separador '/'
   private static String imgPath(String dir, String file) {
      String path = null;
      if (!dir.endsWith("/"))
         path = dir + "/" + file;
      else
         path = dir + file;
      return path;
   }

   public static void main(String[] args) {

      // Obtener un canal para comunicarnos con RabbitMQ
      System.out.println("Accediendo a la cola de trabajos..");
      Channel c = null;
      try {
         while (c == null) {
            System.out.println("Obteniendo conexión con la cola de trabajos...");
            c = ConexionRabbitMQ.getChannel();
            Thread.sleep(2000);
         }
      } catch (Exception e) {
         System.out.println("Error Conexión - Exeption:" + e.toString());
      }
      Channel finalChannel = c;
      String worker = args[0];

      // Subscribirse a la cola
      try {
         c.exchangeDeclare(RabbitMQConfig.EXCHANGE, "direct", true);
         //c.queueDeclare(RabbitMQConfig.COLA_TIEMPOS, true, false, false, null);
         c.queueDeclare(RabbitMQConfig.COLA_TRABAJOS, true, false, false, null);
         c.queueBind(RabbitMQConfig.COLA_TRABAJOS, RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_TRABAJOS);
         //c.queueBind(RabbitMQConfig.COLA_TIEMPOS, RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_TIEMPOS);
      } catch (Exception e) {
         System.out.println("Error Canal - Exeption:" + e.toString());
      }

      // Registrar un handler para procesar cada mensaje
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {

         System.out.println();
         System.out.println("Iniciando proceso...");
         long unixTimeStart = Instant.now().getEpochSecond();
         String message = new String(delivery.getBody(), "UTF-8");
         System.out.println("Mensaje de la cola:" + message);

         // - Obtener cuerpo y deserializar el JSON a ImageEvent
         ImageEvent imageEvent = new Gson().fromJson(message, ImageEvent.class);
         System.out.println("Evento: " + imageEvent);
         if (!imageEvent.getUploadBucket().equals("procesadas")) {
            HashMap<String, String> headers = new HashMap<String, String>();
            HashMap<String, String> params = new HashMap<String, String>();
            HashMap<String, String> files = new HashMap<String, String>();

            String URL = imageEvent.getDownloadURL() + imageEvent.getUploadBucket() + "/" + imageEvent.getImage();
            String path = "/tmp/" + imageEvent.getImage();
            System.out.println("URL:" + URL);

            // - Descargar la imagen (Usa el método saveGetRequest de la clase Http)
            try {
               Http.saveGetRequest(URL, params, headers, path);
            } catch (Exception e) {
               System.out.println("Error SaveRequest - Exception:" + e.toString());
            }

            // Cargar la librería de OpenCV
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            // - Procesar la imagen (ejemplo en SampleAppOpenCV)            
            Mat img = OpenCVUtils.readFile(path);
            Mat imgAux = null;
            try {
               if (imageEvent.getAction().equals("/blur")) {
                  System.out.println("Aplicando blur...");
                  imgAux = OpenCVUtils.blur(img);
               } else if (imageEvent.getAction().equals("/gray")) {
                  System.out.println("Aplicando gray,..");
                  imgAux = OpenCVUtils.gray(img);
               } else if (imageEvent.getAction().equals("/edges")) {
                  System.out.println("Aplicando edges...");
                  imgAux = OpenCVUtils.edges(img);
               }
               // - Simular un tiempo de procesado de 2 segundos
               Thread.sleep(2000);
            } catch (Exception e) {
               System.out.println("Error OpenCV - Exception:" + e.toString());
            }

            String newImagePath = "/tmp" + imageEvent.getAction() + "-" + imageEvent.getImage().toString();
            System.out.println("Ruta de la imagen procesada:" + newImagePath);
            OpenCVUtils.writeImage(imgAux, newImagePath);

            files.put("IMAGE", newImagePath);
            params.put("bucket", "/procesadas");

            // - Subir la imagen procesada al servidor de upload (multipartPostRequest de la clase Http)
            try {
               System.out.println("Enviando petición POST-Multipart...");
               Http.multipartPostRequest(imageEvent.getUploadServer(), headers, params, files);
            } catch (Exception e) {
               System.out.println("Error MultiPartPostRequest - Exception:" + e.toString());
            }
            // Finalizando el metodo y serializando el mensaje
            long unixTimeStop = Instant.now().getEpochSecond();
            System.out.println("Tarea finalizada.");
            JobCompletion jobCompletion = new JobCompletion(worker, imageEvent.getImage(),
                  imageEvent.getTsCreationMessage(), unixTimeStart, unixTimeStop);

            Gson gson = new Gson();
            String jsonJobCompletion = gson.toJson(jobCompletion);
            byte[] messageRabbit = jsonJobCompletion.getBytes();

            // - Notificar consumo del mensaje
            finalChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            // - Envío del mensaje con los tiempos, la imagen y el worker
            finalChannel.basicPublish(RabbitMQConfig.EXCHANGE, RabbitMQConfig.COLA_TIEMPOS, null, messageRabbit);
         } else {
            System.out.println("No hay acciones para el bucket Procesadas");
         }

      };

   }
}
