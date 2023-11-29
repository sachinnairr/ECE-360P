//UT-EID= svn343, amn2867

import java.util.*;
import java.util.concurrent.*;
class ForkSorting extends RecursiveAction{
    public int[] Arr;
    public int start;
    public int end;
    public boolean increasing;
   //copy over array atrribtes to a forksorting class 
    public ForkSorting(int[]A, int start, int end, boolean increasing){
        this.Arr = A;
        this.start = start;
        this.end = end;
        this.increasing = increasing;

    }

    //simple sort depending on increasing or not
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
    //when a thread runs it will sort its part of the array 
    protected void compute() {
        sort(Arr, increasing);
 
    }

}
public class ForkJoinPSort {
    /* Notes:
     * The input array (A) is also the output array,
     * The range to be sorted extends from index begin, inclusive, to index end, exclusive,
     * Sort in increasing order when increasing=true, and decreasing order when increasing=false,
     */
    public static void parallelSort(int[] A, int begin, int end, boolean increasing) {
        // TODO: Implement your parallel sort function using Runnables
        if (A.length < 16){
        //System.out.println("INIT ARRAY: " + Arrays.toString(A));
        A = split(A,begin, end, increasing);
        }
        //else recursivley call
        else{

            parallelSort(A, begin, end, increasing);
        }

        
        //System.out.println("THIS IS MY FINAL ARRAY " + Arrays.toString(A));
    }
    //spit arrays into sub arrays recursivley until n < 16
    public static int[] split(int[] A, int begin, int end, boolean increasing){
        int len = end - begin;
        int mid = len/2;
           
            
            if(len > 16){
                int[] left = Arrays.copyOfRange(A, begin, mid);
                int[] right = Arrays.copyOfRange(A, mid, len);



                int llen = left.length;
                int rlen = right.length;
                ForkSorting sortLeft = new ForkSorting(left, 0, llen, increasing);
                ForkSorting sortRight = new ForkSorting(right, 0, rlen, increasing);
                
                ForkJoinPool pool = ForkJoinPool.commonPool();
                pool.invoke(sortLeft);
                pool.invoke(sortRight);

                
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
    //only used for sorting small arrays
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
