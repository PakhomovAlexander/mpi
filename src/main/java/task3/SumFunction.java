package task3;

import mpi.Datatype;
import mpi.MPIException;
import mpi.User_function;

class SumFunction extends User_function {

    @Override
    public void Call(Object o, int i, Object o1, int i1, int i2, Datatype datatype) throws MPIException {
        try {
            int[] input = (int[]) o;
            int[] res = (int[]) o1;

            for(int j = 0; j < i2; j++) {
                res[j] = Math.abs(res[j]) + Math.abs(input[j]); // +=
            }
        } catch (MPIException e) {
            e.printStackTrace();
        }
    }
}
