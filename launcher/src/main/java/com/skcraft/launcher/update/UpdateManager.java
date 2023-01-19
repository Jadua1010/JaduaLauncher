/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.update;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.selfupdate.LatestVersionInfo;
import com.skcraft.launcher.selfupdate.SelfUpdater;
import com.skcraft.launcher.selfupdate.UpdateChecker;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateManager {

    @Getter
    private final SwingPropertyChangeSupport propertySupport = new SwingPropertyChangeSupport(this);
    private final Launcher launcher;
    private LatestVersionInfo pendingUpdate;

    public UpdateManager(Launcher launcher) {
        this.launcher = launcher;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public boolean getPendingUpdate() {
        return pendingUpdate != null;
    }

    public void checkForUpdate() {
        ListenableFuture<LatestVersionInfo> future = launcher.getExecutor().submit(new UpdateChecker(launcher));

        Futures.addCallback(future, new FutureCallback<LatestVersionInfo>() {
            @Override
            public void onSuccess(LatestVersionInfo result) {
                if (result != null) {
                    requestUpdate(result);
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, SwingExecutor.INSTANCE);
    }

    public void performUpdate(final Window window) {
        final URL url = pendingUpdate.getUrl();

        if (url != null) {
            SelfUpdater downloader = new SelfUpdater(launcher, url);
            ObservableFuture<File> future = new ObservableFuture<File>(
                    launcher.getExecutor().submit(downloader), downloader);

            Futures.addCallback(future, new FutureCallback<File>() {
                @Override
                public void onSuccess(File result) {
                    propertySupport.firePropertyChange("pendingUpdate", true, false);
                    UpdateManager.this.pendingUpdate = null;

                    int response = JOptionPane.showConfirmDialog(null, SharedLocale.tr("launcher.selfUpdateComplete"), SharedLocale.tr("launcher.selfUpdateCompleteTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    switch (response) {
                        case JOptionPane.NO_OPTION:
                            break;
                        case JOptionPane.YES_OPTION:
                            {
                            try {
                                final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw";
                                final File currentJar = future.get();
                                
                                /* is it a jar file? */
                                if(!currentJar.getName().endsWith(".jar"))
                                    return;
                                
                                /* Build command: java -jar application.jar */
                                final ArrayList<String> command = new ArrayList<String>();
                                command.add(javaBin);
                                command.add("-jar");
                                command.add(currentJar.getPath());
                                
                                final ProcessBuilder builder = new ProcessBuilder(command);
                                builder.start();
                                System.exit(0);
                            } catch (IOException | InterruptedException | ExecutionException ex) {
                                Logger.getLogger(UpdateManager.class.getName()).log(Level.SEVERE, "Cannot process restart", ex);
                            }
                            }
                            break;
                        case JOptionPane.CLOSED_OPTION:
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                }
            }, SwingExecutor.INSTANCE);

            ProgressDialog.showProgress(window, future, SharedLocale.tr("launcher.selfUpdatingTitle"), SharedLocale.tr("launcher.selfUpdatingStatus"));
            SwingHelper.addErrorDialogCallback(window, future);
        } else {
            propertySupport.firePropertyChange("pendingUpdate", false, false);
        }
    }

    private void requestUpdate(LatestVersionInfo url) {
        propertySupport.firePropertyChange("pendingUpdate", getPendingUpdate(), url != null);
        this.pendingUpdate = url;
    }


}
