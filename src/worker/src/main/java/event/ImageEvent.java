package event;

public class ImageEvent{
   private String downloadURL;
   private String action;
   private String uploadServer;
   private String uploadBucket;
   private long tsCreationMessage;
   private String image;

   public ImageEvent(){}


   public String getImage() {
      return this.image;
   }

   public void setImage(String image) {
      this.image = image;
   }

   public String getDownloadURL() {
      return this.downloadURL;
   }

   public void setDownloadURL(String downloadURL) {
      this.downloadURL = downloadURL;
   }

   public String getAction() {
      return this.action;
   }

   public void setAction(String action) {
      this.action = action;
   }

   public String getUploadServer() {
      return this.uploadServer;
   }

   public void setUploadServer(String uploadServer) {
      this.uploadServer = uploadServer;
   }

   public String getUploadBucket() {
      return this.uploadBucket;
   }

   public void setUploadBucket(String uploadBucket) {
      this.uploadBucket = uploadBucket;
   }

   public long getTsCreationMessage() {
      return this.tsCreationMessage;
   }

   public void setTsCreationMessage(long tsCreationMessage) {
      this.tsCreationMessage = tsCreationMessage;
   }


   @Override
   public String toString() {
      return "{" +
         " downloadURL='" + getDownloadURL() + "'" +
         ", image='" + getImage() + "'" +         
         ", action='" + getAction() + "'" +
         ", uploadServer='" + getUploadServer() + "'" +
         ", uploadBucket='" + getUploadBucket() + "'" +
         ", tsCreationMessage='" + getTsCreationMessage() + "'" +

         "}";
   }
}
