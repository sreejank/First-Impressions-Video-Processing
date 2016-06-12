package linguistic;
//Helper class for linguistic entropy.
public class Word implements Comparable {
	private String word ;
	private int freq ;
	public Word(String w, int f) {
		word=w ;
		freq=f ;
	}
	public String getWord() {
		return word;
	}
	public int getFreq() {
		return freq;
	}
	public String toString() {
		return word+"="+freq ;
	}
	public int compareTo(Word other) {
		return other.getFreq()-getFreq();
	}
	@Override
	public int compareTo(Object arg0) {
		return compareTo((Word)arg0);
	}
}
