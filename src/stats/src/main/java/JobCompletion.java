import java.lang.StringBuffer;

class JobCompletion{
   private String worker;
   private String image;
   private long tsCreationMessage;
   private long tsReceptionWorker;
   private long tsFinalizationWorker;
   // Constructor
   // Setters & getters

   // Este método se debe usar para almacenar la información de la instancia en un fichero
   public String toCSV(){
      char delim = ';';
      StringBuffer sb = new StringBuffer();
      sb.append(worker);
      sb.append(delim);
      sb.append(image);
      sb.append(delim);
      sb.append(tsCreationMessage);
      sb.append(delim);
      sb.append(tsReceptionWorker);
      sb.append(delim);
      sb.append(tsFinalizationWorker);
      return sb.toString();
   }
}
