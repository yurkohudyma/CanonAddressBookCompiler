package ua.hudyma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class MainOptimal {

    private static final String DN = "dn:";
    private static final String CN = "cn::";
    private static final String MAIL_ADDRESS = "mailaddress:";
    public static final String SPACE = " ";

    public static void main(String[] args) throws IOException {
        var emailList = readFile("src/main/resources/email.list");
        var header = readFile("src/main/resources/template.header");
        var template = readFile("src/main/resources/addressbook.template");
        var outputPath = Paths.get("src/main/resources/adrs_book_CANON_1440.abk");
        try (var writer = Files.newBufferedWriter(outputPath)) {
            for (String line : header) {
                writer.write(line);
                writer.newLine();
            }
            var dnCounter = 1;
            for (String emailString : emailList) {
                var parts = emailString.split("\\s+", 2);
                if (parts.length < 2) {
                    System.out.println("Введено неповну інфо, ігнорую рядок " + emailString);
                    continue;
                }
                var email = parts[0];
                var name = parts[1];
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
