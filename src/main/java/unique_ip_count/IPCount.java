package unique_ip_count;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class IPCount {
    private String FILE_NAME = "";

    private static final int WORKERS = 8;

    private static final int BATCH_SIZE = 5000;

    private final ArrayBlockingQueue<List<String>> queue = new ArrayBlockingQueue<>(10);

    private final List<String> poison = List.of("poison");

    private byte[] addresses = new byte[536870912];

    private AtomicLong unique_addresses = new AtomicLong(0);

    public IPCount(String fileName) throws Exception {
        this.FILE_NAME = fileName;
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
        System.out.println(unique_addresses);
    }

    private class Result {
        public long ipToLong(String ipAddress){
            long result = 0;
            String[] atoms = ipAddress.split("\\.");
            for (int i = 3; i >= 0; i--) {
                result |= (Long.parseLong(atoms[3 - i]) << (i * 8));
            }
            return result;
        }

        private byte getBit(int part, int position)
        {
            return (byte) ((addresses[part] >> position) & 1);
        }

        private void setBit(int part, int position){
            addresses[part] = (byte) (addresses[part] | (1 << position));
        }

        public void checkIP(String address){
            long index = ipToLong(address);
            int part = Math.toIntExact(index % 536870912);
            int position = Math.toIntExact(index / 536870912);
            if(getBit(part, position) == 0){
                unique_addresses.getAndIncrement();
                setBit(part, position);
            }
        }
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
                for (String line : lines) {
                    result.checkIP(line);
                }
            }
            return result;
        }
    }
}