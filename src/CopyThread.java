

import java.io.BufferedReader;
import java.io.InputStreamReader;
public class CopyThread extends Thread {

    private String filename;
    private String machine; 
    private String destDir;

    public CopyThread(String filename, String machine, String destDir){
        this.filename=filename;
        this.machine=machine;
        this.destDir=destDir;
    }

    public void createDirectory() {

        System.out.println("CopyThread running: creating directory");
        try {
            ProcessBuilder pb = new ProcessBuilder("ssh", Static.USERNAME + "@" + machine, "mkdir", "-p", this.destDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            //wait until process finished 
            process.waitFor();
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void copyFile(){
        System.out.println("CopyThread running: copying file");
        // BufferedReader out = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("scp", "-p", filename,
                    Static.USERNAME + "@" + machine + ":"+destDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();
            // out = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // String s;
            // if ((s = out.readLine()) != null) {
            //     System.out.println(s);
            // }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {
        createDirectory();
        copyFile();
    }
    
}