package visual;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.highgui.VideoCapture;

/*
 * This code calculates eye entropy defined by Shannon's Entropy formula with the probability
   function defined with a Markov Chain with conditional probabilities of pupil transition from region i to j.
 */
public class EyeEntropy {
	public static int[] findBounds(Mat image) { //Finds the rectangular bounds for the LEFT eye.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		CascadeClassifier eyeDetector = new CascadeClassifier("haarcascade_mcs_eyepair_big.xml");

		MatOfRect eyeDetections = new MatOfRect();
		try {
			eyeDetector.detectMultiScale(image, eyeDetections);
		}
		catch(CvException ex) {
			int[] fake={-2} ;
			return fake ;
		}
		if(eyeDetections.toArray().length==0) {
			int[] noEye={-50};
			return noEye ;
		}
		Rect rect=new Rect(eyeDetections.toArray()[0].x,eyeDetections.toArray()[0].y,eyeDetections.toArray()[0].width,eyeDetections.toArray()[0].height) ;
		if(eyeDetections.toArray().length>1) {
			Rect large=new Rect(eyeDetections.toArray()[0].x,eyeDetections.toArray()[0].y,eyeDetections.toArray()[0].width,eyeDetections.toArray()[0].height) ;

			for(Rect r:eyeDetections.toArray()) {
				if(r.size().width>large.width) {
					large=r ;

				}
			}
			rect=new Rect(large.x,large.y,large.width,large.height) ; ;
		}
		int lowX=rect.x ; int highX=rect.x+rect.width ;
		int lowY=rect.y ; int highY=rect.y+rect.height ;
		highX=(lowX+highX)/2;
		highX=(int)(highX*0.98) ;
		int[] ans={lowX,highX,lowY,highY} ;
		return ans ;
	}
	//Finds the approximation of the center of the eye pupil through RGB values.
	public static Point findPupil(Mat image) {
		int[] bounds=findBounds(image);
		if(bounds[0]<0) {
			System.out.println("No eye detected. Returning empty Point.");
			return new Point();
		} 
		ArrayList<Integer> pupilX=new ArrayList<Integer>();
		ArrayList<Integer> pupilY=new ArrayList<Integer>();
		for(int y=bounds[2];y<bounds[3];y++) {
			for(int x=bounds[0];x<((bounds[0]+bounds[1])/2);x++) { //old x bound: bounds[1]
				int val=image.getRGB(x, y);
				Color c=new Color(val);
				if(c.getRed()<50 && c.getBlue()<50 && c.getGreen()<50) {
					pupilX.add(x);
					pupilY.add(y);
				}
			}
		}
		if(pupilX.size()==0 || pupilY.size()==0) {
			pupilX.add(0); pupilY.add(0);
		}
		int minX=pupilX.get(0);
		int maxX=pupilX.get(0);
		for(int x:pupilX) {
			if(x>maxX) {
				maxX=x ;
			}
			else if(x<minX) {
				minX=x ;
			}
		}
		int minY=pupilY.get(0);
		int maxY=pupilY.get(0);
		for(int y:pupilY) {
			if(y>maxY) {
				maxY=y ;
			}
			else if(y<minY) {
				minY=y;
			}
		}
		int finalX=(minX+maxX)/2 ;
		int finalY=(minY+maxY)/2 ;
		if(finalX==0 && finalY==0) {

		}
		Point pupil=new Point(finalX,finalY);
		return pupil ;
	}
	//Seperates the eye into 200-300 rectangular regions and consolidates them into an array.
	public static Rect[] rectRegions(Mat image) {
		int[] bounds=findBounds(image);
		if(bounds[0]<0) {

			return null ;
		}
		int minX= bounds[0];
		int maxX=bounds[1] ;
		int minY=bounds[2];
		int maxY=bounds[3];
		int width=maxX-minX ;
		int height=maxY-minY ;
		//Rect whole=new Rect(minX,minY,width,height);
		Rect[] regions=new Rect[100000];
		int xLength=(int)(width/15) ;
		int yLength=(int)(height/15);
		if(xLength==0) {
			xLength++ ;
		}
		if(yLength==0) {
			yLength++ ;
		}
		int i=0 ;
		for(int x=minX;x<maxX;x+=xLength) {
			for(int y=minY;y<maxY-yLength;y+=yLength) {
				regions[i]=new Rect(x,y,xLength,yLength);
				i++ ;

			}
		}

		/* For drawing the rectangular regions of a particular frame. Output file is RECTREGIONSFOR<filename>.png
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Scalar s=new Scalar(120,50,120);

		for(Rect rect:regions) {
			if(rect!=null) {
				Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
						s);
			}
		}


		String output="RECTREGIONSFOR"+fileName+".png";
		Highgui.imwrite(output, image);
		 */

		return regions ;

	}
	//Goes through a frame and writes the point where the pupil is in terms of region. It
	//prints out the pupil location based on the 200-300 region system.
	public static String writePoint(Mat image){
		Point pupil=findPupil(image);
		//System.out.println(pupil);
		if(pupil.x==0 && pupil.y==0) {
			return "FRAME NOT FOUND: INTERPOLATE DATA" ;

		}
		Rect[] regions=rectRegions(image);
		ArrayList<Rect> inside=new ArrayList<Rect>();
		for(Rect r:regions) {
			if(r!=null) {
				if(pupil.inside(r)) {
					inside.add(r);
				}
			}
		}
		Rect place ;
		if(inside.size()>1) {
			int index=0 + (int)(Math.random() * ((inside.size()-1) - 0) + 1) ; //random number
			place=inside.get(index);
		}
		else if(inside.size()==0) {
			return ("POINT OUTSIDE OF MARKOV REGIONS");
		}
		else {
			place=inside.get(0);
		}
		int i=0 ;
		int index=-1 ;
		for(Rect r:regions) {
			if(place.equals(r)) {
				index=i ;
			}
			i++ ;
		}
		//System.out.println(regions.size());
		return index+"";
	}
	public static void writeRegionPoints(String vidFileName){
		VideoCapture vidreader=VideoCapture();
		vidreader.open(vidFileName);
		PrintWriter pw=new PrintWriter("Initial Eye Data for video "+vidFileName.substring(0,vidFileName.length()-3)+".txt");
		Mat frame=new Mat();
		int frameNumber=0 ;
		while(vidreader.grab()) {
			Mat image=vidreader.retrieve();
			pw.println(frameNumber+" : "+writePoint(image));
			frameNumber++;
		}
		System.out.println("Initial data for video "+vidFileName+" created.");
		pw.close();
	}
	/*Characterizes each 200-300 rectangles into 9 different regions, tailored to each video.
	Saves this data into "Markov Regions for <filename.jpg>." This data is inputted into convertToNine.
	The inputs are the 2 horizontal and 2 verticle lines that break up the eye into 9 regions. The fileName
	parameter is the specific chosen frame for the respective where the 9 markov regions were determined.
	Rectangle parameters by video (x1,x2,y1,y2):
	Vid1-217,224,369,385
	Vid2-197,202,397,411
	Vid3-211,217,453,468
	Vid4-192,198,378,390
	Vid5-232,237,465,479
	Vid6-301,305,401,412
	Vid7-248,253,371,382
	Vid8-224,229,418,427
	Vid9-237,243,402,415
	 */
	public static void writeRegions(String chosenFrame, int x1, int x2, int y1, int y2){
		PrintWriter pw=null;
		try  {
			pw=new PrintWriter(new File("Markov Regions for "+chosenFrame.replace(".jpg", "")+".txt"));
		}
		catch(IOException ex) {
			System.out.println("Difficulty writing output file. exiting.");
			System.exit(1);
		}
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat image = Highgui.imread(chosenFrame);
		//
		Rect[] nine= {
				new Rect(new Point(0,0), new Point(y1,x1)),//Region 1
				new Rect(new Point(y1,0), new Point(y2,x1)),//Region 2
				new Rect(new Point(y2,0), new Point(image.width(),x1)),//Region 3
				new Rect(new Point(y2,x1), new Point(image.width(),x2)),//Region 6
				new Rect(new Point(y2,x2), new Point(image.width(),image.height())),//Region 9
				new Rect(new Point(y1,x2), new Point(y2,image.height())),//Region 8
				new Rect(new Point(0,x2), new Point(y1,image.height())),//Region 7
				new Rect(new Point(0,x1), new Point(y1,x2)),//Region 4	
				new Rect(new Point(y1,x1), new Point(y2,x2))//Region 5
		} ;

		Scalar s=new Scalar(255,255,255);

		for(Rect rect:nine) {
			if(rect!=null) {
				Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
						new Scalar(0,0,0));
			}
		}


		Rect[] regions=rectRegions(chosenFrame);

		for(int i=0;i<432;i++) {
			if(regions[i]!=null) {
				Rect markov=regions[i];
				//Core.rectangle(image, new Point(markov.x, markov.y), new Point(markov.x + markov.width, markov.y + markov.height),
				//	s);

				ArrayList<Integer> in=new ArrayList<Integer>();
				for(int j=0;j<nine.length;j++) {
					if(markov!=null && inside(nine[j],markov)) {
						in.add(j);
					}
				}
				int index ;
				if(in.size()>1) {
					Random r=new Random();
					index=in.get(r.nextInt(in.size()));
				}
				else if(in.size()==0) {
					index=-1 ;
				}
				else {
					index=in.get(0);
				}

				pw.println(i+" : "+convert(index));//552-559 null


			}

		}
		pw.close();
		System.out.println("DONE FOR "+chosenFrame);

		String output="MARKOVFOR"+chosenFrame.replace("jpg","")+".png";
		Highgui.imwrite(output, image);

	}


	//Outputs the location of the pupil based on the 9 region system for every frame.
	//Input data is the text generated from writeRegionPoints. The mapFile are the markovRegions documents for each video.
	//Outputs data into "Final Eye Data for <video number>.txt"

	public static void convertToNine(String fileName, String mapFile, String vidname) throws IOException {
		File file=new File(fileName);
		Scanner scan=new Scanner(file);
		TreeMap<Integer,Integer> map=makeMap(mapFile);
		PrintWriter pw=new PrintWriter("Final Eye Data for "+vidname+".txt");
		while(scan.hasNextLine()) {
			String nextLine=scan.nextLine();
			if(!nextLine.contains("FRAME")) {
				String[] split=nextLine.split(" : ");
				int frame=Integer.parseInt(split[0]);
				int markov=Integer.parseInt(split[1]);
				if(markov>399) {
					markov=markov-100 ;
				}
				markov=map.get(markov);
				pw.println(frame+" : "+markov);
				//System.out.println(frame+" : "+markov);
			}
			else {
				pw.println("FRAME NOT FOUND: INTERPOLATE DATA");
			}
		}
		scan.close();
		pw.close();
		System.out.println("File for "+fileName+" created.");
	}

	
	//Calculates entropy based on Shannon entropy using microstates defined through a Markov chain model.

	public static double markovEntropy(ArrayList<Double> vals) {
		double[][] probs=new double[9][9];
		for(int i=1;i<=9;i++) {
			for(int j=1;j<=9;j++) {
				for(int k=0;k<vals.size()-1;k++) {
					if(vals.get(k)==i && vals.get(k+1)==j) {
						probs[i-1][j-1]++ ;
					}
				}
			}
		}
		double[] sums=new double[9];
		for(int i=0;i<9;i++) {
			double sum=0 ;
			for(int j=0;j<9;j++) {
				if(probs[i][j]>0) {
					double p=probs[i][j]/(double)vals.size();
					double logp=Math.log(p);
					double log2=Math.log(2);
					sum+=Math.abs(p*(logp/log2));
				}
			}

			sums[i]=Math.abs(sum) ;
		}

		double entropy=0 ;
		for(int i=0;i<sums.length;i++) {
			entropy+=sums[i]*(1.0/9.0);
		}
		return entropy ;
	}
	public static void main(String[] args) throws IOException {
		//Generate data to find the pupil in terms of the 200-300 region system.
		
		writeRegionPoints("vid1.mp4");
		writeRegionPoints("vid2.mp4");
		writeRegionPoints("vid3.mp4");
		writeRegionPoints("vid4.mp4");
		writeRegionPoints("vid5.mp4");
		writeRegionPoints("vid6.mp4");
		writeRegionPoints("vid7.mp4");
		writeRegionPoints("vid8.mp4");
		writeRegionPoints("vid9.mp4");
		
		//Write the regional data for converting to 9 regions.

		writeRegions("BTANDEM001 HD 0016.txt",217,224,369,385);
		writeRegions("BTANDEM002 HD 0025.jpg",197,202,397,411);
		writeRegions("BTANDEM003 HD 0022.jpg",211,217,453,468);
		writeRegions("BTANDEM004 HD 0017.jpg",192,198,378,390);
		writeRegions("BTANDEM005 HD 0017.jpg",232,237,465,479);
		writeRegions("BTANDEM006 HD 0015.jpg",301,305,401,412);
		writeRegions("BTANDEM007 HD 0020.jpg",248,253,371,382);
		writeRegions("BTANDEM008 HD 0018.jpg",224,229,418,427);
		writeRegions("BTANDEM009 HD 0015.jpg",237,243,402,415);

		//Convert the initial eye data to 9 regions, the final eye data.
		convertToNine("Initial Eye Data for vid1.txt","Markov Regions for BTANDEM001 HD 0016.txt","vid1.mp4");
		convertToNine("Initial Eye Data for vid2.txt","Markov Regions for BTANDEM002 HD 0025.txt","vid2.mp4");
		convertToNine("Initial Eye Data for vid3.txt","Markov Regions for BTANDEM003 HD 0022.txt","vid3.mp4");
		convertToNine("Initial Eye Data for vid4.txt","Markov Regions for BTANDEM004 HD 0017.txt","vid4.mp4");
		convertToNine("Initial Eye Data for vid5.txt","Markov Regions for BTANDEM005 HD 0017.txt","vid5.mp4");
		convertToNine("Initial Eye Data for vid6.txt","Markov Regions for BTANDEM006 HD 0015.txt","vid6.mp4");
		convertToNine("Initial Eye Data for vid7.txt","Markov Regions for BTANDEM007 HD 0020.txt","vid7.mp4");
		convertToNine("Initial Eye Data for vid8.txt","Markov Regions for BTANDEM008 HD 0018.txt","vid8.mp4");
		convertToNine("Initial Eye Data for vid9.txt","Markov Regions for BTANDEM009 HD 0015.txt","vid9.mp4");

		//Prints out entropies
		System.out.println("--------------FINAL ENTROPIES------------------------------");
		System.out.println(markovEntropy(readData("Final Eye Data for vid1.txt")));
		System.out.println(markovEntropy(readData("Final Eye Data for vid2.txt")));
		System.out.println(markovEntropy(readData("Final Eye Data for vid3.txt")));
		System.out.println(markovEntropy(readData("Final Eye Data for vid4.txt")));
		System.out.println(markovEntropy(readData("Final Eye Data for vid5.txt")));
		System.out.println(markovEntropy(readData("Final Eye Data for vid6.txt")));
		System.out.println(markovEntropy(readData("Final Eye Data for vid7.txt")));
		System.out.println(markovEntropy(readData("Final Eye Data for vid8.txt")));
		System.out.println(markovEntropy(readData("Final Eye Data for vid9.txt")));

	}

	//Helper method for writeRegions.
	private static boolean inside(Rect outside, Rect inside) {
		Point p1=new Point(inside.x+inside.width,inside.y);
		Point p2=new Point(inside.x,inside.y+inside.height);
		return outside.contains(p1) || outside.contains(p2) || outside.contains(inside.tl()) || outside.contains(inside.br());
	}

	//Helper method for writeRegions. 
	private static int convert(int index) {

		switch(index){
		case 0:
			return 1 ;
		case 1:
			return 2 ;
		case 2:
			return 3 ;
		case 3:
			return 6 ;
		case 4:
			return 9 ;
		case 5:
			return 8 ;
		case 6:
			return 7 ;
		case 7:
			return 4 ;
		case 8:
			return 5 ;
		default:
			return -1 ;
		}
	}

	//Helper method for convertToNine. The input file is the markovRegions document.
	private static TreeMap<Integer,Integer> makeMap(String fileName) throws FileNotFoundException {
		File file=new File(fileName);
		Scanner scan=new Scanner(file);
		TreeMap<Integer,Integer> map=new TreeMap<Integer,Integer>();
		while(scan.hasNextLine()) {
			String nextLine=scan.nextLine();
			String[] split=nextLine.split(" : ");
			map.put(Integer.parseInt(split[0]), Integer.parseInt(split[1])) ;
		}
		scan.close();
		return map;
	}
	//Helper method for markovEntropy. Reads eye data.
	private static ArrayList<Double> readData(String fileName) throws FileNotFoundException {
		Scanner scan=new Scanner(new File(fileName));
		ArrayList<Double> vals=new ArrayList<Double>();
		while(scan.hasNextLine()) {
			String line=scan.nextLine();
			if(!line.contains("FRAME")) {
				String[] split=line.split(" : ");
				vals.add(Double.parseDouble(split[1]));
			}
		}
		scan.close();
		return vals ;
	}
}
