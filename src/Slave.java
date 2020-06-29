import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Slave {
    private String hostname;
    private ArrayList<String> machinesSelected;

    public Slave() throws IOException {
        this.hostname = InetAddress.getLocalHost().getHostName();

        BufferedReader machinesPath = new BufferedReader(
                new FileReader("/tmp/" + Static.USERNAME + "/" + Static.SELECTEDMACHINES));

        this.machinesSelected = new ArrayList<String>();
        String machine;
        while ((machine = machinesPath.readLine()) != null) {
            this.machinesSelected.add(machine);

        }
        machinesPath.close();
    }

    public static void map(String splitPath) {
        File file = new File(splitPath);
        BufferedReader split = null;
        BufferedWriter mapFile = null;
        char mapNumber = splitPath.charAt(21);

        try {
            Static.createDirectory("/maps");

            split = new BufferedReader(new FileReader(file));
            mapFile = new BufferedWriter(new FileWriter("/tmp/" + Static.USERNAME + "/maps/UM" + mapNumber + ".txt"));

            String line;

            while ((line = split.readLine()) != null) {
                // for each line we split into several words
                String[] words = line.split(" ");
                for (String w : words) {
                    if (!w.isBlank()) {
                        w = w.toLowerCase();
                        mapFile.write(w + " 1\n");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                split.close();
                mapFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void shuffle(String mapPath) throws InterruptedException {
        File file = new File(mapPath);
        BufferedReader mapFile = null;
        BufferedWriter hashedFile = null;

        String line;
        ArrayList<Integer> wordsHashs = new ArrayList<Integer>();

        try {
            Static.createDirectory("/shuffles");
            mapFile = new BufferedReader(new FileReader(file));

            while ((line = mapFile.readLine()) != null) {
                String[] words = line.split(" ");
                int hash = Static.hashFunction(words[0]);
                // System.out.println(line);
                // System.out.println(hash);
                if (!wordsHashs.contains(hash)) {
                    wordsHashs.add(hash);
                }

                hashedFile = new BufferedWriter(new FileWriter(
                        "/tmp/" + Static.USERNAME + "/shuffles/" + hash + "-" + this.hostname + ".txt", true));
                hashedFile.write(line + "\n");
                hashedFile.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mapFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        ArrayList<Thread> threads = new ArrayList<Thread>();

        for (int hash : wordsHashs) {
            int index = hash % this.machinesSelected.size();
            String machine = this.machinesSelected.get(index);
            String hashedFilename = "/tmp/" + Static.USERNAME + "/shuffles/" + hash + "-" + this.hostname + ".txt";
            String destDir = "/tmp/" + Static.USERNAME + "/shufflesreceived/";

            CopyThread copyShuffle = new CopyThread(hashedFilename, machine, destDir);
            threads.add(copyShuffle);
            copyShuffle.start();
        }

        for (Thread t : threads) {
            t.join();
        }

    }

    public static void reduce() {

        BufferedReader hashedFile = null;
        File shufflesDir = new File("/tmp/" + Static.USERNAME + "/shufflesreceived/");
        File[] shufflesReceived = shufflesDir.listFiles();

        HashMap<String, Integer> occurences = new HashMap<String, Integer>();

        try {
            Static.createDirectory("/reduces");

            for (File shuffle : shufflesReceived) {
                hashedFile = new BufferedReader(new FileReader(shuffle));
                String line;

                while ((line = hashedFile.readLine()) != null) {
                    // for each line we split into several words
                    String word = line.split(" ")[0];
                    // !=null means the string is already in the map
                    if (occurences.get(word) != null) {
                        int increment = occurences.get(word) + 1;
                        occurences.replace(word, increment);

                        // if not we put its value as
                        // it is the first time we meet it
                    } else {
                        occurences.put(word, 1);
                    }
                }
                hashedFile.close();
            }

            for (Entry<String, Integer> entry : occurences.entrySet()) {
                String word = entry.getKey();
                int hash = Static.hashFunction(word);
                int occ = entry.getValue();
                BufferedWriter reducedFile = new BufferedWriter(
                        new FileWriter("/tmp/"+ Static.USERNAME + "/reduces/" + hash + ".txt"));
                        reducedFile.write(word + " " + occ + "\n");
                        reducedFile.close();
            }


        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int mode = Integer.parseInt(args[0]);
        String splitPath;

        // System.out.println(splitPath);

        switch (mode) {
            case 0:
                splitPath = args[1];
                map(splitPath);
				break;
            case 1:
                Slave s = new Slave();
                splitPath = args[1];
				s.shuffle(splitPath);
				break;
        
            case 2:
            reduce();
            
            break;

			default:
				break;
		}
    }
    
}