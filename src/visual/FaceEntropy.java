package visual;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.highgui.VideoCapture;

//Finds relative faceshift for each frame in the video (folder "00<x>")
public class FaceEntropy {
	//Finds the rectangular region of the face. Largest rectangle is the face. 
	public static Rect findFace(Mat image) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		CascadeClassifier faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml.xml");
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);
		if(faceDetections.toArray().length==0) {
			return null ;
		}
		//In case false faces are found. In a talking-head video, the real face should be the largest rectangle.
		Rect biggest=faceDetections.toArray()[0] ;

		for (Rect rect : faceDetections.toArray()) {
			if(biggest.width<rect.width) {
				biggest=new Rect(rect.tl(),rect.br());
			}
		}


		return biggest ;
	}
	//Helper method for collectData.
	private static double distance(Point p1, Point p2) {
		double x=(double)p2.x-p1.x ;
		x=x*x ;
		double y=(double)p2.y-p1.y ;
		y=y*y ;
		double sum=x+y ;
		return Math.sqrt(sum);
	}
	//Collects faceshift for each frame (how far approximate middle of one's face has shifted). Outputs it to "Face Shift Data For <directory name>.txt"
	public static void collectData(String vidFileName) throws FileNotFoundException {
		VideoCapture vidreader=new VideoCapture();
		vidreader.open(vidFileName);
		System.out.println("STARTING "+vidFileName);
		PrintWriter pw=new PrintWriter("Face Shift Data For "+vidFileName.substring(0,vidFileName.length()-3)+".txt");
		Mat frame=new Mat();
		vidreader.read(frame);
		if(frame==null) {
			System.out.println("Could not read frame. Returning...")
			return;
		}
		Rect first=findFace(frame);
		int i=1;
		while(vidreader.grab()) {
			frame=vidreader.retrieve();
			Rect r=findFace(frame);
			if(r!=null) {
				Point rPoint=new Point(r.x+(r.width/2),r.y-(r.height/2));
				Point firstPoint=new Point(first.x+(first.width/2),first.y-(first.height/2));
				first=r ;
				pw.println(i+" : "+distance(firstPoint,rPoint));
				i++;
			}
			else {
				pw.println(i+" : FACE NOT FOUND");
				i++;
			}
		}
		pw.close();
		System.out.println("FINISHED WITH "+vidFileName);
	}
	//Collects all faceshift data for each video.
	public static void main(String[] args) throws FileNotFoundException {
		collectData("001.mp4");
		collectData("002.mp4");
		collectData("003.mp4");
		collectData("004.mp4");
		collectData("005.mp4");
		collectData("006.mp4");
		collectData("007.mp4");
		collectData("008.mp4");
		collectData("009.mp4");
	}
}
