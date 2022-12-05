package rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ConexionRabbitMQ {
   private static Channel rabbitc=null;
   /**
    * Implementa el patrón singleton para obtener una instancia
    */
   public static Channel getChannel() throws Exception{
      if (rabbitc==null){
         ConnectionFactory factory = new ConnectionFactory();
         factory.setUsername(RabbitMQConfig.USER);
         factory.setPassword(RabbitMQConfig.PASSWD);
         // Establece el virtual host con el que vamos a interactuar
         factory.setVirtualHost(RabbitMQConfig.VIRTUAL_HOST);
         // Establece la IP de la máquina donde está el broker
         factory.setHost(RabbitMQConfig.HOST);
         // Establece el puerto donde escucha el broker 
         factory.setPort(RabbitMQConfig.PORT);
         Connection conn = factory.newConnection();

         // Creación de un canal para comunicarse con el broker
         rabbitc = conn.createChannel();
     
      }
      return rabbitc;
   }
}
