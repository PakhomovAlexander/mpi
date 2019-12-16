package task5;


import mpi.Cartcomm;
import mpi.MPI;
import mpi.ShiftParms;

import java.util.Arrays;
import java.util.Random;

import static java.lang.String.format;

//18
public class Task5 {
    static final Random random = new Random();

    static final int NUM_DIMS = 1;
    static final int NUMBER = 5;

    static final int[] dims = new int[NUM_DIMS];
    static final boolean[] periods = initBoolArray();


    //max(j)max(k)(a(ik)*b(jk))
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Create topology
        Cartcomm.Dims_create(size, dims);
        Cartcomm cartComm = MPI.COMM_WORLD.Create_cart(dims, periods, false);
        ShiftParms sourceDest = cartComm.Shift(0, -1);

        int[] a = initRandArray(size);
        printRow(format("[%s] A: ", rank), a);
        int[] b = initRandArray(size);
        printRow(format("[%s] B: ", rank), b);

        int[] recvA = new int[size * size];
        int[] recvB = new int[size * size];
        cartComm.Gather(a, 0, size, MPI.INT, recvA, 0, size, MPI.INT, 0);
        cartComm.Gather(b, 0, size, MPI.INT, recvB, 0, size, MPI.INT, 0);
        if (rank == 0) {
            printMattrix("Matrix A: ", recvA, size);
            printMattrix("Matrix B: ", recvB, size);
        }

        int globalMax = getMax(size, a, b);
        cartComm.Sendrecv_replace(b, 0, size, MPI.INT, sourceDest.rank_dest, 0, sourceDest.rank_source, 0);

//        int globalMax = 0;
        int index = rank;
        for (int i = 0; i < size; i++) {
            if (++index == size)
                index = 0;
            int max = getMax(size, a, b);
            if (globalMax < max) globalMax = max;
            cartComm.Sendrecv_replace(b, 0, size, MPI.INT, sourceDest.rank_dest, 0, sourceDest.rank_source, 0);
        }

        int[] maxBuf = {globalMax};

        cartComm.Gather(maxBuf, 0, 1, MPI.INT, recvA, 0, 1, MPI.INT, 0);
        if (rank == 0) {
            printColumn("Result: \n", size, recvA);
        }

        MPI.Finalize();
    }

    private static int getMax(int size, int[] a, int[] b) {
        int max = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int mult = a[i] * b[j];
                if (max < mult) max = mult;
            }
        }
        return max;
    }

    private static void printColumn(String prefix, int size, int[] col) {
        System.out.println(prefix);
        for (int i = 0; i < size; i++) {
            System.out.printf("%4d\n", col[i]);
        }
    }

    private static void printRow(String prefix, int[] row) {
        System.out.println(format("%s\n %s", prefix, Arrays.toString(row)));
    }


    static void printMattrix(String prefix, int[] matrix, int length) {
        System.out.println(prefix);
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                System.out.format("%4d", matrix[length * j + i]);
            }
            System.out.println();
        }
    }

    private static boolean[] initBoolArray() {
        boolean[] arr = new boolean[Task5.NUMBER];
        for (int i = 0; i < Task5.NUMBER; i++) {
            arr[i] = true;
        }

        return arr;
    }

    private static int[] initRandArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(NUMBER);
        }

        return arr;
    }
}
