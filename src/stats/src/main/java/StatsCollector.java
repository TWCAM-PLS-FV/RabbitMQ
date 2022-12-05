import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

import com.google.gson.Gson;

class StatsCollector {
   public static void main(String[] args) throws IOException {
      System.out.println("Accediendo a la cola de tiempos..");
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
      // Obtener un canal para comunicarnos con RabbitMQ
      try {
         c.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
         c.queueDeclare(RabbitMQStuff.COLA_TIEMPOS, true, false, false, null);
         c.queueBind(RabbitMQStuff.COLA_TIEMPOS, RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TIEMPOS);
      } catch (Exception e) {
         System.out.println("Error Canal - Exeption:" + e.toString());
      }
      Channel finalChannel = c;
      boolean autoAck = false;
      finalChannel.basicConsume(RabbitMQStuff.COLA_TIEMPOS, autoAck, "Tiempos",
            new DefaultConsumer(c) {
               public void handleDelivery(String consumerTag,
                     Envelope envelope,
                     AMQP.BasicProperties properties,
                     byte[] body)
                     throws IOException {
                  String routingKey = envelope.getRoutingKey();
                  System.out.println(routingKey);
                  long deliveryTag = envelope.getDeliveryTag();
                  String message = new String(body);
                  System.out.println("Mensaje de la cola:" + message);
                  JobCompletion jobCompletion = new Gson().fromJson(message, JobCompletion.class);
                  long unixTimeStop = Instant.now().getEpochSecond();
                  generarCSV("Tiempos_" + unixTimeStop + ".csv", jobCompletion.toCSV());
                  finalChannel.basicAck(deliveryTag, false);
               }
            });
   }

   public static void generarCSV(String nameFile, String text) {
      BufferedWriter bw = null;
      FileWriter fw = null;
      try {
         File file = new File(nameFile);
         // Si el archivo no existe, se crea.
         if (!file.exists()) {
            file.createNewFile();
         }
         // flag true, indica adjuntar información al archivo.
         fw = new FileWriter(file.getAbsoluteFile(), true);
         bw = new BufferedWriter(fw);
         bw.write(text);
      } catch (IOException e) {
         System.out.println("Error Create/Write File - Exeption:" + e.toString());
      } finally {
         try {
            // Cierra instancias de FileWriter y BufferedWriter
            if (bw != null)
               bw.close();
            if (fw != null)
               fw.close();
         } catch (IOException e) {
            System.out.println("Error CloseFile - Exeption:" + e.toString());
         }
      }
   }

}