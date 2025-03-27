import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardOpenOption.CREATE;

public class TagExtractor extends JFrame {
    JPanel outputPnl;
    JTextArea outputTA;
    JScrollPane scroller;
    JPanel titlePnl;
    JPanel cmdPnl;
    JButton chooseFileBtn;
    JButton uploadBtn;
    JFileChooser chooser;
    Map<String, Integer> wordFreqs;


    public TagExtractor(){
        wordFreqs = new TreeMap<>();

        JPanel mainPnl = new JPanel();
        mainPnl.setLayout(new BorderLayout());

        JScrollPane outerScroller = new JScrollPane(mainPnl);

        createOutputPnl();
        mainPnl.add(outputPnl, BorderLayout.CENTER);

        titlePnl = new JPanel();
        JLabel titleLbl = new JLabel("Tag Extractor", JLabel.CENTER);
        titleLbl.setFont(new Font("Serif", Font.PLAIN, 30));
        titleLbl.setBorder(new EmptyBorder(0, 0, 20, 0));
        titlePnl.add(titleLbl);

        mainPnl.add(titlePnl, BorderLayout.NORTH);

        cmdPnl = new JPanel();
        cmdPnl.setLayout(new GridLayout(1, 3));
        chooseFileBtn = new JButton("Choose Text File");
        uploadBtn = new JButton("Create File from Output");
        JButton quitBtn = new JButton("Quit");
        chooseFileBtn.addActionListener((ActionEvent ae) -> {
            outputTA.setText("");
            readFile(ae);
        });
        uploadBtn.addActionListener((ActionEvent ae) -> {
            if(wordFreqs.isEmpty())
                JOptionPane.showMessageDialog(null, "You need to upload a file for processing first!", "Invalid operation", JOptionPane.INFORMATION_MESSAGE);
            else
                uploadFile(ae);
        });
        quitBtn.addActionListener((ActionEvent ae) -> {
            int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        cmdPnl.add(chooseFileBtn);
        cmdPnl.add(uploadBtn);
        cmdPnl.add(quitBtn);
        mainPnl.add(cmdPnl, BorderLayout.SOUTH);

        setSize(600, 500);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width/2-getSize().width/2, dim.height/2-getSize().height/2);

        setTitle("Sage Bushstone Tag Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(outerScroller);
        setVisible(true);
    }

    public void createOutputPnl(){
        outputPnl = new JPanel();
        outputTA = new JTextArea(15, 40);
        outputTA.setEditable(false);
        scroller = new JScrollPane(outputTA);

        outputPnl.add(scroller);
    }

    public void readFile(ActionEvent ae) {
        chooser = new JFileChooser();
        File selectedFile;
        String rec = "";
        ArrayList<String> lines = new ArrayList<>();
        Set<String> stopList = new HashSet<>();

        try
        {
            File workingDirectory = new File(System.getProperty("user.dir"));
            chooser.setCurrentDirectory(workingDirectory);

            File stopWords = new File("C:/Users/sageb/IdeaProjects/TagExtractor/src/English Stop Words.txt");
            Scanner stopReader = new Scanner(stopWords);
            while (stopReader.hasNextLine()) {
                stopList.add(stopReader.nextLine());
            }

            if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();
                try(InputStream in = new BufferedInputStream(Files.newInputStream(file));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
                {
                    int line = 0;
                    while (reader.ready()) {
                        rec = reader.readLine();
                        lines.add(rec);
                        line++;
                    }
                }
                System.out.println("\n\nData file read!");

                String[] words;
                int index = 0;
                for(String l:lines)
                {
                    words = l.replaceAll("_", "")
                            .replaceAll("[áàâäãå]", "a")
                            .replaceAll("[éèêë]", "e")
                            .replaceAll("[íìîï]", "i")
                            .replaceAll("[óòôöõ]", "o")
                            .replaceAll("[úùûü]", "u")
                            .replaceAll("ç", "c")
                            .replaceAll("ñ", "n")
                            .toLowerCase().split("[^\\w\\d']+"); // Split the record into the words
                    for(String word : words){
                        if(!stopList.contains(word) && !containsNumber(word)){
                            if(word.isEmpty())
                                continue;
                            if(wordFreqs.containsKey(word)) {
                                wordFreqs.put(word, wordFreqs.get(word)+1);
                            }
                            else{
                                wordFreqs.put(word, 1);
                            }
                        }
                    }
                    index++;
                }

                outputTA.setTabSize(10);
                for(String key : wordFreqs.keySet()){
                    outputTA.append(key + "\t" + wordFreqs.get(key) + "\n");
                }

            }
            else
            {
                JOptionPane.showMessageDialog(null, "You need to select a file to generate results!", "Invalid operation", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch (FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(null, "FileNotFound error, try again", "Invalid operation", JOptionPane.INFORMATION_MESSAGE);
            e.printStackTrace();
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "Error, try again!", "Invalid operation", JOptionPane.INFORMATION_MESSAGE);
            e.printStackTrace();
        }

    }

    public void uploadFile(ActionEvent ae){
        try
        {
            File workingDirectory = new File(System.getProperty("user.dir"));
            Path file = Paths.get(workingDirectory.getPath() + "\\src\\OutputWordFreqs.txt");

            OutputStream out =
                    new BufferedOutputStream(Files.newOutputStream(file, CREATE));
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(out));

            for(String word : wordFreqs.keySet())
            {
                writer.write(word + "     " + wordFreqs.get(word));
                writer.newLine();
            }
            writer.close();
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "Error, try again!", "Invalid operation", JOptionPane.INFORMATION_MESSAGE);
            e.printStackTrace();
        }
    }

    public boolean containsNumber(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }
}