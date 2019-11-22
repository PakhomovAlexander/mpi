package task1;

import mpi.MPI;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.String.format;

/**
 * 18. Каждый процесс обменивается сообщениями со всеми остальными и выводит номера тех,
 * процессов, у которых есть хотя бы одно значение, совпадающее с его совственными значениями.
 */
public class Task1 {
    private final static int TAG = 1;
    private final static int SIZE = 3;
    private final static int MAX = 10;

    private final static Random rnd = new Random();

    public static void main(String[] args) {
        MPI.Init(args);

        int processCount = MPI.COMM_WORLD.Size();
        int rank = rank();

        int[] vector = generateArray(SIZE);
        print(Arrays.toString(vector), rank);

        send(processCount, rank, vector);

        receive(processCount, rank, vector);

        MPI.Finalize();
    }

    private static void receive(int processCount, int rank, int[] vector) {
        for (int i = 0; i < processCount; i++) {
            if (rank != i) {
                int[] buffer = new int[SIZE];

                MPI.COMM_WORLD.Recv(buffer, 0, SIZE, MPI.INT, i, TAG);

                if (intersect(vector, buffer)) {
                    print(format("common values with [%s]", i), rank);
                }
            }
        }
    }

    private static void send(int processCount, int rank, int[] vector) {
        for (int i = 0; i < processCount; i++) {
            if (rank != i) {
                MPI.COMM_WORLD.Isend(vector, 0, SIZE, MPI.INT, i, TAG);
            }
        }
    }

    private static boolean intersect(int[] arr1, int[] arr2) {
        for (int elem : arr1) {
            if (contains(arr2, elem))
                return true;
        }

        return false;
    }

    private static boolean contains(int[] vector, int elem) {
        for (int v : vector) {
            if (v == elem) {
                return true;
            }
        }

        return false;
    }

    private static void print(String msg, int rank) {
        System.out.println(wrapMsg(msg, rank));
    }

    private static int rank() {
        return MPI.COMM_WORLD.Rank();
    }

    private static int[] generateArray(int size) {
        return IntStream.range(0, size)
                        .map(i -> rnd.nextInt(MAX))
                        .toArray();
    }

    private static String wrapMsg(String msg, int rank) {
        return format("Process [%s]: %s", rank, msg);
    }

}
