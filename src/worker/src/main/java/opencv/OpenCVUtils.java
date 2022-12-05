package opencv;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class OpenCVUtils {
    public static Mat readFile(String fileName) {
        Mat img = Imgcodecs.imread(fileName);
       return img;
    }
 
    public static void writeImage(Mat mat, String dest){
        Imgcodecs.imwrite(dest, mat);
    }
 
    public static Mat blur(Mat input){
       Mat destImage = input.clone();
       Imgproc.blur(input, destImage, new Size(3.0, 3.0));
       return destImage;
    }
    
    public static Mat gray(Mat input){
       //Mat gray = new Mat(input.rows(),input.cols(),CvType.CV_8UC1);
       Mat gray = new Mat();
       Imgproc.cvtColor(input, gray, Imgproc.COLOR_RGB2GRAY);
       return gray;
    }
    
    public static Mat edges(Mat input){
       Mat gray = gray(input);
       Imgproc.blur(gray, input, new Size(3, 3));
       int threshold=2;
       Imgproc.Canny(input, input, threshold, threshold * 3, 3, false);
       Mat dest = input.clone();
       Core.add(dest, Scalar.all(0), dest);
       dest.copyTo(dest, input);
       return dest;
    }
 }
 