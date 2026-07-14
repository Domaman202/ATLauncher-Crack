package ru.DmN.atlcrack2;

import com.atlauncher.App;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.managers.AccountManager;
import sun.misc.Unsafe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;

public final class UsernameDialog extends JDialog {
    private final JTextField textField;
    private boolean okClicked = false;

    private UsernameDialog() {
        setModalityType(ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!okClicked) {
                    System.exit(0);
                }
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Enter Username:");
        textField = new JTextField(20);
        mainPanel.add(label, BorderLayout.NORTH);
        mainPanel.add(textField, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            String username = textField.getText();
            if (username.length() > 3) {
                okClicked = true;
                dispose();
                FakeAccount account = new FakeAccount(username);
                App.settings.lastAccount = account.username;
                try {
                    Field unsafeF = Unsafe.class.getDeclaredField("theUnsafe");
                    unsafeF.setAccessible(true);
                    Unsafe unsafe = (Unsafe) unsafeF.get(null);
                    unsafe.putInt(account, 8L, unsafe.getInt(unsafe.allocateInstance(MicrosoftAccount.class), 8L));
                } catch (NoSuchFieldException | IllegalAccessException | InstantiationException ex) {
                    throw new RuntimeException(ex);
                }
                AccountManager.addAccount((MicrosoftAccount) (Object) account);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Username must be at least 4 characters",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void showDialog() {
        new UsernameDialog();
    }
}