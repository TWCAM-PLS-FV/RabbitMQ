package event;
import java.lang.StringBuffer;

public class JobCompletion{
   private String worker;
   private String image;
   private long tsCreationMessage;
   private long tsReceptionWorker;
   private long tsFinalizationWorker;
   // Constructor

   public JobCompletion(String worker, String image, long tsC, long tsR, long tsF){
      this.worker=worker;
      this.image=image;
      this.tsCreationMessage=tsC;
      this.tsReceptionWorker=tsR;
      this.tsFinalizationWorker=tsF;
   }
   // Setters & getters

   // Este método se debe usar para almacenar la información de la instancia en un fichero
   public String toCSV(){
      char delim = ';';
      StringBuffer sb = new StringBuffer();
      sb.append(this.worker);
      sb.append(delim);
      sb.append(this.image);
      sb.append(delim);
      sb.append(this.tsCreationMessage);
      sb.append(delim);
      sb.append(this.tsReceptionWorker);
      sb.append(delim);
      sb.append(this.tsFinalizationWorker);
      return sb.toString();
   }
}
