public class Book {
    String name;
    int quantity;
    
    Book(String n, int q) {
        name = n;
        quantity = q;
    }
    public String getName(){
        return name;
    }
    public int getQuantity(){
        return quantity;
    }
    
    public void setName(String nn){
        name = nn;
    }
    public void setQuant(int nq){
        quantity = nq;
    }
    
    public String toString(){
        return "Name: " + name + " Quantity: " + quantity;
    }
}

//class to manage loan records or book name, student name, loan id, and book id
class Record {
    String bookName;
    String studentName;
    Integer loanID;
    Integer bookID;

    Record(String name, String student, Integer id, Integer bID) {
        bookName = name;
        studentName = student;
        loanID = id;
        bookID = bID;
    }
    public String getBookName(){
        return bookName;
    }
    public String getStudentName(){
        return studentName;
    }
    public Integer getLoadID(){
        return loanID;
    }
    public Integer getBookID(){
        return bookID;
    }
    public void setBookName(String bn){
        bookName = bn;
    }
    public void setStudentName(String sn){
        studentName = sn;
    }
    public void setloanID(Integer nLID){
        loanID = nLID;
    }
    public void setBookID(Integer nBID){
        bookID = nBID;
    }
    public String toString(){
        return "Bookname: " + bookName + " Student name: " + studentName + " loanID: " + loanID + " bookID: " + bookID;
    }
}
