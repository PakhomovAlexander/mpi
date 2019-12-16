package task4;

import mpi.*;

import java.util.Random;

import static java.lang.String.format;
import static java.util.stream.IntStream.*;

public class Task4 {

    private final static int MIN_VECTOR_ELEMENT = 0;
    private final static int MAX_VECTOR_ELEMENT = 63;
    private final static Random random = new Random();

    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();

        //minimum 4
        int size = 5;
        int elemsCount = size * 3 + (size - 3) * 2;

        int[] array = new int[size * size];
        int[] blockLengths = range(0, elemsCount).map(x -> 1).toArray();
        int[] blockShifts = new int[elemsCount];

        if (rank == 0) {
            fill(array, size);
            print(array, size);
        }

        constructBlocks(blockShifts, size);

        Datatype customDataType = Datatype.Indexed(blockLengths, blockShifts, MPI.INT);
        customDataType.Commit();

        if (rank == 0) {
            MPI.COMM_WORLD.Send(array, 0, 1, customDataType, 1, 1);
        }

        if (rank == 1) {
            int[] arrayRecv = new int[size * size];
            MPI.COMM_WORLD.Recv(arrayRecv, 0, 1, customDataType, 0, 1);
            System.out.println("Received:");
            print(arrayRecv, size);
        }

        MPI.Finalize();
    }

    private static void constructBlocks(int[] blockShifts, int size) {
        int index = 0;
        int value = 0;

        //first line
        for (int i = 0; i < size; i++) {
            blockShifts[index++] = value++;
        }

        for (int i = 0; i < size - 2; i++) {
            blockShifts[index++] = value++;
            blockShifts[index++] = value;
            value += size - 2;
            blockShifts[index++] = value++;
        }

        //last line
        for (int i = 0; i < size; i++) {
            blockShifts[index++] = value++;
        }
    }

    private static void fill(int[] array, int size) {
        for (int i = 0; i < size * size; i++) {
            array[i] = random.nextInt(MAX_VECTOR_ELEMENT - MIN_VECTOR_ELEMENT) + MIN_VECTOR_ELEMENT;
        }
    }

    private static void print(int[] array, int size) {
        int i = 0;
        for (int item : array) {
            System.out.print(format("%s\t", item));
            if (++i % size == 0) {
                System.out.println();
                i = 0;
            }
        }
    }
}
