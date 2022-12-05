import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson; 

class StatsCollector {
   public static void main(String[] args) {
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
      Channel finalChannel = c;
      // Obtener un canal para comunicarnos con RabbitMQ
      try {
         c.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
         c.queueDeclare(RabbitMQStuff.COLA_TIEMPOS, true, false, false, null);
         c.queueBind(RabbitMQStuff.COLA_TIEMPOS, RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TIEMPOS);
      } catch (Exception e) {
         System.out.println("Error Canal - Exeption:" + e.toString());
      }

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
         String message = delivery.getBody().toString();
         JobCompletion jobCompletion = new Gson().fromJson(message, JobCompletion.class);
         generarCSV("Tiempos.csv", jobCompletion.toCSV());
         finalChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      };

   }

   public static void generarCSV(String file, String text) {
        try {
          FileWriter myWriter = new FileWriter(file);
          myWriter.write(text);
          myWriter.close();
          System.out.println("Se ha generado el archivo exitosamente");
        } catch (IOException e) {
          System.out.println("Error File - Exeption: "+e.getMessage());
        }
    }

}