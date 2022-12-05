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

   public static void main(String[] args) {
      System.out.println("Accediendo a la cola de trabajos..");
      Channel c = null;
      try {
         while (c == null) {
            System.out.println("Obteniendo conexión con la cola de trabajos...");
            c = ConexionRabbitMQ.getChannel();
            Thread.sleep(1000);
         }
      } catch (Exception e) {
         System.out.println("Error Conexión - Exeption:" + e.toString());
      }

      Channel finalChannel = c;
      String worker = args[0];

      // Obtener un canal para comunicarnos con RabbitMQ
      try {
         c.exchangeDeclare(RabbitMQConfig.EXCHANGE, "direct", true);
         c.queueDeclare(RabbitMQConfig.COLA_TRABAJOS, true, false, false, null);
         c.queueBind(RabbitMQConfig.COLA_TRABAJOS, RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_TRABAJOS);
      } catch (Exception e) {
         System.out.println("Error Canal - Exeption:" + e.toString());
      }

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
         System.out.println();
         System.out.println("Iniciando proceso...");
         long unixTimeStart = Instant.now().getEpochSecond();

         String message = new String(delivery.getBody(), "UTF-8");
         System.out.println("Mensaje de la cola:" + message);

         ImageEvent imageEvent = new Gson().fromJson(message, ImageEvent.class);
         System.out.println("Evento: " + imageEvent);

         HashMap<String, String> headers = new HashMap<String, String>();
         HashMap<String, String> params = new HashMap<String, String>();
         HashMap<String, String> files = new HashMap<String, String>();

         String URL = imageEvent.getDownloadURL() + imageEvent.getUploadBucket() + "/" + imageEvent.getImage();
         String path = "/tmp/" + imageEvent.getImage();
         try {
            Http.saveGetRequest(URL, params, headers, path);
         } catch (Exception e) {
            System.out.println("Error SaveRequest - Exception:" + e.toString());
         }

         // OpenCV
         System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
         Mat img = OpenCVUtils.readFile(path);
         Mat imgAux = null;
         try {
            Thread.sleep(2000);
            switch (imageEvent.getAction()) {
               case "/blur": {
                  System.out.println("Aplicando blur...");
                  imgAux = OpenCVUtils.blur(img);
               }
               case "/gray": {
                  System.out.println("Aplicando gray,..");
                  imgAux = OpenCVUtils.gray(img);
               }
               case "/edges": {
                  System.out.println("Aplicando edges...");
                  imgAux = OpenCVUtils.edges(img);
               }
            }
         } catch (Exception e) {
            System.out.println("Error OpenCV - Exception:" + e.toString());
         }

         String newImagePath = "/tmp" + imageEvent.getAction() + "-" + imageEvent.getImage();
         System.out.println("Ruta de la imagen procesada:" + newImagePath);
         OpenCVUtils.writeImage(imgAux, newImagePath);

         files.put("IMAGE", newImagePath);
         params.put("bucket", "/procesadas");

         try {
            System.out.println("Enviando petición POST-Multipart...");
            Http.multipartPostRequest(imageEvent.getUploadServer(), headers, params, files);
         } catch (Exception e) {
            System.out.println("Error MultiPartPostRequest - Exception:" + e.toString());
         }

         long unixTimeStop = Instant.now().getEpochSecond();
         System.out.println("Tarea finalizada.");
         JobCompletion jobCompletion = new JobCompletion(worker, imageEvent.getImage(),
               imageEvent.getTsCreationMessage(), unixTimeStart, unixTimeStop);

         Gson gson = new Gson();
         String jsonJobCompletion = gson.toJson(jobCompletion);
         byte[] messageRabbit = jsonJobCompletion.getBytes();

         finalChannel.basicPublish(RabbitMQConfig.EXCHANGE, RabbitMQConfig.COLA_TIEMPOS, null, messageRabbit);
         finalChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      };

      try {
         c.basicConsume(RabbitMQConfig.COLA_TRABAJOS, false, deliverCallback, consumerTag -> {
         });
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
