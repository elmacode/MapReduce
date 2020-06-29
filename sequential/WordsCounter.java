
/** Question 1 **/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


class WordsCounter {

	private HashMap<String, Integer> occurences;
	private int wordsNb;
	private long perfTime;

	public WordsCounter(){
		this.occurences = new HashMap<String, Integer>();
		this.wordsNb=0;

		
	}

	public HashMap<String, Integer> sortByValue(HashMap<String, Integer> wordCounts) {

        return wordCounts.entrySet()
                .stream()
                .sorted((HashMap.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(HashMap.Entry::getKey, HashMap.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

	}
	
	public HashMap<String, Integer> sortByKey(HashMap<String, Integer> wordCounts) {

        return wordCounts.entrySet()
                .stream()
                .sorted((HashMap.Entry.<String, Integer>comparingByKey()))
                .collect(Collectors.toMap(HashMap.Entry::getKey, HashMap.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

    }

	public void count(String filename)  {
		
		File file = new File(filename); 
		BufferedReader br = null; 
		int wordNumber = 0;
		try {
			br = new BufferedReader(new FileReader(file));
			String line; 
			
			long startTime = System.currentTimeMillis();

			while ((line = br.readLine()) != null){
				//for each line we split into several words 
				String[] words = line.split("\\s+");
				this.wordsNb = this.wordsNb + words.length;
				for(String x : words){
					//!=null means the string is already in the map
					if (occurences.get(x) != null) {
						int increment = occurences.get(x)+1;
						occurences.replace(x,increment);
					
					//if not we put its value as
					//it is the first time we meet it
					} else {
						occurences.put(x,1);
					}
				}
				
				
			} 
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Nb de mots: "+this.wordsNb);
			this.perfTime = totalTime;
			System.out.println("Temps pour compter le nb d'occ: "+totalTime+"ms");
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				br.close();
			} catch (Exception e) {}
		}
	}
	public void printHashMap(){
		long startTime = System.currentTimeMillis();
		//it will be sorted alphabetically
		HashMap<String, Integer> sortedAlphabetically = sortByKey(this.occurences);
		//it will be sorted alphabetically and by occurence
		HashMap<String, Integer> sortedByCount = sortByValue(sortedAlphabetically);
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;

		System.out.println("Temps pour trier: "+totalTime+"ms");
		// for (String word: sortedByCount.keySet()){
        //     String key = word.toString();
        //     String value = sortedByCount.get(word).toString();  
        //     System.out.println(key + " " + value);  
		// } 
		// System.out.println(this.occurences.size());
	}

	public void writeArray(ArrayList<Integer> wordsNumbers, String filename) throws IOException {
		BufferedWriter writer = null;
    
        try {
		  writer = new BufferedWriter(new FileWriter(filename));
		  for(int i=0;i<wordsNumbers.size();i++){
			writer.write(Integer.toString(wordsNumbers.get(i))+",");
		  }
          
        } catch (Exception e) {
          e.printStackTrace();
        }
        finally {
          writer.close();
        }
        
    }

	public static void main(String[] args) throws IOException {

		ArrayList<Integer> wordsNumbers = new ArrayList<Integer>();
		ArrayList<Integer> globalTimes = new ArrayList<Integer>();

		Long startTime, endTime;
		// BufferedReader currText = null;
		
		File corpusDir = new File("corpus/");
        String[] texts = corpusDir.list();

		for(String text : texts){
			startTime = System.currentTimeMillis();
			WordsCounter wordCount = new WordsCounter();
			wordCount.count("corpus/"+text);
			wordCount.printHashMap();
			endTime = System.currentTimeMillis();
			Long globalTime = endTime-startTime;
			globalTimes.add(globalTime.intValue());
			wordsNumbers.add(wordCount.wordsNb);

		}

		WordsCounter wordCount = new WordsCounter();
		wordCount.writeArray(wordsNumbers,"NombresDeMots");
		wordCount.writeArray(globalTimes,"TempsGlobal");
		
	}
         
}

