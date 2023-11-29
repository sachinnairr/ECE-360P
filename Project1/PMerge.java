//UT-EID=amn2867, svn343

import java.util.*;
import java.util.concurrent.*;

public class PMerge {
	/* Notes:
     * Arrays A and B are sorted in the ascending order
     * These arrays may have different sizes.
     * Array C is the merged array sorted in the descending order
     */
    int[] A, B, C;
    int numThreads;

    PMerge(int[] A, int[] B, int[] C, int numThreads) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.numThreads = numThreads;
    }

    public static void parallelMerge(int[] A, int[] B, int[] C, int numThreads) {
    	// TODO: Implement your parallel merge function
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future> futures = new ArrayList<>();

        for (int i = 0; i < A.length; i++) {
            futures.add(executor.submit(new Merging(A, B, C, i, false)));
        }
        for (int i = 0; i < B.length; i++) {
            futures.add(executor.submit(new Merging(B, A, C, i, true)));
        }

        for (Future future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }

    static class Merging implements Runnable {
        private int[] A, B, C;
        private int index;
        private boolean dupes;

        Merging(int[] A, int[] B, int[] C, int index, boolean dupes) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.index = index;
            this.dupes = dupes;
        }

        @Override
        public void run() {
            int cIndex = Arrays.binarySearch(B, A[index]);
            if (cIndex < 0) {
            	cIndex = C.length-index+cIndex;
            } else {
                if (dupes) {
                	cIndex = C.length-index-cIndex-2;
                }else {
                	cIndex = C.length-index-cIndex-1;
                }
            }
            C[cIndex] = A[index];
        }
    }
}