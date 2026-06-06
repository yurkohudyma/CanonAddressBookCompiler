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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AddressBookController extends JFrame {

    static JTextField inputEmail, inputName, inputPath;
    static JLabel labelEmail, labelName;
    static JButton generateButton, pathChooserButton;
    static JFrame jFrame;
    static GenerateHandler generateHandler = new GenerateHandler();
    static PathSelectorHandler pathSelectorHandler = new PathSelectorHandler();
    static JFileChooser directorySelector;
    static String emailResult, nameResult, CANON_FILENAME = "adrs_book_CANON_1440.abk";
    static String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    static Map<String, String> contactMap = new HashMap<>();
    static File directory;
    static Path outputPath;

    private static final String DN = "dn:", CN = "cn::", MAIL_ADDRESS = "mailaddress:", SPACE = " ";

    //todo зробити єдиний хендлер для кнопки Згенерувати, дані збирати з відповідних полів у змінні.

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
        pathChooserButton.addActionListener(pathSelectorHandler);
    }

    static class PathSelectorHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == pathChooserButton) {
                directorySelector = new JFileChooser();
                int returnValue = directorySelector.showOpenDialog(jFrame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    directory = directorySelector.getCurrentDirectory();
                    outputPath = Path.of((directory.getPath() + "\\" + CANON_FILENAME));
                    inputPath.setText(directory.getPath());
                }
                else {
                    directorySelector.cancelSelection();
                }
            }
        }
    }

    static class GenerateHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == generateButton) {
                //var field = (JTextField) event.getSource();
                emailResult = inputEmail.getText();
                if (emailResult.isEmpty() || !emailResult.matches(EMAIL_REGEX)) {
                    inputEmail.setText("Помилка імейлу");
                }
                nameResult = inputName.getText();
                if (nameResult.isEmpty()){
                    inputName.setText("Помилка імені");
                }
                else {
                    contactMap.put(emailResult, "John Doe");
                    try {
                        generateAddressBook(contactMap);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static void generateAddressBook(Map<String, String> contactMap) throws IOException {
        //var emailList = readFile("src/main/resources/email.list");
        var header = readFile("src/main/resources/template.header");
        var template = readFile("src/main/resources/addressbook.template");
        //var outputPath = Paths.get("src/main/resources/adrs_book_CANON_1440.abk"); //todo implem customer path
        if (outputPath == null) {
            throw new IllegalArgumentException("PATH cannot be null");
        }
        try (var writer = Files.newBufferedWriter(outputPath)) {
            for (String line : header) {
                writer.write(line);
                writer.newLine();
            }
            var dnCounter = 1;
            for (Map.Entry<String, String> map : contactMap.entrySet()) {
                /*var parts = emailString.split("\\s+", 2);
                if (parts.length < 2) {
                    System.out.println("Введено неповну інфо, ігнорую рядок " + emailString);
                    continue;
                }
                var email = parts[0];
                var name = parts[1];*/
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

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return Pattern
                .compile(EMAIL_REGEX)
                .matcher(email)
                .matches();
    }

}
