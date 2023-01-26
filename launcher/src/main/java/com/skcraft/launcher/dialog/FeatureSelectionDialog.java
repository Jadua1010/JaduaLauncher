/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import com.skcraft.launcher.model.modpack.Feature;
import com.skcraft.launcher.swing.*;
import com.skcraft.launcher.util.SharedLocale;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.BorderFactory.createEmptyBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class FeatureSelectionDialog extends JDialog {

    private final List<Feature> features;
    private final JPanel container = new JPanel(new BorderLayout());
    private final JTextArea descText = new JTextArea(SharedLocale.tr("features.selectForInfo"));
    private final JScrollPane descScroll = new JScrollPane(descText);
    private final CheckboxTable componentsIndependent = new CheckboxTable();
    private final JScrollPane componentsScrollIndep = new JScrollPane(componentsIndependent);
    private final CheckboxTable componentsTable = new CheckboxTable();
    private final JScrollPane componentsScroll = new JScrollPane(componentsTable);
    private final JSplitPane splitPaneSmall = new JSplitPane(JSplitPane.VERTICAL_SPLIT, componentsScrollIndep, componentsScroll );
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPaneSmall, descScroll);
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
    private final JButton installButton = new JButton(SharedLocale.tr("features.install"));
    List<Integer> independantFeaturesIndex = new ArrayList<>();
    List<Integer> nonIndependantFeaturesIndex = new ArrayList<>();
    List<Feature> independantFeatures = new ArrayList<>();
    List<Feature> nonIndependantFeatures = new ArrayList<>();
    boolean isUpdating = false;
    private final Object receiver;

    public FeatureSelectionDialog(Window owner, @NonNull List<Feature> features, Object receiver) {
        super(owner, ModalityType.DOCUMENT_MODAL);

        this.features = features;
        this.receiver = receiver;
        
        setTitle(SharedLocale.tr("features.title"));
        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(500, 400));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public void initComponents() {
        for (Feature feature : features) {
            if (feature.isIndependent()) {
                independantFeatures.add(feature); 
            } else {
                nonIndependantFeatures.add(feature);
            }
        }

        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            if (feature.isIndependent()) {
                independantFeaturesIndex.add(i);
            } else {
                nonIndependantFeaturesIndex.add(i);
            }
        }

        componentsTable.setModel(new FeatureTableModel(nonIndependantFeatures));
        componentsIndependent.setModel(new FeatureTableModel(independantFeatures));
        TableColumnModel columnModel = componentsIndependent.getColumnModel();
        TableColumn column = columnModel.getColumn(1);
        column.setHeaderValue(SharedLocale.tr("features.independentColumn"));

        descScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        descText.setFont(new JLabel().getFont());
        descText.setEditable(false);
        descText.setWrapStyleWord(true);
        descText.setLineWrap(true);
        SwingHelper.removeOpaqueness(descText);
        descText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);

        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(6);
        SwingHelper.flattenJSplitPane(splitPane);

        splitPaneSmall.setDividerLocation(100);
        splitPaneSmall.setDividerSize(6);
        SwingHelper.flattenJSplitPane(splitPaneSmall);
        
        container.setBorder(createEmptyBorder(12, 12, 12, 12));
        container.add(splitPane, BorderLayout.CENTER);

        buttonsPanel.addGlue();
        buttonsPanel.addElement(installButton);

        JLabel descLabel = new JLabel(SharedLocale.tr("features.intro"));
        descLabel.setBorder(createEmptyBorder(12, 12, 4, 12));

        SwingHelper.equalWidth(installButton, new JButton(SharedLocale.tr("button.cancel")));

        add(descLabel, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        componentsTable.setOpaque(false);
        
        componentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateDescription();
            }
        });
        
        componentsIndependent.setOpaque(false);
        
        componentsIndependent.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!isUpdating) {
                    isUpdating = true;
                    int selectedRow = componentsIndependent.getSelectedRow();
                    if (selectedRow != -1) {
                        for (int i = 0; i < componentsIndependent.getRowCount(); i++) {
                            if ((Boolean) componentsIndependent.getValueAt(i, 0) == true) {
                                int originalIndex = independantFeaturesIndex.get(i);
                                Feature f = features.get(originalIndex);
                                f.setSelected(false);
                                componentsIndependent.setValueAt(false, i, 0);
                            }
                        }
                    }
                    int originalIndex = independantFeaturesIndex.get(selectedRow);
                    Feature f = features.get(originalIndex);
                    f.setSelected(true);
                    componentsIndependent.setValueAt(true, selectedRow, 0);
                    updateIndependentDescription();
                    componentsIndependent.repaint();
                    isUpdating = false;
                }
            }
        });

        
        installButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (receiver) {
                    receiver.notifyAll();
                }
                FeatureSelectionDialog.this.dispose();
            }
        });
    }

    private void updateDescription() {
        int selectedRow = componentsTable.getSelectedRow();
        // check which list the selected row belongs to

        int originalIndex = nonIndependantFeaturesIndex.get(selectedRow);

        if (features.get(originalIndex) != null) {
            descText.setText(features.get(originalIndex).getDescription());
        } else {
            descText.setText(SharedLocale.tr("features.selectForInfo"));
        }
    }
    
    private void updateIndependentDescription() {
        int selectedRow = componentsIndependent.getSelectedRow();
        // check which list the selected row belongs to

        int originalIndex = independantFeaturesIndex.get(selectedRow);

        if (features.get(originalIndex) != null) {
            descText.setText(features.get(originalIndex).getDescription());
        } else {
            descText.setText(SharedLocale.tr("features.selectForInfo"));
        }
    }

}
