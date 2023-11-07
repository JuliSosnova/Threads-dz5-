package lesson5.ru.croc.javaschool.threads;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Стартовый класс.
 */
public class Main {

    /**
     * Точка старта приложения
     *
     * @param args
     *          стартовые аргументы
     */
    public static void main(String[] args){

        var matrix = MatrixExample.D;

        //printResult("detOneThread", matrix, () -> detOneThread(matrix.getMatrix()));
        printResult("detManyThreads", matrix, () -> detManyThreads(matrix.getMatrix(),6));
    }

    /**
     * Рекурсивный расчет определителя матрицы методом разложения по строке в один поток.
     *
     * @param a
     *          матрица
     * @return определитель матрицы
     */
    private static long detOneThread(long[][] a) {
        if (a.length == 1) {
            return a[0][0];
        }
        var result = 0L;
        for (var i = 0; i < a.length; i++) {
            var sign = (i % 2 == 0 ? 1 : -1);
            result = result + sign * a[i][0] * detOneThread(minor(a, i));
        }

        return result;
    }
    /**
     * Рекурсивный расчет определителя матрицы методом разложения по строке в несколько потоков.
     *
     * @param a
     *          матрица
     *  @param numthreads количество потоков
     * @return определитель матрицы
     */
    private static long detManyThreads(long[][] a,int numthreads) {
        if (a.length == 1) {
            return a[0][0];
        }
        var executor= Executors.newFixedThreadPool(numthreads) ;//пул из переданного числа потоков
        var future= new Future[a.length];
        for (var i = 0; i < a.length; i++) {
            int row=i;
            future[i]=executor.submit(()->{ //запуск выполнения задачи
                var sign = (row % 2 == 0 ? 1 : -1);
                return sign * a[row][0] * detOneThread(minor(a, row));
            });
        }
        executor.shutdown();//остановка выполнения
        var result = 0L;
        for (var i = 0; i < a.length; i++) {
            try{
                result+=(long)future[i].get();
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
        return result;
    }

    /**
     * Вычисляет минорную матрицу от заданной. Удаляется первый столбец и заданная строка.
     *
     * @param original
     *          матрица, от которой требуется вычислить минорную
     * @param exceptRow
     *          удаляемая строка
     * @return минорная матрица
     */
    public static long[][] minor(final long[][] original, int exceptRow) {
        long[][] minor = new long[original.length-1][original.length-1];
        var minorLength = minor.length;
        for (int i = 0; i < exceptRow; i++) {
            System.arraycopy(original[i], 1, minor[i], 0, minorLength);
        }
        for (int i = exceptRow + 1; i < original.length; i++) {
            System.arraycopy(original[i], 1, minor[i - 1], 0, minorLength);
        }
        return minor;
    }

    /**
     * Выводит в консоль результат работы.
     *
     * @param method
     *          название метода расчета
     * @param matrix
     *          матрица из предложенных для примера
     * @param executor
     *          алгоритм расчета определителя матрицы
     */
    private static void printResult(String method, MatrixExample matrix, Supplier<Long> executor) {
        var start = System.currentTimeMillis();
        var det = executor.get();
        var executionTime = (System.currentTimeMillis() - start);
        System.out.println("Method -> " + method);
        System.out.println("Matrix name -> " + matrix.name());
        System.out.println("Matrix dimension -> " + matrix.getMatrix().length);
        System.out.println("Matrix determinant  = " + det + (det != matrix.getDeterminant() ? " ERROR!" : ""));
        System.out.println("Execution time -> " + executionTime);
        System.out.println();
    }
}