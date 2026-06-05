package ua.hudyma;

import javax.swing.*;

public class AddressBookProcessor {

    public static void main(String[] args) {
        var controller = new AddressBookController("Canon 1440 Addressbook Compiler v.1.0 by Hudyma");
        controller.setVisible(true);
        controller.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        controller.setSize(425, 120);
        controller.setResizable(true);
        controller.setLocationRelativeTo(null);
    }
}
