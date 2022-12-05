import org.opencv.core.Core;
import org.opencv.core.Mat;

import opencv.OpenCVUtils;

class SampleAppOpenCV{
   public static void main(String[] args) throws Exception{
      System.loadLibrary( Core.NATIVE_LIBRARY_NAME ); // Esto es para trabajar con OpenCV
      String file = args[0];
      Mat img = OpenCVUtils.readFile(file);
      Mat aux = OpenCVUtils.blur(img);
      OpenCVUtils.writeImage(aux,"blurred.png");
      aux = OpenCVUtils.gray(img);
      OpenCVUtils.writeImage(aux,"gray.png");
      aux = OpenCVUtils.edges(img);
      OpenCVUtils.writeImage(aux,"edges.png");
   }
}
