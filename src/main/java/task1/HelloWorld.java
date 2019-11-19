package task1;

import mpi.MPI;

public class HelloWorld {
    public static void main(String[] args) throws Exception {

        if(args.length < 2) {
            args = new String[]{"2", "", ""};
        }

        MPI.Init(args);

        int me = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        System.out.println("Hi from <" + me + ">");
        MPI.Finalize();
    }
}
