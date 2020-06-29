

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;


/** Test the ssh connection and indicate the available machines to use  */
public class ConnectionThread extends Thread {
   
    private String machine;
    private ConcurrentLinkedQueue<String> machinesAvailable;
    
    public ConnectionThread(String machine, ConcurrentLinkedQueue<String> machinesAvailable) {
        this.machine = machine;
        this.machinesAvailable = machinesAvailable;
    }

 
    public void run() {
        
        System.out.println("ConnectionThread running");
        BufferedReader out = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("ssh", Static.USERNAME + "@" + machine, "echo working");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            out = new BufferedReader(new InputStreamReader(process.getInputStream()));

            boolean b = process.waitFor(Static.TIMEOUT, TimeUnit.SECONDS);

            if (!b) {
                // process.destroy();
                System.out.println("TIMEOUT, " + machine + " : connection failed");
            }

            String s;
            if ((s = out.readLine()) != null && s.equals("working")) {
                System.out.println(machine + " : connection succeeded");
                this.machinesAvailable.add(machine);
            }

            else
                System.out.println(machine + " : connection failed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}