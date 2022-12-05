package http;

import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.io.File;

public class Http {

   private static String getFilename(String path){
      String file=null;
      int index = path.lastIndexOf("/");
      if (index > 1)
         file = path.substring(index+1);
      else
         file=path;
      return file;
   }

   private static String buildParams(HashMap<String,String> params) throws Exception {
      StringBuilder sb = new StringBuilder();
      for (String param : params.keySet()) {
         sb.append(param);
         sb.append('=');
         sb.append(URLEncoder.encode(params.get(param), "UTF-8"));
         sb.append('&');
      }
      sb.deleteCharAt(sb.length() - 1);
      return sb.toString();
   }

   /**
    * 
    * @param url the URL of the resource to download
    * @param params params of the request
    * @param headers headers of the request
    * @param file the path and name of the file to save the body of the response
    * @return the status code of the server
    * @throws Exception
    */
   public static int saveGetRequest(String url, HashMap<String, String> params, HashMap<String, String> headers, String file)
         throws Exception {
      
      String aux_url = url;
      if (params != null)
         if (params.size()>0)
            aux_url = aux_url + "?" + buildParams(params);
      URL u = new URL(aux_url);
      HttpURLConnection c = (HttpURLConnection) u.openConnection();
      c.setRequestMethod("GET");
      c.setDoInput(true);
      if (headers != null) {
         for (String keyHeader: headers.keySet())
            c.setRequestProperty(keyHeader, headers.get(keyHeader));
      }
      InputStream in = c.getInputStream();
      int status = c.getResponseCode();
      if (status == HttpURLConnection.HTTP_OK) {
         byte[] data = new byte[1024];
         OutputStream out = new FileOutputStream(file);
         int nbytes;
         while ((nbytes = in.read(data)) != -1)
            out.write(data, 0, nbytes);
         in.close();
         out.flush();
         out.close();
      }
      return c.getResponseCode();
   }


    /**
    * Performs a multipart POST request
    *
    * @param url              url of the service
    * @param params           params to be passed in the body as multipart
    * @param headers          headers of the request
    * @param files            files to be passed in the body as multipart
    * @return the status of the response of the upload server
    * @throws Exception
    */
    public static int multipartPostRequest(String url, HashMap<String,String> headers, HashMap<String,String> params,
      HashMap<String,String> files) throws Exception {

      // Voy a usar la librería HTTPClient de Apache para realizar la petición
      // Multipart

      HttpPost post = new HttpPost(url);

      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

      // Add parameters and files to the body of the request
      if (params != null)
      for (String p: params.keySet())
         builder.addTextBody(p, params.get(p), ContentType.DEFAULT_BINARY);

      if (files != null)
         for (String f: files.keySet()){
            File file = new File(files.get(f));
            builder.addBinaryBody(f, file, ContentType.DEFAULT_BINARY, 
              getFilename(files.get(f)));
         }
         
      HttpEntity entity = builder.build();
      post.setEntity(entity);
   

      // Make request
      CloseableHttpClient client = HttpClients.createDefault();

      if (headers != null)
         for (String h: headers.keySet())
            post.setHeader(h, headers.get(h));
      
      HttpResponse response = client.execute(post);

      return response.getStatusLine().getStatusCode();
   }
}
