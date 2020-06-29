import java.io.IOException;

public class ExecuteClean extends Thread {

    private String machine;

    ExecuteClean(String machine) {
        this.machine = machine;
    }

    public void executeClean() throws IOException, InterruptedException {
        String base = "/tmp/" + Static.USERNAME;
        ProcessBuilder pb = new ProcessBuilder("ssh", this.machine, "rm", "-rfd", base + "/maps/", base + "/splits/",
                base + "/shuffles/", base + "/shufflesreceived/",base+"/reduces/", base+"/results", base + "/machinesSelected.txt", base + "/slave.jar");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        process.waitFor();
        int err = process.waitFor();
        if (err == 0) {
            System.out.println("Cleaned !");
        } else {
            System.out.println("An error occured durring the clean !");
        }
    }

    public void run() {
        try {
            executeClean();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}