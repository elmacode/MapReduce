import java.io.IOException;

public class ExecuteSlave extends Thread {
    private String machine;
    private int mode;
    private String pathFile;

    public ExecuteSlave(String machine, int mode, String pathFile) {
        this.machine = machine;
        this.mode = mode;
        this.pathFile = pathFile;
    }

    public ExecuteSlave(String machine, int mode) {
        this.machine = machine;
        this.mode = mode;
        this.pathFile = "";
    }

    public void executeSlave() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("ssh", machine, "java", "-jar", "/tmp/" + Static.USERNAME + "/slave.jar",
                "" + mode, pathFile);

        Process process = pb.inheritIO().start();
        int err = process.waitFor();
        if (err == 0) {
            System.out.println("Successfully executed !");
        } else {
            System.out.println("Cannot execute !");
        }
    }

    public void run() {
        try {
            executeSlave();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    
}