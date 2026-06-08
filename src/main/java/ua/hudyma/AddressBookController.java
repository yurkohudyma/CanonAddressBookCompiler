package ua.hudyma;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class AddressBookController extends JFrame {

    static final File USER_HOME = new File(System.getProperty("user.home"));
    static JTextField inputEmail, inputName, inputPath;
    static JLabel labelEmail, labelName;
    static JButton generateButton, pathChooserButton;
    static JFrame jFrame;
    static GenerateHandler generateHandler = new GenerateHandler();
    static PathSelectorHandler pathSelectorHandler = new PathSelectorHandler();
    static String emailResult, nameResult, CANON_FILENAME = "adrs_book_CANON_1440.abk";
    static String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    static Map<String, String> contactMap = new HashMap<>();
    static Path outputPath;

    private static final String DN = "dn:", CN = "cn::", MAIL_ADDRESS = "mailaddress:", SPACE = " ";

    public AddressBookController (String header) {
        super(header);
        setLayout(new FlowLayout());
        jFrame = new JFrame();
        inputEmail = new JTextField(12);
        inputName = new JTextField(12);
        inputPath = new JTextField(12);
        labelEmail = new JLabel("Email:");
        labelName = new JLabel("Name:");
        generateButton = new JButton("Додати контакт");
        pathChooserButton = new JButton("Вибрати шлях");
        add(labelEmail);
        add(inputEmail);
        add(labelName);
        add(inputName);
        add(pathChooserButton);
        add(inputPath);
        add(generateButton);
        inputEmail.addActionListener(generateHandler);
        inputName.addActionListener(generateHandler);
        generateButton.addActionListener(generateHandler);
        inputPath.addActionListener(generateHandler);
        pathChooserButton.addActionListener(pathSelectorHandler);
        inputPath.setText(String.valueOf(USER_HOME));
        outputPath = Paths.get(USER_HOME + "/" + CANON_FILENAME);
    }

    static class PathSelectorHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            var chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(jFrame) == JFileChooser.APPROVE_OPTION) {
                var directory = chooser.getSelectedFile();
                outputPath = directory.toPath().resolve(CANON_FILENAME);
                inputPath.setText(directory.getAbsolutePath());
            }
        }
    }

    static class GenerateHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == generateButton) {
                emailResult = inputEmail.getText();
                if (emailResult.isEmpty() || !emailResult.matches(EMAIL_REGEX)) {
                    inputEmail.setText("Помилка імейлу");
                    return;
                }
                nameResult = inputName.getText();
                if (nameResult.isEmpty() || nameResult.equals("Помилка імені")){
                    inputName.setText("Помилка імені");
                    return;
                }
                var pathText = inputPath.getText();
                if (pathText.isEmpty()){
                    inputPath.setText("Виберіть шлях");
                }
                else {
                    contactMap.put(emailResult, nameResult);
                    try {
                        generateAddressBook(contactMap);
                        JOptionPane.showMessageDialog(jFrame, "Контакт додано", "Успіх", JOptionPane.INFORMATION_MESSAGE);
                        inputEmail.setText("");
                        inputName.setText("");
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(jFrame, "Помилка IOException", "Халепа", JOptionPane.ERROR_MESSAGE);
                    }
                    catch (Exception ex){
                        JOptionPane.showMessageDialog(jFrame, "Проблема із записом файла", "Помилка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private static void generateAddressBook(Map<String, String> contactMap) throws IOException {
        var header = readFile("src/main/resources/template.header");
        var template = readFile("src/main/resources/addressbook.template");
        if (outputPath == null) {
            JOptionPane.showMessageDialog(jFrame, "Path is NULL", "Помилка", JOptionPane.ERROR_MESSAGE);
        }
        try (var writer = Files
                .newBufferedWriter(outputPath)) {
            for (String line : header) {
                writer.write(line);
                writer.newLine();
            }
            var dnCounter = 1;
            for (Map.Entry<String, String> map : contactMap.entrySet()) {
                var email = map.getKey();
                var name = map.getValue();
                var encodedName = Base64.getEncoder()
                        .encodeToString(name.getBytes());
                writer.newLine();
                for (String templateItem : template) {
                    var line = switch (templateItem) {
                        case DN -> DN + SPACE + dnCounter++;
                        case CN -> CN + SPACE + encodedName;
                        case MAIL_ADDRESS -> MAIL_ADDRESS + SPACE + email;
                        default -> templateItem;
                    };
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }

    private static List<String> readFile(String fileName) throws IOException {
        return Files.readAllLines(Paths.get(fileName));
    }
}
