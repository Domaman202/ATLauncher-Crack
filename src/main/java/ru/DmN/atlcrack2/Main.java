package ru.DmN.atlcrack2;

import com.atlauncher.App;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        App.workingDir = new File("ATL").toPath();
        App.main(args);
        UsernameDialog.showDialog();
    }
}
