package task3;

import mpi.*;

import java.util.Arrays;
import java.util.Random;

// 18. Найти сумму абсолютных значений
public class Task3 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();

        int n = 3;
        int[] resBuff = new int[n];
        int[] localBuff = generateArray(n);
        printArray(localBuff, String.format("[%d] Process", rank));

        User_function fun = new SumFunction();
        MPI.COMM_WORLD.Reduce(localBuff, 0, resBuff, 0, n, MPI.INT, new Op(fun, false), 0);
        if (rank == 0) {
            printArray(resBuff, String.format("[%d] Result", rank));
        }

        MPI.Finalize();
    }

    private static int[] generateArray(int n) {
        Random rand = new Random();
        int[] localBuff = new int[n];

        for (int i = 0; i < localBuff.length; i++) {
            localBuff[i] = rand.nextInt(100) - 50;
        }

        return localBuff;
    }

    private static void printArray(int[] arr, String msg) {
        String s = Arrays.toString(arr);
        System.out.println(msg + ": " + s);
    }
}

