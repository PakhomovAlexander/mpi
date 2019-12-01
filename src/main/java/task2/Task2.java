package task2;

import mpi.MPI;

public class Task2 {

    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        double a = -0.9, b = 0.8;
        double[] eps = new double[]{0.0000000001};
        int n = 10;
        int k = MPI.COMM_WORLD.Size();
        int count = (int) Math.ceil(n * 1.0 / k);
        int xSize = count * k;


        if (rank == 0) {
            masterProcess(a, b, eps, n, k, count, xSize);
        } else {
            slaveProcess(count, xSize);
        }

        MPI.Finalize();
    }

    private static void slaveProcess(int count, int xSize) {
        double[] x = new double[xSize];
        double[] epsReceived = new double[1];
        MPI.COMM_WORLD.Bcast(x, 0, count, MPI.DOUBLE, 0);

        double[] xReceived = new double[xSize];
        MPI.COMM_WORLD.Scatter(x, 0, count, MPI.DOUBLE, xReceived, 0, count, MPI.DOUBLE, 0);

        MPI.COMM_WORLD.Bcast(epsReceived, 0, 1, MPI.DOUBLE, 0);

        double[] y = calculate(count, xSize, xReceived, epsReceived[0]);

        MPI.COMM_WORLD.Gather(y, 0, count, MPI.DOUBLE, new double[xSize], 0, count, MPI.DOUBLE, 0);
    }

    private static void masterProcess(double a, double b, double[] eps, int n, int k, int count, int xSize) {
        double[] xReceived = new double[xSize];
        double[] x = getX(a, b, n, k, count);

        MPI.COMM_WORLD.Bcast(x, 0, count, MPI.DOUBLE, 0);
        MPI.COMM_WORLD.Scatter(x, 0, count, MPI.DOUBLE, xReceived, 0, count, MPI.DOUBLE, 0);
        MPI.COMM_WORLD.Bcast(eps, 0, 1, MPI.DOUBLE, 0);

        double[] y = calculate(count, xSize, xReceived, eps[0]);

        double[] result = new double[xSize];

        MPI.COMM_WORLD.Gather(y, 0, count, MPI.DOUBLE, result, 0, count, MPI.DOUBLE, 0);

        print(n, x, result);
    }

    private static double[] calculate(int count, int xSize, double[] xReceived, double eps) {
        double[] y = new double[xSize];
        for (int i = 0; i < count; i++) {
            y[i] = calculateFunc(xReceived[i], eps);
        }
        return y;
    }

    private static double[] getX(double a, double b, int n, int k, int count) {
        double[] x;
        x = new double[count * k];
        double step = (b - a) / (n - 1);
        for (int i = 0; i < n; i++) {
            x[i] = a;
            a += step;
        }
        return x;
    }

    private static void print(int n, double[] x, double[] result) {
        String leftAlignFormat = "|  %,.14f  |  %,.14f  |  %,.14f  |%n";
        printHeader();
        for (int i = 0; i < n; i++) {
            System.out.format(leftAlignFormat, x[i], exactFunc(x[i]), result[i]);
        }
        printFooter("+--------------------+---------------------+--------------------+%n");
    }

    private static void printFooter(String s) {
        System.out.format(s);
    }

    private static void printHeader() {
        System.out.format("+--------------------+---------------------+--------------------+%n");
        System.out.format("| X                  | Exact value         | Found value        |%n");
        System.out.format("+--------------------+---------------------+--------------------+%n");
    }


    // ln(x + sqrt(x^2 + 1))
    private static double exactFunc(double x) {
        return Math.log(x + Math.sqrt(x * x + 1));
    }

    // ln(x + sqrt(x^2 + 1))
    private static double calculateFunc(double x, double eps) {
        double res = x;
        double prevRes;
        double multiplier = 1;
        int i = 3;

        do {
            prevRes = res;
            multiplier = -(multiplier * (i - 2) / (i - 1));
            res += multiplier * (Math.pow(x, i) / i);
            i += 2;
        } while (Math.abs(prevRes - res) > eps);

        return res;
    }
}
