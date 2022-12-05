import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

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
                  generarCSV("Tiempos_"+unixTimeStop+".csv", jobCompletion.toCSV());
                  finalChannel.basicAck(deliveryTag, false);
               }
            });
   }

   public static void generarCSV(String file, String text) {
      try {
         FileWriter myWriter = new FileWriter(file);
         myWriter.write(text);
         myWriter.close();
         System.out.println("Se ha generado el archivo exitosamente");
      } catch (IOException e) {
         System.out.println("Error File - Exeption: " + e.getMessage());
      }
   }

}