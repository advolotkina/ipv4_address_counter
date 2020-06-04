package unique_ip_count;

import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        String filePath;
        if (args.length > 0) {
            try {
                filePath = args[0];
                IPCount ipCount = new IPCount(filePath);
            } catch (Exception e) {
                System.exit(1);
            }
        }
    }
}
