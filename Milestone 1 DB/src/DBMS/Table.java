package DBMS;
import java.io.Serializable;
import java.util.ArrayList;

public class Table implements Serializable {
    String name;
    String[] columns;
    int pageCount;
    ArrayList<String> trace = new ArrayList<>();
}