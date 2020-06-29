
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.management.ConstructorParameters;

import java.util.concurrent.ConcurrentLinkedQueue;



public class Deploy {
    
    private Object[] machinesAvailable;
    private int machinesNumber;
    private boolean full;


    public Deploy(int machinesNumber,boolean full){
        this.machinesAvailable = null;
        this.machinesNumber = machinesNumber;
        this.full = full;
    }


     /**
     * 
     * @param filename in which there are the full machine names
     */ 

    public void testConnection(String filename){
        ArrayList<ConnectionThread> connectionThreads = new ArrayList<ConnectionThread>();
        ConcurrentLinkedQueue<String> machines = new ConcurrentLinkedQueue<String>();
        File file = new File(filename);
        BufferedReader br = null; 
       
        try {
            br = new BufferedReader(new FileReader(file));
            String machine; 
            
            //test ssh connection
            while ((machine = br.readLine()) != null){
                ConnectionThread connection = new ConnectionThread(machine, machines);
                connection.start();
                connectionThreads.add(connection);
            }
            
            //wait that the thread finishes 
            //so that we have the list of available machines
            for(ConnectionThread t : connectionThreads){
                t.join();
            }
            
            //here we have all the available machine for the execution
            this.machinesAvailable = machines.toArray();
             

            br.close();
            //System.out.println(this.machinesAvailable.size());
            if(full)
                this.machinesNumber = this.machinesAvailable.length;
            
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                br.close();
            } catch (Exception e) {}
        }

    }

    public void compileAll() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("javac","src/Master.java","src/ConnectionThread.java", "src/CopyThread.java", 
        "src/CopyThread.java", "src/Deploy.java", "src/Master.java", "src/Slave.java","src/Static.java");
		pb.redirectErrorStream(true);
		Process process = pb.start();

        process.waitFor();
        int err = process.waitFor();
        if(err == 0) {
			System.out.println("All files compiled successfully !");
		}
		else {
            System.out.println("An error occured durring the compilation !");
		}
        
    }

    public void generateJar() throws IOException, InterruptedException {
        //jar cvfm slave.jar manifest.txt *.class
        ProcessBuilder pb = new ProcessBuilder("jar", "cvfm", "src/slave.jar", "src/manifest.txt", "-C", "bin", ".");
		pb.redirectErrorStream(true);
		Process process = pb.start();

		int err = process.waitFor();
        if(err == 0) {
			System.out.println("Jar generated successfully !");
		}
		else {
            System.out.println("An error occured durring the jar generation !");
		}

    }

    public void deploySlave() throws Exception {


        //by using build.xml 
        //deploy depends on build which compile all java files 
        //and put them in the bin folder
        //compileAll();
        generateJar();

        Static.deleteOldMachines();

        ArrayList<Thread> threads = new ArrayList<Thread>();
        

        if(this.machinesAvailable.length<machinesNumber)
            throw new Exception("Not enough available machines");


        for (int i = 0; i < this.machinesNumber; i++) {

            String machine = (String) this.machinesAvailable[i];
            Static.writeMachines(machine);
            
            String destDir = "/tmp/"+Static.USERNAME+"/"; 
            CopyThread copyJar = new CopyThread("src/slave.jar",machine,destDir);
            threads.add(copyJar);
            copyJar.start();
        }

        for (Thread t : threads) {
			t.join();
		}

    }

    public static void main(String[] args) throws Exception {
       

        int machinesNumber = Integer.parseInt(args[0]);
        boolean full = false;
        if(machinesNumber==0){
            full = true;
        }

        Deploy deploy = new Deploy(machinesNumber,full); 
        deploy.testConnection("machines.txt");
        deploy.deploySlave();

        System.out.println("*******************************************************************************");
		System.out.println("Number of available machine for this run: " + deploy.machinesAvailable.length );
		System.out.println("*******************************************************************************");
        
		

    }
    
}