
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
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.management.ConstructorParameters;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Master {

    private ArrayList<String> machinesAvailable;
    private boolean full;
    

    public Master() throws Exception {
        
        // Static.SELECTEDMACHINES are a set of machines that were previously tested to
        // connect
        // in ssh with deplot. All machines are avaiable
        // you can pick some of them or use them all.
        BufferedReader machinesPath = new BufferedReader(new FileReader(Static.SELECTEDMACHINES));

        this.machinesAvailable = new ArrayList<String>();
        String machine;

        while ((machine = machinesPath.readLine()) != null) {
            this.machinesAvailable.add(machine);

        }
        machinesPath.close();        

    }


    public void split(String input) throws Exception {

        Static.deleteOldSplits();

        ArrayList<Thread> threads = new ArrayList<Thread>();

        RandomAccessFile inputFile = new RandomAccessFile(input, "r");
        long inputLength = inputFile.length();
        // System.out.println(inputLength);

        long splitLength = inputLength / this.machinesAvailable.size();
        // System.out.println(splitLength);

        long splitCurr = 1;
        long splitEnd = splitLength;

        for (int i = 0; i < this.machinesAvailable.size(); i++) {
            String splitName = "src/splits/S" + i + ".txt";
            RandomAccessFile split = new RandomAccessFile(splitName, "rw");
            char c = ' ';
            byte b;

            // check if c!='\n' is not enough
            // it remains checking \t \r \f ...
            while (splitCurr < splitEnd || !Character.isWhitespace(c)) {
                try {
                    b = inputFile.readByte();
                    c = (char) b;
                    split.write(b);
                    splitCurr++;
                } catch (EOFException e) {
                    break;
                }
            }

            // i is the current split name and will be assigned
            // to the ith machine in machinesAvailable
            String machine = this.machinesAvailable.get(i);
            // Static.writeMachines(machine);

            String destDir = "/tmp/" + Static.USERNAME + "/splits";
            CopyThread copySplit = new CopyThread(splitName, machine, destDir);
            threads.add(copySplit);
            copySplit.start();

            splitEnd = splitCurr + splitLength - 1;
            split.close();
            // make sure to run clean before
            // so can the splits remain consistent
        }

        inputFile.close();

        for (Thread t : threads) {
            t.join();
        }

    }

    public void map() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<Thread>();

        for (int i = 0; i < this.machinesAvailable.size(); i++) {
            String machine = this.machinesAvailable.get(i);
            String splitPath = "/tmp/" + Static.USERNAME + "/splits/S" + i + ".txt";

            ExecuteSlave execSlave = new ExecuteSlave(machine, 0, splitPath);
            threads.add(execSlave);
            execSlave.start();
        }

        for (Thread t : threads) {
            t.join();
        }
    }

    public void preSuffle() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < this.machinesAvailable.size(); i++) {
            String machine = this.machinesAvailable.get(i);

            String destDir = "/tmp/" + Static.USERNAME + "/";
            CopyThread copyMachine = new CopyThread(Static.SELECTEDMACHINES, machine, destDir);
            threads.add(copyMachine);
            copyMachine.start();
        }

        for (Thread t : threads) {
            t.join();
        }

    }

    public void suffle() throws InterruptedException {
        preSuffle();
        ArrayList<Thread> threads = new ArrayList<Thread>();

        for (int i = 0; i < this.machinesAvailable.size(); i++) {
            String machine = this.machinesAvailable.get(i);
            String mapPath = "/tmp/" + Static.USERNAME + "/maps/UM" + i + ".txt";

            ExecuteSlave execSlave = new ExecuteSlave(machine, 1, mapPath);
            threads.add(execSlave);
            execSlave.start();

        }
        for (Thread t : threads) {
            t.join();
        }

    }

    public void reduce() throws IOException, InterruptedException {
        ArrayList<Thread> threads = new ArrayList<Thread>();

        for (int i = 0; i < this.machinesAvailable.size(); i++) {
            String machine = this.machinesAvailable.get(i);
            ExecuteSlave execSlave = new ExecuteSlave(machine, 2);
            threads.add(execSlave);
            execSlave.start();
        }

        for (Thread t : threads) {
            t.join();
        }
    }

    public void aggregateReduces() {
        ArrayList<Thread> threads = new ArrayList<Thread>();

        try {
            Static.createDirectory("/results");
            

            for (int i = 0; i < this.machinesAvailable.size(); i++) {
                String machine = this.machinesAvailable.get(i);
                String dirPath = "/tmp/"+Static.USERNAME+"/reduces";
                String destDirPath = "/tmp/"+Static.USERNAME+"/results" ;
                FileThread copyReduces = new FileThread(machine,dirPath,destDirPath);
                threads.add(copyReduces);
                copyReduces.start();
            }

            for (Thread t : threads) {
                t.join();
            }

        } catch (InterruptedException | IOException e) {
            
            e.printStackTrace();
        }
    }

    public void printResults(){
        aggregateReduces();
        HashMap<String, Integer> occurrences = new HashMap<String, Integer>();

        BufferedReader results = null;
        File resultsDir = new File("/tmp/" + Static.USERNAME + "/results");
        File[] allResults = resultsDir.listFiles();

        try {
            for(File res : allResults ){
                results = new BufferedReader(new FileReader(res));
                String[] map = results.readLine().split(" ");
                occurrences.put(map[0],Integer.parseInt(map[1]));
                results.close();
            }

            Static.showResult(occurrences);
        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }
    public static void main(String[] args) throws Exception {
       
        String input = args[0];
        
        
        long startTime, endTime, splitTime, mapTime, shuffleTime, reduceTime;
        
        Master master = new Master(); 

        System.out.println("********************************************************");
		System.out.println("Start spliting with " + master.machinesAvailable.size() + " machines..." );
		System.out.println("********************************************************");
        startTime = System.currentTimeMillis();
        master.split(input);
        endTime = System.currentTimeMillis();
        System.out.println("SPLIT FINISHED");
		splitTime = endTime - startTime;
        
        
        System.out.println("********************************************************");
		System.out.println("Start mapping ..." );
		System.out.println("********************************************************");
        startTime = System.currentTimeMillis();
        master.map();
        endTime = System.currentTimeMillis();
        System.out.println("MAP FINISHED");
        mapTime = endTime - startTime;

        System.out.println("********************************************************");
		System.out.println("Start shuffling ..." );
		System.out.println("********************************************************");
        startTime = System.currentTimeMillis();
        master.suffle();
        endTime = System.currentTimeMillis();
        System.out.println("SHUFFLE FINISHED");
        shuffleTime = endTime - startTime;
        

        System.out.println("********************************************************");
		System.out.println("Start reducing ..." );
		System.out.println("********************************************************");
        startTime = System.currentTimeMillis();
        master.reduce();
        endTime = System.currentTimeMillis();
        System.out.println("REDUCE FINISHED");
        reduceTime = endTime - startTime;

        System.out.println("********************************************************");
        System.out.println("Results !" );
        System.out.println("********************************************************");
        master.printResults();
        
        

        System.out.println("********************************************************");
        System.out.println("Times : " );
		System.out.println("********************************************************");
		System.out.println("Time to split: " + splitTime + "ms");
		System.out.println("Time to map: " + mapTime + "ms");
		System.out.println("Time to shuffle: " + shuffleTime + "ms");
		System.out.println("Time to reduce: " + reduceTime + "ms");
    
        
		

    }

  
}