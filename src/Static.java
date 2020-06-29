import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Static {

    public static final String USERNAME = "eblinda";
    public static final int TIMEOUT = 30;
    public static final String SELECTEDMACHINES = "machinesSelected.txt";

    public static void createDirectory(String dirName) throws InterruptedException, IOException {
        
        String dirPath = "/tmp/"+Static.USERNAME+dirName;
        ProcessBuilder pb = new ProcessBuilder("mkdir", "-p", dirPath);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        int err = process.waitFor();
        if(err == 0) {
          System.out.println("Directory created successfully !");
        }
        else {
                System.out.println("Cannot create directory !");
                
        }
    }

    public static void deleteOldMachines(){
        File file = new File(Static.SELECTEDMACHINES);
        file.delete();
    }

    public static void deleteOldSplits() throws IOException, InterruptedException {
      // File splitDir = new File("src/splits");
      // File[] splitFiles = splitDir.listFiles();
      // for(File split : splitFiles ){
      //   split.delete();
      // }

      ProcessBuilder pb = new ProcessBuilder("rm","-rfd","src/splits/");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        int err = process.waitFor();
        if(err == 0) {
          System.out.println("Old splits removed !");
        }
        else {
                System.out.println("Cannot remove splits !");
                
        }

        ProcessBuilder pb2 = new ProcessBuilder("mkdir","-p","src/splits/");
        pb2.redirectErrorStream(true);
        Process process2 = pb2.start();

        int err2 = process2.waitFor();
        if(err == 0) {
          System.out.println("Split folder created !");
        }
        else {
                System.out.println("Split folder not created !");
                
        }

    }

    public static void writeMachines(String machine) throws IOException {
        BufferedWriter writer = null;
    
        try {
          writer = new BufferedWriter(new FileWriter(Static.SELECTEDMACHINES, true));
          writer.write(machine+"\n");
        } catch (Exception e) {
          e.printStackTrace();
        }
        finally {
          writer.close();
        }
        
    }

    public static int hashFunction(String s){
      return (s.hashCode() == Integer.MIN_VALUE) ? 0 : Math.abs(s.hashCode());
  }

    public static void showResult(HashMap<String, Integer> results){

      ArrayList<String> resultLines = new ArrayList<String>(); 
      results.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByKey())
        .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
        .forEach(entry -> {
          resultLines.add(entry.getKey() + " " + entry.getValue() + "\n");
          System.out.println(entry.getKey() + " " + entry.getValue());
          });
   
          BufferedWriter saveResults = null;
          try {
            saveResults = new BufferedWriter(new FileWriter("results"));
            for(String res : resultLines){
              saveResults.write(res);
            }
            
          } catch (Exception e) {
            e.printStackTrace();
          }
          finally{
            try {
              saveResults.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          
        
    
    
    }

}