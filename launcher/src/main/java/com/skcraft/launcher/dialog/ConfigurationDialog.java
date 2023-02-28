/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.dialog.component.BetterComboBox;
import com.skcraft.launcher.launch.runtime.AddJavaRuntime;
import com.skcraft.launcher.launch.runtime.JavaRuntime;
import com.skcraft.launcher.launch.runtime.JavaRuntimeFinder;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.popups.Notification;
import com.skcraft.launcher.swing.*;
import com.skcraft.launcher.update.UpdateManager;
import com.skcraft.launcher.util.SharedLocale;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A dialog to modify configuration options.
 */
public class ConfigurationDialog extends JDialog {

    private final Configuration config;
    private final ObjectSwingMapper mapper;

    private final JPanel tabContainer = new JPanel(new BorderLayout());
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final FormPanel javaSettingsPanel = new FormPanel();
    private final JComboBox<JavaRuntime> jvmRuntime = new JComboBox<>();
    private final JTextField jvmArgsText = new JTextField();
    private final JSpinner minMemorySpinner = new JSpinner();
    private final JSpinner maxMemorySpinner = new JSpinner();
    private final JSpinner permGenSpinner = new JSpinner();
    private final FormPanel gameSettingsPanel = new FormPanel();
    private final JSpinner widthSpinner = new JSpinner();
    private final JSpinner heightSpinner = new JSpinner();
    private final JCheckBox openConsole = new JCheckBox(SharedLocale.tr("options.openConsole"));
    private final FormPanel proxySettingsPanel = new FormPanel();
    private final JCheckBox useProxyCheck = new JCheckBox(SharedLocale.tr("options.useProxyCheck"));
    private final JTextField proxyHostText = new JTextField();
    private final JSpinner proxyPortText = new JSpinner();
    private final JTextField proxyUsernameText = new JTextField();
    private final JPasswordField proxyPasswordText = new JPasswordField();
    private final FormPanel advancedPanel = new FormPanel();
    private final JTextField gameKeyText = new JTextField();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
    private final JButton okButton = new JButton(SharedLocale.tr("button.ok"));
    private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));
    private final JButton aboutButton = new JButton(SharedLocale.tr("options.about"));
    private final JButton logButton = new JButton(SharedLocale.tr("options.launcherConsole"));
    private final JCheckBox lightMode = new JCheckBox(SharedLocale.tr("options.lightMode"));

    /**
     * Create a new configuration dialog.
     *
     * @param owner the window owner
     * @param launcher the launcher
     * @param window the window itself
     */
    public ConfigurationDialog(Window owner, @NonNull Launcher launcher, LauncherFrame window) {
        super(owner, ModalityType.DOCUMENT_MODAL);

        this.config = launcher.getConfig();
        mapper = new ObjectSwingMapper(config);

        JavaRuntime[] javaRuntimes = JavaRuntimeFinder.getAvailableRuntimes().toArray(new JavaRuntime[0]);
        DefaultComboBoxModel<JavaRuntime> model = new DefaultComboBoxModel<>(javaRuntimes);

        // Put the runtime from the config in the model if it isn't
        boolean configRuntimeFound = Arrays.stream(javaRuntimes).anyMatch(r -> r.equals(config.getJavaRuntime()));
        if (!configRuntimeFound && config.getJavaRuntime() != null) {
            model.insertElementAt(config.getJavaRuntime(), 0);
        }

        jvmRuntime.setModel(model);
        jvmRuntime.addItem(AddJavaRuntime.ADD_RUNTIME_SENTINEL);

        jvmRuntime.setSelectedItem(config.getJavaRuntime());

        setTitle(SharedLocale.tr("options.title"));
        initComponents(); // Must be called after jvmRuntime model setup
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 500));
        setResizable(false);
        setLocationRelativeTo(owner);

        mapper.map(jvmArgsText, "jvmArgs");
        mapper.map(minMemorySpinner, "minMemory");
        mapper.map(maxMemorySpinner, "maxMemory");
        mapper.map(permGenSpinner, "permGen");
        mapper.map(widthSpinner, "windowWidth");
        mapper.map(heightSpinner, "windowHeight");
        mapper.map(openConsole, "consoleEnabled");
        mapper.map(useProxyCheck, "proxyEnabled");
        mapper.map(proxyHostText, "proxyHost");
        mapper.map(proxyPortText, "proxyPort");
        mapper.map(proxyUsernameText, "proxyUsername");
        mapper.map(proxyPasswordText, "proxyPassword");
        mapper.map(gameKeyText, "gameKey");
        mapper.map(lightMode, "lightModeEnabled");

        mapper.copyFromObject();
    }

    private void initComponents() {
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.jvmPath")), jvmRuntime);
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.jvmArguments")), jvmArgsText);
        javaSettingsPanel.addRow(Box.createVerticalStrut(15));
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.64BitJavaWarning")));
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.minMemory")), minMemorySpinner);
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.maxMemory")), maxMemorySpinner);
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.permGen")), permGenSpinner);
        SwingHelper.removeOpaqueness(javaSettingsPanel);
        tabbedPane.addTab(SharedLocale.tr("options.javaTab"), SwingHelper.alignTabbedPane(javaSettingsPanel));

        gameSettingsPanel.addRow(openConsole);
        gameSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.windowWidth")), widthSpinner);
        gameSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.windowHeight")), heightSpinner);
        SwingHelper.removeOpaqueness(gameSettingsPanel);
        tabbedPane.addTab(SharedLocale.tr("options.minecraftTab"), SwingHelper.alignTabbedPane(gameSettingsPanel));

        proxySettingsPanel.addRow(useProxyCheck);
        proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyHost")), proxyHostText);
        proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyPort")), proxyPortText);
        proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyUsername")), proxyUsernameText);
        proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyPassword")), proxyPasswordText);
        SwingHelper.removeOpaqueness(proxySettingsPanel);
        tabbedPane.addTab(SharedLocale.tr("options.proxyTab"), SwingHelper.alignTabbedPane(proxySettingsPanel));

        advancedPanel.addRow(new JLabel(SharedLocale.tr("options.gameKey")), gameKeyText);
        advancedPanel.addRow(Box.createVerticalStrut(15));
        advancedPanel.addRow(new JLabel(SharedLocale.tr("options.theming")));
        advancedPanel.addRow(lightMode);
        SwingHelper.removeOpaqueness(advancedPanel);
        tabbedPane.addTab(SharedLocale.tr("options.advancedTab"), SwingHelper.alignTabbedPane(advancedPanel));

        buttonsPanel.addElement(logButton);
        buttonsPanel.addElement(aboutButton);
        buttonsPanel.addGlue();
        buttonsPanel.addElement(okButton);
        buttonsPanel.addElement(cancelButton);

        tabContainer.add(tabbedPane, BorderLayout.CENTER);
        tabContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(tabContainer, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        SwingHelper.equalWidth(okButton, cancelButton);

        cancelButton.addActionListener(ActionListeners.dispose(this));

        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog.showAboutDialog(ConfigurationDialog.this);
            }
        });

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Notification panel;
                LauncherFrame window = LauncherFrame.currentInstance;
                try {
                    panel = new Notification(window, Notification.Type.SUCCESS, Notification.Location.TOP_CENTER, "Settings have been saved");
                    panel.showNotification();
                } catch (InterruptedException ex) {
                    Logger.getLogger(LauncherFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                save();
            }
        });

        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConsoleFrame.showMessages();
            }
        });
        
        lightMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int response = JOptionPane.showConfirmDialog(null, SharedLocale.tr("options.needsRestart"), SharedLocale.tr("options.needsRestartTitle"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    switch (response) {
                        case JOptionPane.NO_OPTION:
                            break;
                        case JOptionPane.YES_OPTION:
                            save();
                            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                            File currentJar = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                            
                            /* is it a jar file? */
                            if(!currentJar.getName().endsWith(".jar")) {
                                return;
                            }
                            
                            /* Build command: java -jar application.jar */
                            StringBuilder cmd = new StringBuilder();
                            cmd.append("\"").append(javaBin).append("\" ");
                            cmd.append("-jar ");
                            cmd.append("\"").append(currentJar.getPath()).append("\" ");
                            
                            Runtime.getRuntime().exec(cmd.toString());
                            System.exit(0);
                            break;
                        case JOptionPane.CLOSED_OPTION:
                            break;
                        default:
                            break;
                    }   } catch (URISyntaxException | IOException ex) {
                    Logger.getLogger(ConfigurationDialog.class.getName()).log(Level.SEVERE, "Failed To Restart", ex);
                }
            }
        });

        jvmRuntime.addActionListener(e -> {
            // A little fun hack...
            if (jvmRuntime.getSelectedItem() == AddJavaRuntime.ADD_RUNTIME_SENTINEL) {
                jvmRuntime.setSelectedItem(null);
                jvmRuntime.setPopupVisible(false);

                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setFileFilter(new JavaRuntimeFileFilter());
                chooser.setDialogTitle("Choose a Java executable");

                int result = chooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    JavaRuntime runtime = JavaRuntimeFinder.getRuntimeFromPath(chooser.getSelectedFile().getAbsolutePath());

                    MutableComboBoxModel<JavaRuntime> model = (MutableComboBoxModel<JavaRuntime>) jvmRuntime.getModel();
                    model.insertElementAt(runtime, 0);
                    jvmRuntime.setSelectedItem(runtime);
                }
            }
        });
    }

    /**
     * Save the configuration and close the dialog.
     */
    public void save() {
        mapper.copyFromSwing();
        config.setJavaRuntime((JavaRuntime) jvmRuntime.getSelectedItem());

        Persistence.commitAndForget(config);
        dispose();
    }

    static class JavaRuntimeFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().startsWith("java") && f.canExecute();
        }

        @Override
        public String getDescription() {
            return "Java runtime executables";
        }
    }
}