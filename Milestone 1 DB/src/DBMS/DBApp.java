package DBMS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

public class DBApp
{
	static int dataPageSize = 2;
	
	public static void createTable(String tableName, String[] columnsNames) {
	    Table t = new Table();
	    t.name = tableName;
	    t.columns = columnsNames;
	    t.pageCount = 0;
	    t.trace.add("Table created name:" + tableName + ", columnsNames:" + Arrays.toString(columnsNames));
	    FileManager.storeTable(tableName, t);
	}
	
	public static void insert(String tableName, String[] record) {
	    Table t = FileManager.loadTable(tableName);
	    
	    Page p;
	    int pIndex;
	    
	    if(t.pageCount == 0) {
	        p = new Page();
	        pIndex = 0;
	    } else {
	        pIndex = t.pageCount - 1;
	        p = FileManager.loadTablePage(tableName, pIndex);
	        
	        if(p.records.size() >= dataPageSize) {
	            p = new Page();
	            pIndex = t.pageCount;
	        }
	    }
	    p.records.add(record);
	    if(pIndex == t.pageCount) {
	        t.pageCount++;
	    }
	    t.trace.add("Inserted:" + Arrays.toString(record) + 
	                ", at page number:" + pIndex + 
	                ", execution time (mil):" + System.currentTimeMillis() % 1000);
	    
	    FileManager.storeTablePage(tableName, pIndex, p);
	    FileManager.storeTable(tableName, t);
	}
	
	
	//selects entire table 
	public static ArrayList<String[]> select(String tableName) {
	    long start = System.currentTimeMillis();
	    ArrayList<String[]> result = new ArrayList<>();
	    Table t = FileManager.loadTable(tableName);
	    for(int i = 0; i < t.pageCount; i++) {
	        Page p = FileManager.loadTablePage(tableName, i);
	        for(String[] record : p.records)
	            result.add(record);
	    }
	    t.trace.add("Select all pages:" + t.pageCount + 
	    		", records:" + result.size() + ", execution time (mil):" + (System.currentTimeMillis() - start));
	    FileManager.storeTable(tableName, t);
	    return result;
	}
	
	//select with WHERE condition 
	public static ArrayList<String[]> select(String tableName, String[] cols, String[] vals) {
	    long start = System.currentTimeMillis();
	    ArrayList<String[]> result = new ArrayList<>();
	    Table t = FileManager.loadTable(tableName);
	    int[] pageFrequency = new int[t.pageCount];
	    for(int i = 0; i < t.pageCount; i++) {
	        Page p = FileManager.loadTablePage(tableName, i);
	        for(String[] record : p.records) {
	            boolean match = true;
	            for(int j = 0; j < cols.length; j++) {
	                int colIndex = Arrays.asList(t.columns).indexOf(cols[j]);
	                if(!record[colIndex].equals(vals[j])) { 
	                	match = false; 
	                	break; }
	            }
	            if(match) { 
	            	result.add(record); 
	            	pageFrequency[i]++; 
	            	}
	        }
	    }
	    ArrayList<String> pagesCounts = new ArrayList<>();
	    for(int i = 0; i < pageFrequency.length; i++)
	        if(pageFrequency[i] != 0) pagesCounts.add("[" + i + ", " + pageFrequency[i] + "]");
	    t.trace.add("Select condition:" + Arrays.toString(cols) + "->" + Arrays.toString(vals) + ", Records per page:" + pagesCounts.toString() + ", records:" + result.size() + ", execution time (mil):" + (System.currentTimeMillis() - start));
	    FileManager.storeTable(tableName, t);
	    return result;
	}
	
	//select with record number 
	public static ArrayList<String[]> select(String tableName, int pageNumber, int recordNumber) {
	    long start = System.currentTimeMillis();
	    ArrayList<String[]> result = new ArrayList<>();
	    Table t = FileManager.loadTable(tableName);
	    Page p = FileManager.loadTablePage(tableName, pageNumber);
	    // i do this safety check because if recordNumber is out of bounds, i return empty just like in normal sql database
	    if(recordNumber >= p.records.size() || recordNumber < 0) {
	        t.trace.add("Select pointer page:" + pageNumber + ", record:" + recordNumber + 
	                    ", total output count:0, execution time (mil):" + 
	                    (System.currentTimeMillis() - start));
	        FileManager.storeTable(tableName, t);
	        return result;
	    }
	    result.add(p.records.get(recordNumber));
	    t.trace.add("Select pointer page:" + pageNumber + ", record:" + recordNumber + 
	                ", total output count:1, execution time (mil):" + 
	                (System.currentTimeMillis() - start));
	    FileManager.storeTable(tableName, t);
	    return result;
	}
	
	
	public static String getFullTrace(String tableName) {
	    Table t = FileManager.loadTable(tableName);
	    int totalRecords = 0;
	    for(int i = 0; i < t.pageCount; i++)
	        totalRecords += FileManager.loadTablePage(tableName, i).records.size();
	    return String.join("\n", t.trace) + "\nPages Count: " + t.pageCount + ", Records Count: " + totalRecords;
	}
	
	public static String getLastTrace(String tableName) {
	    Table t = FileManager.loadTable(tableName);
	    return t.trace.get(t.trace.size() - 1);
	}
	
	
	public static void main(String []args) throws IOException
	{
	}
}
	
	

