package linguistic;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeMap;
/*
 * This is the code for calculating Shannon linguistic entropy by word ranks. This was used for the transcripts.
 */

public class LinguisticEntropy {
	public static double calculateEntropy(File file) { //Calculates the linguistic entropy by word.
		ArrayList<String> words=processFile(file);
		TreeMap<String, Integer> map=new TreeMap<String, Integer>();
		for(int i=0;i<words.size();i++) {
			if(!map.containsKey(words.get(i))) {
				map.put(words.get(i),1) ;
			}
			else {
				map.put(words.get(i), map.get(words.get(i))+1) ;
			}
		}
		map.remove("");
		//System.out.println(map); 
		//Uncommenting the above line allows you to get a list of the word frequencies.
		double entropy=0.0 ;
		for(String s:map.keySet()) {
			double p=(double)map.get(s)/(double)words.size();
			double log2=(double)Math.log(2.0);
			double logp=(double)Math.log(p);
			double add= Math.abs(p*(logp/log2)) ;
			entropy+= Math.abs(add) ;
		}
		return entropy ;
	}
	public static ArrayList<String> processFile(File file) {// Goes through transcript file and stores each word into an ArrayList.
		Scanner scan=null ;
		try {
			scan=new Scanner(file);
		}
		catch(FileNotFoundException ex) {
			System.out.println("File not found, quitting program.");
			System.exit(0);
		}
		ArrayList<String> list=new ArrayList<String>();
		while(scan.hasNextLine()) {
			String line=scan.nextLine();
			line=line.toLowerCase();
			//System.out.println(line);
			String[] words=line.split(" ");
			for(String s:words) {
				list.add(s);
			}
		}
		return list ;
	}
	public static void main(String[] args) {
		//goes through each transcript and calculates the linguistic entropy.
		File d=new File("transcripts");
		File[] files=d.listFiles();
		Arrays.sort(files);
		int i=1 ;
		for(File f: files) {
			System.out.println(i+" "+calculateEntropy(f));
			i++ ;
		}
		}
}
