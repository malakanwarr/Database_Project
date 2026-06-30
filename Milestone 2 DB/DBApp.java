package DBMS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DBApp
{
	static int dataPageSize = 2;


	public static void createTable(String tableName, String[] columnsNames)
	{
		Table t = new Table(tableName, columnsNames);
		FileManager.storeTable(tableName, t);
	}

	public static void insert(String tableName, String[] record) {
	    Table t = FileManager.loadTable(tableName);
	    if (t == null) return;
	    
	    t.insert(record);
	    FileManager.storeTable(tableName, t);
	    String[] cols = t.getColumnsNames();
	    
	    for (int i = 0; i < cols.length; i++) {
	        BitmapIndex bIndex = FileManager.loadTableIndex(tableName, cols[i]);
	        
	        if (bIndex != null) {
	            String newValue = record[i];

	            for (String key : bIndex.index.keySet()) {
	                String currentBits = bIndex.index.get(key);
	                
	                if (key.equals(newValue)) {
	                    bIndex.index.put(key, currentBits + "1");
	                } else {
	                    bIndex.index.put(key, currentBits + "0");
	                }
	            }

	            if (!bIndex.index.containsKey(newValue)) {
	                int totalRecordsBefore = t.getRecordsCount() - 1;
	                char[] zeros = new char[totalRecordsBefore];
	                java.util.Arrays.fill(zeros, '0');
	                bIndex.index.put(newValue, new String(zeros) + "1");
	            }
	            FileManager.storeTableIndex(tableName, cols[i], bIndex);
	        }
	    }
	}

	public static ArrayList<String []> select(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select();
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static ArrayList<String []> select(String tableName, int pageNumber, int recordNumber)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select(pageNumber, recordNumber);
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static ArrayList<String []> select(String tableName, String[] cols, String[] vals)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select(cols, vals);
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static String getFullTrace(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
		String res = t.getFullTrace();
		return res;
	}

	public static String getLastTrace(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
		String res = t.getLastTrace();
		return res;
	}
	
	public static void createBitMapIndex(String tableName, String colName) {
	    long startTime = System.currentTimeMillis();

	    // 1. lod the table
	    Table t = FileManager.loadTable(tableName);
	    if (t == null) return;

	    BitmapIndex finalIndex = new BitmapIndex();
	    int totalRecords = t.getRecordsCount();
	    
	    // 2. find the index of the column we are indexing
	    String[] columns = t.getColumnsNames();
	    int colindex = -1;
	    for (int i = 0; i < columns.length; i++) {
	        if (columns[i].equals(colName)) {
	            colindex = i;
	            break;
	        }
	    }
	    
	    // if column not found exit 
	    if (colindex == -1) return;

	    // temp map to hold our char[] arrays for efficient bit flipping
	    HashMap<String, char[]> tempMap = new HashMap<>();
	    int rowIndex = 0;

	    // 3. iterate through all pages
	    int pages = t.getPageCount();
	    for (int i = 0; i < pages; i++) {
	        Page p = FileManager.loadTablePage(tableName, i);
	        if (p == null) continue;

	        // iterate thru all records 
	        ArrayList<String[]> records = p.select();
	        for (String[] record : records) {
	            String colValue = record[colindex]; 

	            // initialize the bitstring array if new value
	            if (!tempMap.containsKey(colValue)) {
	                char[] newBitstring = new char[totalRecords];
	                Arrays.fill(newBitstring, '0');
	                tempMap.put(colValue, newBitstring);
	            }

	            // Flip the bit to '1' at the current absolute row index
	            char[] currentBitstring = tempMap.get(colValue);
	            currentBitstring[rowIndex] = '1';

	            rowIndex++;
	        }
	    }

	    // 4. covert array to string 
	    for (String key : tempMap.keySet()) {
	        finalIndex.index.put(key, new String(tempMap.get(key)));
	    }
	    FileManager.storeTableIndex(tableName, colName, finalIndex);

	    // trace
	    long stopTime = System.currentTimeMillis();
	    t.addTrace("Index created for column: " + colName + ", execution time (mil):" + (stopTime - startTime));
	    FileManager.storeTable(tableName, t);
	}
	

	public static String getValueBits(String tableName, String colName, String value) {
	    // 1. load index from disk 
	    BitmapIndex bIndex = FileManager.loadTableIndex(tableName, colName);
	    
	    if (bIndex == null) {
	        System.out.println("No index found for column: " + colName);
	        return "";
	    }
	    
	    // 2. check if value exists 
	    if (bIndex.index.containsKey(value)) {
	        return bIndex.index.get(value);
	    } else {
	       //value doesnt ecist so we load table and return string of zeros
	        Table t = FileManager.loadTable(tableName);
	        if (t != null) {
	            int totalRecords = t.getRecordsCount();
	            char[] zeros = new char[totalRecords];
	            Arrays.fill(zeros, '0');
	            return new String(zeros);
	        }
	        return ""; 
	    }
	}

	public static ArrayList<String[]> selectIndex(String tableName, String[] cols, String[] vals) {
	    long startTime = System.currentTimeMillis();
	    Table t = FileManager.loadTable(tableName);
	    if (t == null) return new ArrayList<>(); 

	    ArrayList<String> indexedCols = new ArrayList<>();
	    ArrayList<String> indexedVals = new ArrayList<>();
	    ArrayList<String> nonIndexedCols = new ArrayList<>();
	    ArrayList<String> nonIndexedVals = new ArrayList<>();

	   
	    // first i seperate the nonindexed and the indexed 
	    for (int i = 0; i < cols.length; i++) {
	        if (FileManager.loadTableIndex(tableName, cols[i]) != null) {
	            indexedCols.add(cols[i]);
	            indexedVals.add(vals[i]);
	        } else {
	            nonIndexedCols.add(cols[i]);
	            nonIndexedVals.add(vals[i]);
	        }
	    }

	    ArrayList<String[]> result = new ArrayList<>();
	    int indexedSelectionCount = 0;

	   //if no indexed columns do normal linear search
	    if (indexedCols.isEmpty()) {
	        result = t.select(cols, vals); 
	    } else {
	        // cases 1, 2, 3: Bitmap Index Search
	         // grab the  bitstring from the first indexed column
	        char[] finalBits = getValueBits(tableName, indexedCols.get(0), indexedVals.get(0)).toCharArray();
	        // AND it with the other bitstrings
	        for (int i = 1; i < indexedCols.size(); i++) {
	            String nextBits = getValueBits(tableName, indexedCols.get(i), indexedVals.get(i));
	            for (int j = 0; j < finalBits.length; j++) {
	                if (finalBits[j] == '1' && nextBits.charAt(j) == '1') {
	                    finalBits[j] = '1';
	                } else {
	                    finalBits[j] = '0';
	                }
	            }
	        }

	        Page currentPage = null;
	        int currentPageNum = -1;
	        String[] colNames = t.getColumnsNames();

	        // Retrieve actual records using high-performance physical location mapping
	        for (int j = 0; j < finalBits.length; j++) {
	            if (finalBits[j] == '1') {
	                indexedSelectionCount++;
	                
	                int pageNum = j / DBApp.dataPageSize;
	                int rowNum = j % DBApp.dataPageSize;

	                // load page only if we aren't already on it(optimization)
	                if (pageNum != currentPageNum) {
	                    currentPage = FileManager.loadTablePage(tableName, pageNum);
	                    currentPageNum = pageNum;
	                }

	                // extract record using MS1 select method
	                String[] record = currentPage.select(rowNum).get(0);

	                boolean matchesNonIndexed = true;
	                for (int k = 0; k < nonIndexedCols.size(); k++) {
	                    String targetCol = nonIndexedCols.get(k);
	                    String targetVal = nonIndexedVals.get(k);

	                    int colIndex = -1;
	                    for (int c = 0; c < colNames.length; c++) {
	                        if (colNames[c].equals(targetCol)) {
	                            colIndex = c;
	                            break;
	                        }
	                    }

	                    if (colIndex != -1 && !record[colIndex].equals(targetVal)) {
	                        matchesNonIndexed = false;
	                        break;
	                    }
	                }

	                if (matchesNonIndexed) {
	                    result.add(record);
	                }
	            }
	        }
	    }

	    //trace
	    long stopTime = System.currentTimeMillis();

	    // sort arrays alphabetically 
	    java.util.Collections.sort(indexedCols);
	    java.util.Collections.sort(nonIndexedCols);

	    String trace = "Select index condition:" + java.util.Arrays.toString(cols) + "->" + java.util.Arrays.toString(vals);
	    
	    if (!indexedCols.isEmpty()) {
	        trace += ", Indexed columns: " + indexedCols.toString() + ", Indexed selection count: " + indexedSelectionCount;
	    }
	    
	    if (!nonIndexedCols.isEmpty()) {
	        trace += ", Non Indexed: " + nonIndexedCols.toString();
	    }

	    trace += ", Final count: " + result.size() + ", execution time (mil):" + (stopTime - startTime);

	    t.addTrace(trace);
	    FileManager.storeTable(tableName, t);

	    return result;
	}
	

	public static ArrayList<String[]> validateRecords(String tableName) {
	    Table t = FileManager.loadTable(tableName);
	    ArrayList<String[]> missingRecords = new ArrayList<>();
	    if (t == null) return missingRecords;

	    // 1. find missing pages 
	    ArrayList<Integer> missingPages = new ArrayList<>();
	    for (int i = 0; i < t.getPageCount(); i++) {
	        if (FileManager.loadTablePage(tableName, i) == null) {
	            missingPages.add(i);
	        }
	    }

	    // 2. extract records 
	    if (!missingPages.isEmpty()) {
	        for (String log : t.getTrace()) {
	            if (log.startsWith("Inserted:")) {
	                int pageNum = Integer.parseInt(log.split("page number:")[1].split(",")[0].trim());

	                if (missingPages.contains(pageNum)) {
	                    String arrayContent = log.substring(log.indexOf("[") + 1, log.indexOf("]"));
	                    missingRecords.add(arrayContent.split(",\\s*"));
	                }
	            }
	        }
	    }
	    
	    t.addTrace("Validating records: " + missingRecords.size() + " records missing.");
	    FileManager.storeTable(tableName, t); 
	    return missingRecords;
	}

	public static void recoverRecords(String tableName, ArrayList<String[]> missing) {
	    Table t = FileManager.loadTable(tableName);
	    if (t == null) return;
	    java.util.HashMap<Integer, Page> rebuiltPages = new java.util.HashMap<>();

	    // rebuild missing pages 
	    for (String[] record : missing) {
	        String recordStr = Arrays.toString(record);

	        for (String log : t.getTrace()) {
	            if (log.startsWith("Inserted:") && log.contains(recordStr)) {
	                int originalPageNum = Integer.parseInt(log.split("page number:")[1].split(",")[0].trim());
	                
	                rebuiltPages.putIfAbsent(originalPageNum, new Page());
	                rebuiltPages.get(originalPageNum).insert(record);
	                break;
	            }
	        }
	    }
	    for (Integer pageNum : rebuiltPages.keySet()) {
	        FileManager.storeTablePage(tableName, pageNum, rebuiltPages.get(pageNum));
	    }

	    ArrayList<Integer> recoveredNums = new ArrayList<>(rebuiltPages.keySet());
	    java.util.Collections.sort(recoveredNums);
	
	    t.addTrace("Recovering " + missing.size() + " records in pages: " + recoveredNums + ".");
	    FileManager.storeTable(tableName, t);
	}
	
	
	
	public static void main(String []args) throws IOException
    {
       
    }

}
