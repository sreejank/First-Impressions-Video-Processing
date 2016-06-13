package visual;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeMap;
import org.opencv.highgui.VideoCapture;

import javax.imageio.ImageIO;

import org.opencv.core.Point;
//Finds the angles of body tilt for the silhouette videos "bodymovement<X>"
public class BodyTilt {
	//Convert mat to BufferedImage
	public static BufferedImage mat2buffimg(Mat image) {
		byte[] data = new byte[320 * 240 * (int)image.elemSize()];
		image.get(0,0,data);
		int type;
		if(image.channels()==1) {
			type=BufferedImage.TYPE_BYTE_GRAY;
		}
		else {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage ret=new BufferedImage(320, 240, type);
		ret.getRaster().setDataElements(0, 0, 320, 240, data);
		return ret;
	}
	//Gets the angle of body tilt for one frame, rounded to nearest 10th.
	public static int getAngle(Mat frame){
		BufferedImage image=mat2buffimg(frame);
		boolean foundIt=false ;
		int midY=image.getHeight()/2 ;
		int xval=0 ;
		int midleftX=-1 ;
		while(!foundIt) {
			Color c=new Color(image.getRGB(xval, midY));
			if(c.getBlue()>20 || c.getGreen()>20 || c.getRed()>20) {
				foundIt=true ;
				midleftX=xval ;
			}
			xval++ ;
		}
		xval=image.getWidth()-1 ;
		int midrightX=-1 ;
		foundIt=false ;
		while(!foundIt) {
			Color c=new Color(image.getRGB(xval, midY));
			if(c.getBlue()>20 || c.getGreen()>20 || c.getRed()>20) {
				foundIt=true ;
				midrightX=xval ;
			}
			xval-- ;
		}
		int topX=(midrightX+midleftX)/2;
		int topY=-1 ;
		foundIt=false ;
		xval=topX ;
		int yval=image.getHeight()/2 ;
		while(yval>0 && !foundIt) {
			Color c=new Color(image.getRGB(xval, yval));
			if(c.getBlue()<40 && c.getGreen()<40 && c.getRed()<40) {
				foundIt=true ;
				topY=yval ;
			}
			yval-- ;
		}
		
		ArrayList<Integer> xvalues=new ArrayList<Integer>();
		for(int x=0;x<image.getWidth();x++) {
			Color c=new Color(image.getRGB(x, topY)) ;
			if(c.getBlue()<20 && c.getGreen()<20 && c.getRed()<20) {
				xvalues.add(x);
			}
		}
		int least=xvalues.get(0);
		int greatest=xvalues.get(0);
		for(int x:xvalues) {
			if(x>greatest) {
				greatest=x ;
			}
			if(x<least) {
				least=x ;
			}
		}
		topX=(greatest+least)/2 ;
		Point topPoint=new Point(topX,topY);
		foundIt=false ;
		Point bottomLeft=new Point();
		for(int x=0;x<image.getWidth() && !foundIt;x++) {
			Color c=new Color(image.getRGB(x, image.getHeight()-1));
			if(c.getBlue()>20 || c.getGreen()>20 || c.getRed()>20) {
				bottomLeft=new Point(x,image.getHeight()-1);
				foundIt=true ;
			}
		}
		foundIt=false ;
		Point bottomRight=new Point();
		for(int x=image.getWidth()-1;x>=0 && !foundIt;x--) {
			Color c=new Color(image.getRGB(x, image.getHeight()-1));
			if(c.getBlue()>20 && c.getGreen()>20 && c.getRed()>20) {
				bottomRight=new Point(x,image.getHeight()-1);
				foundIt=true ;
			}
		}
		double bottomXVal=(bottomLeft.x+bottomRight.x)/2 ;
		double bottomYVal=(bottomLeft.y+bottomRight.y)/2 ;
		Point bottomPoint=new Point(bottomXVal,bottomYVal);
		
		double deltaX=Math.abs(topPoint.x-bottomPoint.y);
		double deltaY=Math.abs(topPoint.y-bottomPoint.y);
		double angle=deltaX/deltaY ;
		angle=Math.atan(angle);
		angle=angle*(180/Math.PI);
		int finalAngle=(int)angle ;
		int ones=finalAngle%10;
		if(ones>=5) {
			finalAngle+=(10-ones);
		}
		else {
			finalAngle-=ones;
		}
		return finalAngle ;
	}
	//Finds the angle of all frames in a directory. Outputs it to: <folder> data.txt
	public static void collectData(String vidFileName){
		System.out.println("STARTING "+vidFileName);
		PrintWriter pw=null;
		try {
			pw=new PrintWriter(vidFileName.substring(0,vidFileName.length()-3)+" data.txt");
		}
		catch(IOException ex) {
			System.out.println("Difficulty in writing to output file. Exiting");
			System.exit(1);
		}
		VideoCapture vidreader=new VideoCapture();
		vidreader.open(vidFileName);
		while(vidreader.grab()) {
			Mat frame=vidreader.retrieve();
			pw.println(frame);
		}
		System.out.println("DONE WITH "+vidFileName);
		pw.close();
		vidreader.release();
	}
	//Calculates shannon entropy.
	public static double shannonEntropy(ArrayList<Double> values) { 
		TreeMap<Double,Integer> map=new TreeMap<Double,Integer>();
		for(int i=0;i<values.size();i++) {
			if(!map.containsKey(values.get(i))) {
				map.put(values.get(i), 1);
			}
			else {
				map.put(values.get(i), map.get(values.get(i))+1);				
			}
		}
		//System.out.println(map);
		double entropy=0 ;
		for(double val: map.keySet()) {
			double p=(double)(map.get(val))/(double)values.size();
			double log2=Math.log(2.0) ;
			double logp=Math.log(1/p);
			double add=p*(logp/log2) ;
			entropy+=Math.abs(add) ;
		}
		return entropy ;
	}
	//Computes the body shift data for all body videos. Prints out each shannon entropy.
	public static void main(String[] args) throws IOException {
		/*
		collectData("bodymovement1");
		collectData("bodymovement2");
		collectData("bodymovement3");
		collectData("bodymovement4");
		
		collectData("bodymovement5");
		collectData("bodymovement6");
		collectData("bodymovement7");
		collectData("bodymovement8");
		collectData("bodymovement9");
		*/
		System.out.println(shannonEntropy(readData("bodymovement1 data.txt")));
		System.out.println(shannonEntropy(readData("bodymovement2 data.txt")));
		System.out.println(shannonEntropy(readData("bodymovement3 data.txt")));
		System.out.println(shannonEntropy(readData("bodymovement4 data.txt")));
		System.out.println(shannonEntropy(readData("bodymovement5 data.txt")));
		System.out.println(shannonEntropy(readData("bodymovement6 data.txt")));
		System.out.println(shannonEntropy(readData("bodymovement7 data.txt")));
		System.out.println(shannonEntropy(readData("bodymovement8 data.txt")));
		System.out.println(shannonEntropy(readData("bodymovement9 data.txt")));
	}
	private static ArrayList<Double> readData(String fileName){
		Scanner scan=null;
		try {
			scan=new Scanner(new File(fileName));
		}
		catch(IOException ex) {
			System.out.println("Could not find data file for "+fileName+". Exiting");
			System.out.println(1);

		}
		ArrayList<Double> vals=new ArrayList<Double>();
		while(scan.hasNextLine()) {
			String nextLine=scan.nextLine();
			if(!nextLine.equals("")) {
				vals.add(Double.parseDouble(nextLine));
			}
		}
		scan.close();
		return vals ;
	}
}
