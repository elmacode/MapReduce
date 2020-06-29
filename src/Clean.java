
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



public class Clean {
    
    private ArrayList<String> machines;
    

    public Clean() throws IOException {
        BufferedReader machinesPath = new BufferedReader(new FileReader(Static.SELECTEDMACHINES));
		//int machinesNumber = Integer.parseInt(machinesPath.readLine());
		this.machines = new ArrayList<String>();
        String machine;
		while( (machine  = machinesPath.readLine()) != null ){
            this.machines.add(machine);

        }
		machinesPath.close();
        
    }



    public void cleanMachines() throws Exception {

        ArrayList<Thread> threads = new ArrayList<Thread>();
        

        for(String machine : this.machines){
            

            ExecuteClean execClean = new ExecuteClean(machine);
            threads.add(execClean);
            execClean.start();
        }

        for (Thread t : threads) {
            t.join();
        }

    }

  

    public static void main(String[] args) throws Exception {

        Clean cleaner = new Clean(); 
        cleaner.cleanMachines();
        
		

    }
    
}