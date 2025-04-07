import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

public class TagExtractor extends JFrame implements ActionListener {

    private JTextArea tagTextArea;
    private JLabel sourceFileNameLabel;
    private JButton selectFileButton, selectStopWordsButton, saveTagsButton;
    private JFileChooser fileChooser;
    private File sourceFile, stopWordsFile;
    private Map<String, Integer> tagFrequencies;
    private Set<String> stopWords;
    private File currentDirectory;

    public TagExtractor() {
        setTitle("Tag Extractor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tagTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(tagTextArea);
        sourceFileNameLabel = new JLabel("No file selected");
        selectFileButton = new JButton("Select Text File");
        selectStopWordsButton = new JButton("Select Stop Words File");
        saveTagsButton = new JButton("Save Tags");
        fileChooser = new JFileChooser();

        currentDirectory = new File(System.getProperty("user.dir"));
        fileChooser.setCurrentDirectory(currentDirectory);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(selectFileButton);
        buttonPanel.add(selectStopWordsButton);
        buttonPanel.add(saveTagsButton);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(new JLabel("Source File:"), BorderLayout.NORTH);
        contentPane.add(sourceFileNameLabel, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        selectFileButton.addActionListener(this);
        selectStopWordsButton.addActionListener(this);
        saveTagsButton.addActionListener(this);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TagExtractor::new);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectFileButton) {
            selectFile();
        } else if (e.getSource() == selectStopWordsButton) {
            selectStopWordsFile();
        } else if (e.getSource() == saveTagsButton) {
            saveTagsToFile();
        }
    }

    private void selectFile() {
        fileChooser.setDialogTitle("Select Text File");
        fileChooser.setCurrentDirectory(currentDirectory);
        FileNameExtensionFilter textFilter = new FileNameExtensionFilter("Text Files", "txt");
        fileChooser.setFileFilter(textFilter);
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            sourceFile = fileChooser.getSelectedFile();
            sourceFileNameLabel.setText("File: " + sourceFile.getName());
            currentDirectory = sourceFile.getParentFile();
            if (stopWordsFile != null) {
                processFile();
            }
        }
    }

    private void selectStopWordsFile() {
        fileChooser.setDialogTitle("Select Stop Words File");
        fileChooser.setCurrentDirectory(currentDirectory);
        FileNameExtensionFilter textFilter = new FileNameExtensionFilter("Text Files", "txt");
        fileChooser.setFileFilter(textFilter);
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            stopWordsFile = fileChooser.getSelectedFile();
            loadStopWords();
            currentDirectory = stopWordsFile.getParentFile();
            if (sourceFile != null) {
                processFile();
            }
        }
    }

    private void loadStopWords() {
        stopWords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(stopWordsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.trim());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading stop words file.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void processFile() {
        tagFrequencies = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase().replaceAll("[^a-z\\s]", "").split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty() && !stopWords.contains(word)) {
                        tagFrequencies.put(word, tagFrequencies.getOrDefault(word, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading text file.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        displayTags();
    }

    private void displayTags() {
        tagTextArea.setText("");
        for (Map.Entry<String, Integer> entry : tagFrequencies.entrySet()) {
            tagTextArea.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }
    }

    private void saveTagsToFile() {
        if (tagFrequencies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tags to save.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        fileChooser.setDialogTitle("Save Tags to File");
        fileChooser.setCurrentDirectory(currentDirectory);
        FileNameExtensionFilter textFilter = new FileNameExtensionFilter("Text Files", "txt");
        fileChooser.setFileFilter(textFilter);
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(fileToSave)) {
                for (Map.Entry<String, Integer> entry : tagFrequencies.entrySet()) {
                    writer.println(entry.getKey() + ": " + entry.getValue());
                }
                JOptionPane.showMessageDialog(this, "Tags saved to file.", "Success", JOptionPane.INFORMATION_MESSAGE);
                currentDirectory = fileToSave.getParentFile();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving tags to file.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}
