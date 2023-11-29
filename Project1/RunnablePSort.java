//UT-EID= svn343, amn2867

import java.util.*;
import java.util.concurrent.*;

class Sorting implements Runnable{
    public int[] Arr;
    public int start;
    public int end;
    public boolean increasing;
    //copy a and atributes to a runnable class
    public Sorting(int[]A, int start, int end, boolean increasing){
        this.Arr = A;
        this.start = start;
        this.end = end;
        this.increasing = increasing;

    }
    //when each thread runs it sorts its part of the array
    public void run() {
        sort(Arr, increasing);
        
    }
    //simple sort
    public static int[] sort(int[]A, boolean increasing){
        int n = A.length;
        //System.out.println("THIS IS BEING DONE IN A THREAD");
        if(increasing){
            for (int i = 1; i < n; ++i) {
                int key = A[i];
                int j = i - 1;
                while (j >= 0 && A[j] > key) {
                    A[j + 1] = A[j];
                    j = j - 1;
                }
                A[j + 1] = key;
            }
        }
        else{
            for (int i = 1; i < n; ++i) {
                int key = A[i];
                int j = i - 1;
                while (j >= 0 && A[j] < key) {
                    A[j + 1] = A[j];
                    j = j - 1;
                }
                A[j + 1] = key;
            }
        }
        return A;
    }

}
public class RunnablePSort {
    /* Notes:
     * The input array (A) is also the output array,
     * The range to be sorted extends from index begin, inclusive, to index end, exclusive,
     * Sort in increasing order when increasing=true, and decreasing order when increasing=false,
     */
    public static void parallelSort(int[] A, int begin, int end, boolean increasing) {
        // TODO: Implement your parallel sort function using ForkJoinPool
        if (A.length < 16){
            //System.out.println("INIT ARRAY: " + Arrays.toString(A));
            A = split(A,begin, end, increasing);
            }
            //else recursivley call
            else{
    
                parallelSort(A, begin, end, increasing);
            }


    }
    //split array until n < 16
    public static int[] split(int[] A, int begin, int end, boolean increasing){
        int len = end - begin;
        int mid = len/2;
           
            
            if(len > 16){
                int[] left = Arrays.copyOfRange(A, begin, mid);
                int[] right = Arrays.copyOfRange(A, mid, len);


                //have one thread do left sort and have another do right sort
                int llen = left.length;
                int rlen = right.length;
                Sorting sortLeft = new Sorting(left, 0, llen, increasing);
                Sorting sortRight = new Sorting(right, 0, rlen, increasing);
                Thread t1 = new Thread(sortLeft);
                Thread t2 = new Thread(sortRight);

                t1.start();
                t2.start();

                try{
                    t1.join();
                    t2.join();
                }
                catch (InterruptedException e){};
                
                return join(left, right, increasing);
            }
            else{
                return sort(A, increasing);
            }
        }
            
        
    
    //join 2 sorted arrays
    public static int[] join(int[] A, int[] B, Boolean increasing){
        int[] join = new int[(A.length + B.length)];
        System.arraycopy(A, 0, join, 0, A.length);
        System.arraycopy(B, 0, join, A.length,B.length);
        join = sort(join,increasing);
        return join;
    }
    //sort only used when array is small enough
    public static int[] sort(int[]A, boolean increasing){
        int n = A.length;
        if(increasing){
            for (int i = 1; i < n; ++i) {
                int key = A[i];
                int j = i - 1;
                while (j >= 0 && A[j] > key) {
                    A[j + 1] = A[j];
                    j = j - 1;
                }
                A[j + 1] = key;
            }
        }
        else{
            for (int i = 1; i < n; ++i) {
                int key = A[i];
                int j = i - 1;
                while (j >= 0 && A[j] < key) {
                    A[j + 1] = A[j];
                    j = j - 1;
                }
                A[j + 1] = key;
            }
        }
        return A;
    }
}