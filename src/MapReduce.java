import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/* 
 * 
 * CT414 - Distributed Systems & Co Operative Computing
 * 4BCT 
 * Caroline Richardson
 * Nicole Ferry 
 * 
*/


public class MapReduce 
{
        
	
	public static void main(String[] args) throws IOException, Exception  
	{
	        
        // the problem:
        
        // from here (INPUT)
        
        // "file1.txt" => "foo foo bar cat dog dog"
        // "file2.txt" => "foo house cat cat dog"
        // "file3.txt" => "foo foo foo bird"

        // we want to go to here (OUTPUT)
        
        // "foo" => { "file1.txt" => 2, "file3.txt" => 3, "file2.txt" => 1 }
        // "bar" => { "file1.txt" => 1 }
        // "cat" => { "file2.txt" => 2, "file1.txt" => 1 }
        // "dog" => { "file2.txt" => 1, "file1.txt" => 2 }
        // "house" => { "file2.txt" => 1 }
        // "bird" => { "file3.txt" => 1 }
        
        // in plain English we want to
        
        // Given a set of files with contents
        // we want to index them by word 
        // so I can return all files that contain a given word
        // together with the number of occurrences of that word
        // without any sorting
        
        ////////////
        // INPUT:
        ///////////
        
	
        //Map<String, String> input = new HashMap<String, String>();
        
        //input.put("file1.txt", "foo foo bar cat dog dog");
        //input.put("file2.txt", "foo house cat cat dog");
        //input.put("file3.txt", "foo foo foo bird");
	        
		
 /*-------------------------------------------------------------------------------------*/
		
		
		
		/* TO DO */
		//PART 1 - read in files and store the content of each in a map - DONE
		//PART 2 - count based on first letter of words as opposed to the words - DONE
		//PART 3 - thread pool
		
		
		Map<String, String> input = new HashMap<String, String>();
		
	    @SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
	    
        String f1 = scan.next();
        String f1contents = readFile(f1);
        
        String f2 = scan.next();
        String f2contents = readFile(f2);
        
        String f3 = scan.next();
        String f3contents = readFile(f3);
        
        
        input.put(f1, f1contents);
        input.put(f2, f2contents);
        input.put(f3, f3contents);
        

        
        
        //----- NO NEED FOR APPROACH 1 & 2 FROM ASSIGNMENT OUTLINE -------//
        
        /*
	    // APPROACH #1: Brute force
        {
        	
            Map<String, Map<String, Integer>> output = new HashMap<String, Map<String, Integer>>();
            
            Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
            while(inputIter.hasNext()) 
            {
            	
                   
            	Map.Entry<String, String> entry = inputIter.next();
                String file = entry.getKey();
                String contents = entry.getValue();
                    
                String[] words = contents.trim().split("\\s+");
                    
                for(String word : words) 
                {
                        
                    Map<String, Integer> files = output.get(word);
                    if (files == null) 
                    {
                            
                    	files = new HashMap<String, Integer>();
                        output.put(word, files);
                    }
                    
                    Integer occurrences = files.remove(file);
                    if (occurrences == null) 
                    { 
                    	files.put(file, 1);
                    } 
                    
                    else 
                    {
                        files.put(file, occurrences.intValue() + 1);
                    }
                }
            }
            
            // show me:
            System.out.println(output);
            
        } */
        
        
        /*
        // APPROACH #2: MapReduce
        {
                
        	Map<String, Map<String, Integer>> output = new HashMap<String, Map<String, Integer>>();
                
                
        	// MAP: 
                
        	List<MappedItem> mappedItems = new LinkedList<MappedItem>();
                
            Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
            while(inputIter.hasNext()) 
            {
                    
            	Map.Entry<String, String> entry = inputIter.next();
                String file = entry.getKey();
                String contents = entry.getValue();
                    
                map(file, contents, mappedItems);
            }
            
            
            // GROUP:
            
            Map<String, List<String>> groupedItems = new HashMap<String, List<String>>();
            
            Iterator<MappedItem> mappedIter = mappedItems.iterator();
            
            while(mappedIter.hasNext()) 
            {
            	
                MappedItem item = mappedIter.next();
                String word = item.getWord();
                String file = item.getFile();
                
                List<String> list = groupedItems.get(word);
                if (list == null) 
                {
                        list = new LinkedList<String>();
                        groupedItems.put(word, list);
                }
                
                list.add(file);
            }
            
            // REDUCE:
            
            Iterator<Map.Entry<String, List<String>>> groupedIter = groupedItems.entrySet().iterator();
            while(groupedIter.hasNext()) 
            {
            	
                Map.Entry<String, List<String>> entry = groupedIter.next();
                String word = entry.getKey();
                List<String> list = entry.getValue();
                
                reduce(word, list, output);
            }
            
            System.out.println(output);
        }*/
        
        
        
        
        
        // Assignment says use Part 3 so other parts commented out
      
        // APPROACH #3: Distributed MapReduce
        {
                
        	final Map<String, Map<String, Integer>> output = new HashMap<String, Map<String, Integer>>();
                
            // MAP:
            
            final List<MappedItem> mappedItems = new LinkedList<MappedItem>();
            
            final MapCallback<String, MappedItem> mapCallback = new MapCallback<String, MappedItem>() 
            {
                   
            	@Override
            	public synchronized void mapDone(String file, List<MappedItem> results) 
                {
                	mappedItems.addAll(results);
                }
            	
            };
            
            List<Thread> mapCluster = new ArrayList<Thread>(input.size());
            
            Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
            while(inputIter.hasNext()) 
            {
                    
            	Map.Entry<String, String> entry = inputIter.next();
            	
                final String file = entry.getKey();
                final String contents = entry.getValue();
                
                Thread t = new Thread(new Runnable() 
                {
                	
                    @Override
                    public void run() 
                    {
                    	map(file, contents, mapCallback);
                    }
                    
                });
                
                mapCluster.add(t);
                t.start();
            }
            
            
            // wait for mapping phase to be over:
            for(Thread t : mapCluster) 
            {
            	
                try 
                {  
                	t.join();
                } 
                
                catch(InterruptedException e) 
                { 
                	throw new RuntimeException(e);
                }
            }
            
            
            // GROUP:
            
            Map<String, List<String>> groupedItems = new HashMap<String, List<String>>();
            
            Iterator<MappedItem> mappedIter = mappedItems.iterator();
            
            while(mappedIter.hasNext()) 
            {
            	
                MappedItem item = mappedIter.next();
                
                String word = item.getWord();
                String file = item.getFile();
                String letter = item.getFirstLetter();
                
                //List<String> list = groupedItems.get(word);
                List<String> list = groupedItems.get(letter);
                
                if (list == null) 
                {
                        
                	list = new LinkedList<String>();
                    //groupedItems.put(word, list);
                    groupedItems.put(letter, list);
                }
                
                list.add(file);
                
            }
            
            
            
            // REDUCE:
            
            final ReduceCallback<String, String, Integer> reduceCallback = new ReduceCallback<String, String, Integer>() 
            {
            	
                @Override
                public synchronized void reduceDone(String k, Map<String, Integer> v) 
                {
                	output.put(k, v);
                }
                
            };
            
            List<Thread> reduceCluster = new ArrayList<Thread>(groupedItems.size());
            
            Iterator<Map.Entry<String, List<String>>> groupedIter = groupedItems.entrySet().iterator();
            while(groupedIter.hasNext()) 
            {
                    
            	Map.Entry<String, List<String>> entry = groupedIter.next();
                final String word = entry.getKey();
                final List<String> list = entry.getValue();
                    
                    
                Thread t = new Thread(new Runnable() 
                {
                    	
                    @Override
                    public void run() 
                    {
                    	reduce(word, list, reduceCallback);
                    }
                    
                });
                
                reduceCluster.add(t);
                t.start();
            }
            
            // wait for reducing phase to be over:
            for(Thread t : reduceCluster) 
            {
            	
                try 
                {
                	t.join();
                } 
                catch(InterruptedException e) 
                {
                    throw new RuntimeException(e);
                }
            }
            
            System.out.println(output);
        }
        
	}
	
	
	//------------------------------------------------------------------------------//
	
	
	/*public static void map(String file, String contents, List<MappedItem> mappedItems) 
	{
        String[] words = contents.trim().split("\\s+");
        
        char firstLetter;
        
        for(String word: words) 
        {
        	
        	if (Character.isLetter(word.charAt(0)))
        	{
        		mappedItems.add(new MappedItem(word, file));
        	}
        	
        }
        	
	}*/
	
	
	
	//------------------------------------------------------------------------------//
	
	
	public static void reduce(String word, List<String> list, Map<String, Map<String, Integer>> output) 
	{
		
        Map<String, Integer> reducedList = new HashMap<String, Integer>();
        
        for(String file: list) 
        {
            
        	Integer occurrences = reducedList.get(file);
            
            if (occurrences == null) 
            {
            	reducedList.put(file, 1);
            } 
            
            else 
            {
            	reducedList.put(file, occurrences.intValue() + 1);
            }
        }
        
        output.put(word, reducedList);
	}
	
	
	//------------------------------------------------------------------------------//
	
	
	public static interface MapCallback<E, V> 
	{
		public void mapDone(E key, List<V> values);
	}
	
	
	//------------------------------------------------------------------------------//
	
	
	public static void map(String file, String contents, MapCallback<String, MappedItem> callback) 
	{
		
        String[] words = contents.trim().split("\\s+");
        List<MappedItem> results = new ArrayList<MappedItem>(words.length);
        
        //hold first letter
        String letter;
        
        
        //loop through words
        for(String word: words) 
        {
        	//get letter at first point
        	letter = Character.toString(word.charAt(0));
        	results.add(new MappedItem(word, file, letter));
        }
        
        callback.mapDone(file, results);
	}
	
	
	
	//------------------------------------------------------------------------------//
	
	
	public static interface ReduceCallback<E, K, V> 
	{
		public void reduceDone(E e, Map<K,V> results);
	}
	
	
	//------------------------------------------------------------------------------//
	
	
	public static void reduce(String word, List<String> list, ReduceCallback<String, String, Integer> callback) 
	{
	        
        Map<String, Integer> reducedList = new HashMap<String, Integer>();
        
        for(String file: list) 
        {
        	
            Integer occurrences = reducedList.get(file);
            
            if (occurrences == null) 
            {
                    
            	reducedList.put(file, 1);
            } 
            
            else 
            {   
            	reducedList.put(file, occurrences.intValue() + 1);
            }
            
        }
        
        callback.reduceDone(word, reducedList);
	}
	
	
	//------------------------------------------------------------------------------//
	
	
	//added method for reading from file input
	/*private static String readFile(String pathname) throws IOException 
	{

	    File file = new File(pathname);
	    StringBuilder fileContents = new StringBuilder((int)file.length());
	    Scanner scanner = new Scanner(file);
	    
	    String lineSeparator = System.getProperty("line.separator");

	    try 
	    {
	        while(scanner.hasNextLine()) 
	        {
	            fileContents.append(scanner.nextLine() + lineSeparator);
	       
	        }
	        
	        return fileContents.toString();
	    } 
	    
	    finally 
	    {
	        scanner.close();
	    }
	  
	}*/
	
	
	//added method for reading from file input
    private static String readFile(String pathname) throws IOException 
    {

        File file = new File(pathname);
        Scanner scanner = new Scanner(file);
        
        String fileContents = null;
        String[] words;
        
        String lineSeparator = System.getProperty("line.separator");

        try 
        {
            while(scanner.hasNextLine()) 
            {
            	
            	words = scanner.nextLine().split("\\s+");
            	
            	//loop through the array of words read in, remove non a-z characters
            	for(int i=0; i<words.length; i++)
            	{
                    words[i] = words[i].replaceAll("[^a-zA-Z]", "").toLowerCase();
                    fileContents += words[i] + " "; 
            	}
            	
            }

            return fileContents.toString();
        } 

        finally 
        {
            scanner.close();
        }
 

    }
	
	
	//------------------------------------------------------------------------------//
	
	
	private static class MappedItem 
	{ 
	        
        private final String word;
        private final String file;
        private final String firstLetter;
        
        public MappedItem(String word, String file, String letter) 
        {
                
        	this.word = word;
            this.file = file;
            this.firstLetter = letter;
        }

        public String getWord() 
        {   
        	return word;
        }

        public String getFile() 
        {    
        	return file;
        }
        
        public String getFirstLetter()
        {
        	return firstLetter;
        }
        
        
        @Override
        public String toString() 
        {
        	
        	return "[\"" + firstLetter + "\",\"" + file + "\"]";
            //return "[\"" + word + "\",\"" + file + "\"]";
        }
	        
	}
	
}