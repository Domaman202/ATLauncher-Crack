/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher;

import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.DownloadableFile;
import com.atlauncher.data.LauncherVersion;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.NewsTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.gui.tabs.ServersTab;
import com.atlauncher.gui.tabs.VanillaPacksTab;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.CheckingServersManager;
import com.atlauncher.managers.CurseForgeUpdateManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.managers.ModpacksChUpdateManager;
import com.atlauncher.managers.NewsManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.mini2Dx.gettext.GetText;

import net.arikia.dev.drpc.DiscordRPC;
import okhttp3.OkHttpClient;

public class Launcher {
    // Holding update data
    private LauncherVersion latestLauncherVersion; // Latest Launcher version
    private List<DownloadableFile> launcherFiles; // Files the Launcher needs to download

    // UI things
    private JFrame parent; // Parent JFrame of the actual Launcher
    private InstancesTab instancesPanel; // The instances panel
    private ServersTab serversPanel; // The instances panel
    private NewsTab newsPanel; // The news panel
    private VanillaPacksTab vanillaPacksPanel; // The vanilla packs panel
    private PacksTab featuredPacksPanel; // The featured packs panel
    private PacksTab packsPanel; // The packs panel

    // Update thread
    private Thread updateThread;

    // Minecraft tracking variables
    private Process minecraftProcess = null; // The process minecraft is running on
    public boolean minecraftLaunched = false; // If Minecraft has been Launched

    public void checkIfWeCanLoad() {
        if (!Java.isUsingJavaSupportingLetsEncrypt()) {
            LogManager.warn("You're using an old version of Java that will not work!");

            DialogManager.optionDialog().setTitle(GetText.tr("Unsupported Java Version"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "You're using an unsupported version of Java. You need to upgrade your Java to at minimum Java 8 version 141.<br/><br/>The launcher will not start until you do this.<br/><br/>If you're seeing this message even after installing a newer version, you may need to uninstall the old version first.<br/><br/>Click ok to open the Java download page and close the launcher."))
                            .build())
                    .addOption(GetText.tr("Ok")).setType(DialogManager.ERROR).show();

            OS.openWebBrowser("https://atl.pw/java8download");
            System.exit(0);
        }
    }

    public void loadEverything() {
        PerformanceManager.start();

        addExecutableBitToTools();

        NewsManager.loadNews(); // Load the news

        MinecraftManager.loadMinecraftVersions(); // Load info about the different Minecraft versions

        // Load info about the different java runtimes
        App.TASKPOOL.execute(() -> {
            MinecraftManager.loadJavaRuntimes();
        });

        PackManager.loadPacks(); // Load the Packs available in the Launcher

        PackManager.loadUsers(); // Load the Testers and Allowed Players for the packs

        InstanceManager.loadInstances(); // Load the users installed Instances

        ServerManager.loadServers(); // Load the users installed servers

        AccountManager.loadAccounts(); // Load the saved Accounts

        CheckingServersManager.loadCheckingServers(); // Load the saved servers we're checking with the tool

        PackManager.removeUnusedImages(); // remove unused pack images

        if (OS.isWindows() && !OS.is64Bit() && OS.isWindows64Bit()) {
            LogManager.warn("You're using 32 bit Java on a 64 bit Windows install!");

            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Running 32 Bit Java on 64 Bit Windows"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "We have detected that you're running 64 bit Windows but not 64 bit Java.<br/><br/>This will cause severe issues playing all packs if not fixed.<br/><br/>Do you want to close the launcher and learn how to fix this issue now?"))
                            .build())
                    .setType(DialogManager.ERROR).show();

            if (ret == 0) {
                OS.openWebBrowser("https://atlauncher.com/help/32bit/");
                System.exit(0);
            }
        }

        if (Java.isMinecraftJavaNewerThanJava8() && !App.settings.hideJava9Warning) {
            LogManager.warn("You're using a newer version of Java than Java 8! Modpacks may not launch!");

            int ret = DialogManager.optionDialog()
                    .setTitle(GetText.tr("Warning! You may not be able to play Minecraft"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "You're using Java 9 or newer! Older modpacks may not work.<br/><br/>If you have issues playing some packs, you may need to install Java 8 and set it to be used in the launchers java settings"))
                            .build())
                    .addOption(GetText.tr("Download"), true).addOption(GetText.tr("Ok"))
                    .addOption(GetText.tr("Don't Remind Me Again")).setType(DialogManager.WARNING).show();

            if (ret == 0) {
                OS.openWebBrowser("https://atl.pw/java8download");
                System.exit(0);
            } else if (ret == 2) {
                App.settings.hideJava9Warning = true;
                App.settings.save();
            }
        }

        if (!Java.isJava7OrAbove(true) && !App.settings.hideOldJavaWarning) {
            LogManager.warn("You're using an old unsupported version of Java (Java 7 or older)!");

            int ret = DialogManager.optionDialog().setTitle(GetText.tr("Unsupported Java Version"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "You're using an unsupported version of Java. You should upgrade your Java to at minimum Java 7.<br/><br/>Without Java 7 some mods will refuse to load meaning you cannot play.<br/><br/>Click Download to go to the Java downloads page"))
                            .build())
                    .addOption(GetText.tr("Download"), true).addOption(GetText.tr("Ok"))
                    .addOption(GetText.tr("Don't Remind Me Again")).setType(DialogManager.WARNING).show();

            if (ret == 0) {
                OS.openWebBrowser("https://atl.pw/java8download");
                System.exit(0);
            } else if (ret == 2) {
                App.settings.hideOldJavaWarning = true;
                App.settings.save();
            }
        }

        if (App.settings.enableServerChecker) {
            CheckingServersManager.startCheckingServers();
        }

        checkForExternalPackUpdates();

        if (!App.settings.firstTimeRun && App.settings.enableLogs && App.settings.enableAnalytics) {
            Analytics.startSession();
        }
        PerformanceManager.end();
    }

    public void checkForExternalPackUpdates() {
        if (updateThread != null && updateThread.isAlive()) {
            updateThread.interrupt();
        }

        updateThread = new Thread(() -> {
            if (InstanceManager.getInstances().stream().anyMatch(i -> i.isModpacksChPack())) {
                ModpacksChUpdateManager.checkForUpdates();
            }
            if (InstanceManager.getInstances().stream().anyMatch(i -> i.isCurseForgePack())) {
                CurseForgeUpdateManager.checkForUpdates();
            }
        });
        updateThread.start();
    }

    public void updateData() {
        MinecraftManager.loadMinecraftVersions(); // Load info about the different Minecraft versions
        MinecraftManager.loadJavaRuntimes(); // Load info about the different java runtimes
    }

    public void reloadLauncherData() {
        final JDialog dialog = new JDialog(this.parent, ModalityType.DOCUMENT_MODAL);
        dialog.setSize(300, 100);
        dialog.setTitle("Updating Launcher");
        dialog.setLocationRelativeTo(App.launcher.getParent());
        dialog.setLayout(new FlowLayout());
        dialog.setResizable(false);
        dialog.add(new JLabel(GetText.tr("Updating Launcher. Please Wait")));
        App.TASKPOOL.execute(() -> {
            checkForExternalPackUpdates();
            addExecutableBitToTools();

            NewsManager.loadNews(); // Load the news
            reloadNewsPanel(); // Reload news panel
            PackManager.loadPacks(); // Load the Packs available in the Launcher
            reloadFeaturedPacksPanel(); // Reload packs panel
            reloadPacksPanel(); // Reload packs panel
            PackManager.loadUsers(); // Load the Testers and Allowed Players for the packs
            InstanceManager.loadInstances(); // Load the users installed Instances
            reloadInstancesPanel(); // Reload instances panel
            reloadServersPanel(); // Reload instances panel
            dialog.setVisible(false); // Remove the dialog
            dialog.dispose(); // Dispose the dialog
        });
        dialog.setVisible(true);
    }

    private void addExecutableBitToTools() {
        PerformanceManager.start();
        File[] files = FileSystem.TOOLS.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.canExecute()) {
                    LogManager.info("Executable bit being set on " + file.getName());
                    file.setExecutable(true);
                }
            }
        }
        PerformanceManager.end();
    }

    /**
     * Sets the main parent JFrame reference for the Launcher
     *
     * @param parent The Launcher main JFrame
     */
    public void setParentFrame(JFrame parent) {
        this.parent = parent;
    }

    public void setMinecraftLaunched(boolean launched) {
        this.minecraftLaunched = launched;
        App.TRAY_MENU.setMinecraftLaunched(launched);
    }

    /**
     * Returns the JFrame reference of the main Launcher
     *
     * @return Main JFrame of the Launcher
     */
    public Window getParent() {
        return this.parent;
    }

    /**
     * Sets the panel used for Instances
     *
     * @param instancesPanel Instances Panel
     */
    public void setInstancesPanel(InstancesTab instancesPanel) {
        this.instancesPanel = instancesPanel;
    }

    /**
     * Reloads the panel used for Instances
     */
    public void reloadInstancesPanel() {
        if (instancesPanel != null) {
            this.instancesPanel.reload(); // Reload the instances panel
        }
    }

    public void setServersPanel(ServersTab serversPanel) {
        this.serversPanel = serversPanel;
    }

    public void reloadServersPanel() {
        if (serversPanel != null) {
            this.serversPanel.reload(); // Reload the servers panel
        }
    }

    /**
     * Sets the panel used for Vanilla Packs
     *
     * @param vanillaPacksPanel Vanilla Packs Panel
     */
    public void setVanillaPacksPanel(VanillaPacksTab vanillaPacksPanel) {
        this.vanillaPacksPanel = vanillaPacksPanel;
    }

    /**
     * Sets the panel used for Featured Packs
     *
     * @param featuredPacksPanel Featured Packs Panel
     */
    public void setFeaturedPacksPanel(PacksTab featuredPacksPanel) {
        this.featuredPacksPanel = featuredPacksPanel;
    }

    /**
     * Sets the panel used for Packs
     *
     * @param packsPanel Packs Panel
     */
    public void setPacksPanel(PacksTab packsPanel) {
        this.packsPanel = packsPanel;
    }

    /**
     * Sets the panel used for News
     *
     * @param newsPanel News Panel
     */
    public void setNewsPanel(NewsTab newsPanel) {
        this.newsPanel = newsPanel;
    }

    /**
     * Reloads the panel used for News
     */
    public void reloadNewsPanel() {
        this.newsPanel.reload(); // Reload the news panel
    }

    /**
     * Reloads the panel used for Featured Packs
     */
    public void reloadFeaturedPacksPanel() {
        this.featuredPacksPanel.reload();
    }

    /**
     * Refreshes the panel used for Featured Packs
     */
    public void refreshFeaturedPacksPanel() {
        this.featuredPacksPanel.refresh();
    }

    /**
     * Reloads the panel used for Packs
     */
    public void reloadPacksPanel() {
        this.packsPanel.reload(); // Reload the instances panel
    }

    /**
     * Refreshes the panel used for Packs
     */
    public void refreshPacksPanel() {
        this.packsPanel.refresh(); // Refresh the instances panel
    }

    public void showKillMinecraft(Process minecraft) {
        this.minecraftProcess = minecraft;
        App.console.showKillMinecraft();
    }

    public void hideKillMinecraft() {
        App.console.hideKillMinecraft();
    }

    public void killMinecraft() {
        if (this.minecraftProcess != null) {
            LogManager.error("Killing Minecraft");

            if (App.settings.enableDiscordIntegration && App.discordInitialized) {
                DiscordRPC.discordClearPresence();
            }

            this.minecraftProcess.destroy();
            this.minecraftProcess = null;
        } else {
            LogManager.error("Cannot kill Minecraft as there is no instance open!");
        }
    }
}
