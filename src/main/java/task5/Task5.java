package task5;


import mpi.Cartcomm;
import mpi.MPI;
import mpi.ShiftParms;

//9
public class Task5 {
    static final int NUM_DIMS = 1;
    static final int[] dims;
    static final boolean[] periods;
    static final int NUMBER = 10;

    static {
        dims = new int[NUM_DIMS];
        periods = new boolean[NUM_DIMS];
        for (int i = 0; i < NUM_DIMS; i++) {
            dims[i] = 0;
            periods[i] = true;
        }
    }


    //min(j)max(k)(a(ik)+b(ki))
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        //ÐžÐ¿Ñ€ÐµÐ´ÐµÐ»ÑÐµÐ¼ Ñ€Ð°Ð·Ð¼ÐµÑ€ Ð¾Ð´Ð½Ð¾Ð¼ÐµÑ€Ð½Ð¾Ð¹ Ñ€ÐµÑˆÐµÑ‚ÐºÐ¸ <=> dims = {size}
        Cartcomm.Dims_create(size, dims);

        //ÐžÐ¿Ñ€ÐµÐ´ÐµÐ»ÑÐµÐ¼ Ð½Ð¾Ð²ÑƒÑŽ Ñ‚Ð¾Ð¿Ð¾Ð»Ð¾Ð³Ð¸ÑŽ
        Cartcomm cart_comm = MPI.COMM_WORLD.Create_cart(dims, periods, false);

        //ÐžÐ¿Ñ€ÐµÐ´ÐµÐ»ÑÐµÐ¼ ÑÐ¾ÑÐµÐ´ÐµÐ¹
        ShiftParms source_dest = cart_comm.Shift(0, -1);

        int[] a = new int[size];
        int[] b = new int[size];
        for (int i = 0; i < size; i++) {
            a[i] = (int) (Math.random() * NUMBER);
            b[i] = (int) (Math.random() * NUMBER);
        }

        //ÐÑƒÐ»ÐµÐ²Ð¾Ð¹ Ð¿Ñ€Ð¾Ñ†ÐµÑÑ Ð²Ñ‹Ð²Ð¾Ð´Ð¸Ñ‚ ÑÐ³ÐµÐ½ÐµÑ€Ð¸Ñ€Ð¾Ð²Ð°Ð½Ð½Ñ‹Ðµ Ð¼Ð°Ñ‚Ñ€Ð¸Ñ†Ñ‹
        int[] recvbufA = new int[size * size];
        int[] recvbufB = new int[size * size];
        cart_comm.Gather(a, 0, size, MPI.INT, recvbufA, 0, size, MPI.INT, 0);
        cart_comm.Gather(b, 0, size, MPI.INT, recvbufB, 0, size, MPI.INT, 0);
        if (rank == 0) {
            System.out.println("Matrix A: ");
            printMattrix(recvbufA, size);
            System.out.println("Matrix B: ");
            printMattrix(recvbufB, size);
        }

        //Ð˜Ð½Ð´ÐµÐºÑ ÑÑ‚Ñ€Ð¾ÐºÐ¸ Ð¼Ð°Ñ‚Ñ€Ð¸Ñ†Ñ‹ B, ÐºÐ¾Ñ‚Ð¾Ñ€Ð°Ñ Ð² Ð´Ð°Ð½Ð½Ñ‹Ð¹ Ð¼Ð¾Ð¼ÐµÐ½Ñ‚ Ð½Ð°Ñ…Ð¾Ð´Ð¸Ñ‚ÑÑ Ð² Ð¼Ð°ÑÑÐ¸Ð²Ðµ b
        int index = rank;

        int localMax = 0;
        for (int k = 0; k < size; k++) {
            if (localMax < (b[k] + a[k])) localMax = (b[k] + a[k]);
        }
        cart_comm.Sendrecv_replace(b, 0, size, MPI.INT, source_dest.rank_dest, 0, source_dest.rank_source, 0);

        int globalMin = localMax;
        for (int i = 0; i < size; i++) {
            if (++index == size)
                index = 0;
            int max = 0;
            for (int j = 0; j < size; j++) {
                if (max < (b[j] + a[j])) max = (b[j] + a[j]);
            }
            if (globalMin > max) globalMin = max;
            cart_comm.Sendrecv_replace(b, 0, size, MPI.INT, source_dest.rank_dest, 0, source_dest.rank_source, 0);

        }

        int[] minbuf = {globalMin};

        cart_comm.Gather(minbuf, 0, 1, MPI.INT, recvbufA, 0, 1, MPI.INT, 0);
        if (rank == 0) {

            System.out.print(" \n");
            for (int i = 0; i < size; i++) {
                System.out.printf("%4d\n", recvbufA[i]);
            }
        }

        MPI.Finalize();
    }

    static void printMattrix(int[] matrix, int length) {
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                System.out.format("%4d", matrix[length * i + j]);
            }
            System.out.println();
        }
    }
}
