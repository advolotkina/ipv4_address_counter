package unique_ip_count;

import java.lang.reflect.Constructor;

public class TestAlgos {
    public static void measureTimeAndMemory(String className) throws Exception {
        int n = 10;
        double sum_time = 0;

        for(int i = 0; i < 8; i++) {
            System.out.println("Number of addresses in file: " + n);
            String FILE_NAME = "/home/zhblnd/IdeaProjects/ecwid-ip-count/src/test_files/"+ n + "_addresses.txt";
            for (int k = 0; k < 50; k++) {
                long start = System.nanoTime();
                Class<?> c = Class.forName(className);
                Constructor<?> cons = c.getConstructor(String.class);
                Object object = cons.newInstance(FILE_NAME);
                long elapsedTime = System.nanoTime() - start;
                sum_time += elapsedTime;
            }
            n *= 10;
            System.out.println(className + " | AVG time: "
                    + sum_time / 50 / 1_000_000_000.0
                    + " sec");
            sum_time = 0;
        }
    }
    public static void main(String[] args) throws Exception {
        measureTimeAndMemory("unique_ip_count.IPCount");
        measureTimeAndMemory("unique_ip_count.IPCountHashMap");
    }
}
