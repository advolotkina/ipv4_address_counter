package unique_ip_count;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class IPCountHashMap {

    private String FILE_NAME = "";
    private static final int WORKERS = 8;

    private static final int BATCH_SIZE = 5000;

    private final ArrayBlockingQueue<List<String>> queue = new ArrayBlockingQueue<>(10);

    private final List<String> poison = List.of("poison");

    private ConcurrentHashMap<String, Byte> addresses = new ConcurrentHashMap<>();

    public IPCountHashMap(String FILE_NAME) throws Exception {
        this.FILE_NAME = FILE_NAME;
        run();
    }

    public void run() throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();
        ArrayList<Future<Result>> futures = new ArrayList<Future<Result>>();
        Future<Result> producer = executor.submit(new Producer());
        futures.add(producer);
        for (int i = 0; i < WORKERS; i++) {
            Future<Result> worker = executor.submit(new Worker());
            futures.add(worker);
        }
        List<Result> results = new ArrayList<>();
        for (Future<Result> future : futures) {
            Result result = future.get();
            results.add(result);
        }
        executor.shutdown();
        System.out.println(addresses.size());
    }

    private class Result {
    }

    class Producer implements Callable<Result> {
        @Override
        public Result call() throws Exception {
            try (BufferedReader reader = Files.newBufferedReader(Path.of(FILE_NAME), StandardCharsets.UTF_8)) {
                String readLine = "";
                List<String> lines = new ArrayList<String>(BATCH_SIZE);
                int batchIndex = 0;
                while ((readLine = reader.readLine()) != null) {
                    lines.add(readLine);
                    batchIndex++;
                    if (batchIndex == BATCH_SIZE) {
                        queue.put(lines);
                        lines = new ArrayList<>(BATCH_SIZE);
                        batchIndex = 0;
                    }
                }
                queue.put(lines);
                for (int i = 0; i < WORKERS; i++) {
                    queue.put(poison);
                }
                return new Result();
            }
        }
    }

    class Worker implements Callable<Result> {
        @Override
        public Result call() throws Exception {
            Result result = new Result();
            while (true) {
                List<String> lines = queue.take();
                if (lines == poison) {
                    break;
                }
                byte a = 0;
                for (String line : lines) {
                    addresses.put(line, a);
                }
            }
            return result;
        }
    }
}