import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileThread extends Thread {

    private String destMachine;
    private String dirPath;
    private String destDirPath;

    public FileThread(String destMachine, String dirPath, String destDirPath) {
        this.destMachine = destMachine;
        this.dirPath = dirPath;
        this.destDirPath = destDirPath;

    }

    private void copyFile(String filename) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("scp", this.destMachine + ":" + this.dirPath + "/" + filename,
                destDirPath);
        pb.redirectErrorStream(true);
        pb.inheritIO();
        Process process = pb.start();
        int err = process.waitFor(); 
        if (err == 0) {
            // System.out.println("Result copied!");
        } else {
            System.out.println("Error on copying a result !");
        }
    }

    private void copyAllFiles() throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder("ssh", this.destMachine, "ls", this.dirPath);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        int err = process.waitFor(); 
        if (err == 0) {
            System.out.println("All Results listed!");
        } else {
            System.out.println("Error on listing results !");
        }

        String filename;
        while ((filename = reader.readLine()) != null) {
            copyFile(filename);
        }

    }

    public void run() {
        try {
            copyAllFiles();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }



}