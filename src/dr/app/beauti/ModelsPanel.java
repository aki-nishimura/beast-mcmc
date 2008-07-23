/*
 * ModelPanel.java
 *
 * Copyright (C) 2002-2006 Alexei Drummond and Andrew Rambaut
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package dr.app.beauti;

import dr.evolution.datatype.DataType;
import dr.app.beauti.options.BeautiOptions;
import dr.app.beauti.options.PartitionModel;
import org.virion.jam.components.RealNumberField;
import org.virion.jam.framework.Exportable;
import org.virion.jam.panels.OptionsPanel;
import org.virion.jam.table.HeaderRenderer;
import org.virion.jam.table.TableEditorStopper;
import org.virion.jam.util.IconUtils;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author			Andrew Rambaut
 * @author			Alexei Drummond
 * @version			$Id: ModelPanel.java,v 1.17 2006/09/05 13:29:34 rambaut Exp $
 */
public class ModelsPanel extends JPanel implements Exportable {

    private static final long serialVersionUID = 2778103564318492601L;

    JScrollPane scrollPane = new JScrollPane();
    JTable modelTable = null;
    ModelTableModel modelTableModel = null;
    BeautiOptions options = null;
    PartitionModel currentModel = null;

    OptionsPanel modelPanel;
    JComboBox nucSubstCombo = new JComboBox(new String[] {"HKY", "GTR"});
    JComboBox aaSubstCombo = new JComboBox(new String[] {"Blosum62", "Dayhoff", "JTT", "mtREV", "cpREV", "WAG"});
    JComboBox binarySubstCombo = new JComboBox(new String[] {"Simple", "Covarion"});

    JComboBox frequencyCombo =new JComboBox(new String[] {"Estimated", "Empirical", "All equal"});

    JComboBox heteroCombo = new JComboBox(new String[] {"None", "Gamma", "Invariant Sites", "Gamma + Invariant Sites"});

    JComboBox gammaCatCombo = new JComboBox(new String[] {"4", "5", "6", "7", "8", "9", "10"});
    JLabel gammaCatLabel;

    JComboBox codingCombo = new JComboBox(new String[] {
            "Off",
            "2 partitions: codon positions (1 + 2), 3",
            "3 partitions: codon positions 1, 2, 3"});
    JCheckBox substUnlinkCheck = new JCheckBox("Unlink substitution model across codon positions");
    JCheckBox heteroUnlinkCheck = new JCheckBox("Unlink rate heterogeneity model across codon positions");
    JCheckBox freqsUnlinkCheck = new JCheckBox("Unlink base frequencies across codon positions");

    JButton setSRD06Button;

    JCheckBox fixedSubstitutionRateCheck = new JCheckBox("Fix mean substitution rate:");
    JLabel substitutionRateLabel = new JLabel("Mean substitution rate:");
    RealNumberField substitutionRateField = new RealNumberField(Double.MIN_VALUE, Double.POSITIVE_INFINITY);

    JComboBox clockModelCombo = new JComboBox(new String[] {
            "Strict Clock",
            "Random Local Clock",
            "Relaxed Clock: Uncorrelated Lognormal",
            "Relaxed Clock: Uncorrelated Exponential" } );

    BeautiFrame frame = null;

    boolean warningShown = false;
    boolean hasSetFixedSubstitutionRate = false;

    boolean settingOptions = false;

    boolean hasAlignment = false;

    int dataType = DataType.NUCLEOTIDES;

    public ModelsPanel(BeautiFrame parent) {

        super();

        this.frame = parent;

        modelTableModel = new ModelTableModel();
        modelTable = new JTable(modelTableModel);

        modelTable.getTableHeader().setReorderingAllowed(false);
        modelTable.getTableHeader().setDefaultRenderer(
                new HeaderRenderer(SwingConstants.LEFT, new Insets(0, 4, 0, 4)));

        TableEditorStopper.ensureEditingStopWhenTableLosesFocus(modelTable);

        modelTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                selectionChanged();
            }
        });

        scrollPane = new JScrollPane(modelTable,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setOpaque(false);

        Icon addIcon = null, removeIcon = null;
        try {
            addIcon = new ImageIcon(IconUtils.getImage(this.getClass(), "images/add.png"));
            removeIcon = new ImageIcon(IconUtils.getImage(this.getClass(), "images/remove.png"));
        } catch (Exception e) {
            // do nothing
        }
        JPanel buttonPanel = createAddRemoveButtonPanel(addModelAction, addIcon, "Create a new model",
                removeModelAction, removeIcon, "Delete the selected model",
                javax.swing.BoxLayout.Y_AXIS);

        modelPanel = new OptionsPanel(12, 18);
        modelPanel.setOpaque(true);

        setupComponent(substUnlinkCheck);
        substUnlinkCheck.setEnabled(false);
        substUnlinkCheck.setToolTipText("" +
                "<html>Gives each codon position partition different<br>" +
                "substitution model parameters.</html>");

        setupComponent(heteroUnlinkCheck);
        heteroUnlinkCheck.setEnabled(false);
        heteroUnlinkCheck.setToolTipText("<html>Gives each codon position partition different<br>rate heterogeneity model parameters.</html>");

        setupComponent(freqsUnlinkCheck);
        freqsUnlinkCheck.setEnabled(false);
        freqsUnlinkCheck.setToolTipText("<html>Gives each codon position partition different<br>nucleotide frequency parameters.</html>");

        java.awt.event.ItemListener listener = new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent ev) {
                frame.modelChanged();
            }
        };

        setupComponent(nucSubstCombo);
        nucSubstCombo.addItemListener(listener);
        nucSubstCombo.setToolTipText("<html>Select the type of nucleotide substitution model.</html>");

        setupComponent(aaSubstCombo);
        aaSubstCombo.addItemListener(listener);
        aaSubstCombo.setToolTipText("<html>Select the type of amino acid substitution model.</html>");

        setupComponent(binarySubstCombo);
        binarySubstCombo.addItemListener(listener);
        binarySubstCombo.setToolTipText("<html>Select the type of binay substitution model.</html>");

        setupComponent(frequencyCombo);
        frequencyCombo.addItemListener(listener);
        frequencyCombo.setToolTipText("<html>Select the policy for determining the base frequencies.</html>");

        setupComponent(heteroCombo);
        heteroCombo.setToolTipText("<html>Select the type of site-specific rate<br>heterogeneity model.</html>");
        heteroCombo.addItemListener(
                new java.awt.event.ItemListener() {
                    public void itemStateChanged(java.awt.event.ItemEvent ev) {

                        frame.modelChanged();

                        if (heteroCombo.getSelectedIndex() == 1 || heteroCombo.getSelectedIndex() == 3) {
                            gammaCatLabel.setEnabled(true);
                            gammaCatCombo.setEnabled(true);
                        } else {
                            gammaCatLabel.setEnabled(false);
                            gammaCatCombo.setEnabled(false);
                        }
                    }
                }
        );

        setupComponent(gammaCatCombo);
        gammaCatCombo.setToolTipText("<html>Select the number of categories to use for<br>the discrete gamma rate heterogeneity model.</html>");
        gammaCatCombo.addItemListener(listener);

        setupComponent(codingCombo);
        codingCombo.setToolTipText("<html>Select how to partition the codon positions.</html>");
        codingCombo.addItemListener(
                new java.awt.event.ItemListener() {
                    public void itemStateChanged(java.awt.event.ItemEvent ev) {

                        frame.modelChanged();

                        if (codingCombo.getSelectedIndex() != 0) { // codon position partitioning
                            substUnlinkCheck.setEnabled(true);
                            heteroUnlinkCheck.setEnabled(true);
                            freqsUnlinkCheck.setEnabled(true);
                        } else {
                            substUnlinkCheck.setEnabled(false);
                            substUnlinkCheck.setSelected(false);
                            heteroUnlinkCheck.setEnabled(false);
                            heteroUnlinkCheck.setSelected(false);
                            freqsUnlinkCheck.setEnabled(false);
                            freqsUnlinkCheck.setSelected(false);
                        }
                    }
                }
        );

        substUnlinkCheck.addItemListener(listener);
        heteroUnlinkCheck.addItemListener(listener);
        freqsUnlinkCheck.addItemListener(listener);

        setSRD06Button = new JButton(setSRD06Action);
        setupComponent(setSRD06Button);
        setSRD06Button.setToolTipText("<html>Sets the SRD06 model as described in<br>" +
                "Shapiro, Rambaut & Drummond (2006) <i>MBE</i> <b>23</b>: 7-9.</html>");

        setupComponent(fixedSubstitutionRateCheck);
        fixedSubstitutionRateCheck.setToolTipText(
                "<html>Select this option to fix the substitution rate<br>" +
                        "rather than try to infer it. If this option is<br>" +
                        "turned off then either the sequences should have<br>" +
                        "dates or the tree should have sufficient calibration<br>" +
                        "informations specified as priors.</html>");
        fixedSubstitutionRateCheck.addItemListener(
                new java.awt.event.ItemListener() {
                    public void itemStateChanged(java.awt.event.ItemEvent ev) {
                        boolean fixed = fixedSubstitutionRateCheck.isSelected();
                        substitutionRateLabel.setEnabled(fixed);
                        substitutionRateField.setEnabled(fixed);
                        hasSetFixedSubstitutionRate = true;
                        frame.modelChanged();
                    }
                }
        );

        setupComponent(substitutionRateField);
        substitutionRateField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent ev) {
                frame.mcmcChanged();
            }});
        substitutionRateField.setToolTipText("<html>Enter the substitution rate here.</html>");

        setupComponent(clockModelCombo);
        clockModelCombo.setToolTipText("<html>Select either a strict molecular clock or<br>or a relaxed clock model.</html>");
        clockModelCombo.addItemListener(listener);

        currentModel = options.getPartitionModels().get(0);

        setupPanel(currentModel, modelPanel);

        OptionsPanel panel = new OptionsPanel(0, 0);
        panel.addComponentWithLabel("Molecular Clock Model:", clockModelCombo);

        setOpaque(false);
        setBorder(new BorderUIResource.EmptyBorderUIResource(new Insets(12, 12, 12, 12)));
        setLayout(new BorderLayout(0,0));
        add(scrollPane, BorderLayout.WEST);
        add(modelPanel, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
    }

    private JPanel createAddRemoveButtonPanel(Action addAction, Icon addIcon, String addToolTip,
                                              Action removeAction, Icon removeIcon, String removeToolTip, int axis) {

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, axis));
        buttonPanel.setOpaque(false);
        JButton addButton = new JButton(addAction);
        if (addIcon != null) {
            addButton.setIcon(addIcon);
            addButton.setText(null);
        }
        addButton.setToolTipText(addToolTip);
        addButton.putClientProperty("JButton.buttonType", "toolbar");
        addButton.setOpaque(false);
        addAction.setEnabled(false);

        JButton removeButton = new JButton(removeAction);
        if (removeIcon != null) {
            removeButton.setIcon(removeIcon);
            removeButton.setText(null);
        }
        removeButton.setToolTipText(removeToolTip);
        removeButton.putClientProperty("JButton.buttonType", "toolbar");
        removeButton.setOpaque(false);
        removeAction.setEnabled(false);

        buttonPanel.add(addButton);
        buttonPanel.add(new JToolBar.Separator(new Dimension(6,6)));
        buttonPanel.add(removeButton);

        return buttonPanel;
    }

    Action addModelAction = new AbstractAction("+") {
        private static final long serialVersionUID = 20273987098143413L;

        public void actionPerformed(ActionEvent ae) {
        }
    };

    Action removeModelAction = new AbstractAction("-") {
        public void actionPerformed(ActionEvent ae) {
        }
    };

   private void setupComponent(JComponent comp) {
        comp.setOpaque(false);

        //comp.setFont(UIManager.getFont("SmallSystemFont"));
        //comp.putClientProperty("JComponent.sizeVariant", "small");
        if (comp instanceof JButton) {
            comp.putClientProperty("JButton.buttonType", "roundRect");
        }
        if (comp instanceof JComboBox) {
            comp.putClientProperty("JComboBox.isSquare", Boolean.TRUE);
        }
    }

    private void setupPanel(PartitionModel model, OptionsPanel panel) {

        panel.removeAll();

        if (hasAlignment) {

            switch (dataType){
                case DataType.NUCLEOTIDES:
                    panel.addComponentWithLabel("Substitution Model:", nucSubstCombo, true);
                    panel.addComponentWithLabel("Base frequencies:", frequencyCombo);
                    panel.addComponentWithLabel("Site Heterogeneity Model:", heteroCombo);
                    gammaCatLabel = panel.addComponentWithLabel("Number of Gamma Categories:", gammaCatCombo);

                    panel.addSeparator();

                    JPanel panel1 = new JPanel(new BorderLayout(6,6));
                    panel1.setOpaque(false);
                    panel1.add(codingCombo, BorderLayout.CENTER);
                    panel1.add(setSRD06Button, BorderLayout.EAST);
                    panel.addComponentWithLabel("Partition into codon positions:", panel1);

                    panel1 = new JPanel();
                    panel1.setOpaque(false);
                    panel1.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                    panel1.setBorder(BorderFactory.createTitledBorder("Link/Unlink parameters:"));
                    panel1.add(substUnlinkCheck);
                    panel1.add(heteroUnlinkCheck);
                    panel1.add(freqsUnlinkCheck);

                    panel.addComponent(panel1);
                    break;

                case DataType.AMINO_ACIDS:
                    panel.addComponentWithLabel("Substitution Model:", aaSubstCombo);
                    panel.addComponentWithLabel("Site Heterogeneity Model:", heteroCombo);
                    gammaCatLabel = panel.addComponentWithLabel("Number of Gamma Categories:", gammaCatCombo);

                    break;

                case DataType.TWO_STATES:
                case DataType.COVARION:
                    panel.addComponentWithLabel("Substitution Model:", binarySubstCombo);
                    panel.addComponentWithLabel("Site Heterogeneity Model:", heteroCombo);
                    gammaCatLabel = panel.addComponentWithLabel("Number of Gamma Categories:", gammaCatCombo);

                    break;

                default:
                    throw new IllegalArgumentException("Unknown data type");

            }

            panel.addSeparator();

            //addComponent(fixedSubstitutionRateCheck);
            substitutionRateField.setColumns(10);
            panel.addComponents(fixedSubstitutionRateCheck, substitutionRateField);

            panel.addSeparator();
        }

        validate();
        repaint();
    }

    private void setSRD06Model() {
        nucSubstCombo.setSelectedIndex(0);
        heteroCombo.setSelectedIndex(1);
        codingCombo.setSelectedIndex(1);
        substUnlinkCheck.setSelected(true);
        heteroUnlinkCheck.setSelected(true);
    }

    public void setOptions(BeautiOptions options) {

        this.options = options;

        settingOptions = true;

        setModelOptions(currentModel);

        switch (options.clockModel) {
            case BeautiOptions.STRICT_CLOCK:
                clockModelCombo.setSelectedIndex(0); break;
            case BeautiOptions.RANDOM_LOCAL_CLOCK:
                clockModelCombo.setSelectedIndex(1); break;
            case BeautiOptions.UNCORRELATED_LOGNORMAL:
                clockModelCombo.setSelectedIndex(2); break;
            case BeautiOptions.UNCORRELATED_EXPONENTIAL:
                clockModelCombo.setSelectedIndex(3); break;
            default:
                throw new IllegalArgumentException("Unknown option for clock model");
        }

        settingOptions = false;

        modelTableModel.fireTableDataChanged();

        validate();
        repaint();
    }

    public void setModelOptions(PartitionModel model) {


            dataType = model.dataType.getType();
            switch(dataType){
                case DataType.NUCLEOTIDES:
                    if (model.nucSubstitutionModel == BeautiOptions.GTR) {
                        nucSubstCombo.setSelectedIndex(1);
                    } else {
                        nucSubstCombo.setSelectedIndex(0);
                    }

                    frequencyCombo.setSelectedIndex(model.frequencyPolicy);

                    break;

                case DataType.AMINO_ACIDS:
                    aaSubstCombo.setSelectedIndex(model.aaSubstitutionModel);
                    break;

                case DataType.TWO_STATES:
                case DataType.COVARION:
                    binarySubstCombo.setSelectedIndex(model.binarySubstitutionModel);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown data type");
            }

        if (model.gammaHetero && !model.invarHetero) {
            heteroCombo.setSelectedIndex(1);
        } else if (!model.gammaHetero && model.invarHetero) {
            heteroCombo.setSelectedIndex(2);
        } else if (model.gammaHetero && model.invarHetero) {
            heteroCombo.setSelectedIndex(3);
        } else {
            heteroCombo.setSelectedIndex(0);
        }

        gammaCatCombo.setSelectedIndex(model.gammaCategories - 4);

        if (model.codonHeteroPattern == null) {
            codingCombo.setSelectedIndex(0);
        } else if (model.codonHeteroPattern.equals("112")) {
            codingCombo.setSelectedIndex(1);
        } else {
            codingCombo.setSelectedIndex(2);
        }

        substUnlinkCheck.setSelected(model.unlinkedSubstitutionModel);
        heteroUnlinkCheck.setSelected(model.unlinkedHeterogeneityModel);
        freqsUnlinkCheck.setSelected(model.unlinkedFrequencyModel);

        hasSetFixedSubstitutionRate = model.hasSetFixedSubstitutionRate;
        if (!hasSetFixedSubstitutionRate) {
            if (model.maximumTipHeight > 0.0) {
                model.meanSubstitutionRate = 0.001;
                model.fixedSubstitutionRate = false;
            } else {
                model.meanSubstitutionRate = 1.0;
                model.fixedSubstitutionRate = true;
            }
        }

        fixedSubstitutionRateCheck.setSelected(model.fixedSubstitutionRate);
        substitutionRateField.setValue(model.meanSubstitutionRate);
        substitutionRateField.setEnabled(model.fixedSubstitutionRate);

        setupPanel(currentModel, modelPanel);
    }

    public void getOptions(BeautiOptions options) {

        // This prevents options be overwritten due to listeners calling
        // this function (indirectly through modelChanged()) whilst in the
        // middle of the setOptions() method.
        if (settingOptions) return;

        getOptions(currentModel);

        boolean fixed = fixedSubstitutionRateCheck.isSelected();
        if (!warningShown && !fixed && options.maximumTipHeight == 0.0) {
            JOptionPane.showMessageDialog(frame,
                    "You have chosen to sample substitution rates but all \n"+
                            "the sequences have the same date. In order for this to \n"+
                            "work, a strong prior is required on the substitution\n"+
                            "rate or the root of the tree.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            warningShown = true;
        }

        switch (clockModelCombo.getSelectedIndex()) {
            case 0:
                options.clockModel = BeautiOptions.STRICT_CLOCK; break;
            case 1:
                options.clockModel = BeautiOptions.RANDOM_LOCAL_CLOCK; break;
            case 2:
                options.clockModel = BeautiOptions.UNCORRELATED_LOGNORMAL; break;
            case 3:
                options.clockModel = BeautiOptions.UNCORRELATED_EXPONENTIAL; break;
            default:
                throw new IllegalArgumentException("Unknown option for clock model");
        }
    }


    public void getOptions(PartitionModel model) {

        if (nucSubstCombo.getSelectedIndex() == 1) {
            model.nucSubstitutionModel = BeautiOptions.GTR;
        } else {
            model.nucSubstitutionModel = BeautiOptions.HKY;
        }
        model.aaSubstitutionModel = aaSubstCombo.getSelectedIndex();

        model.binarySubstitutionModel = binarySubstCombo.getSelectedIndex();

        model.frequencyPolicy = frequencyCombo.getSelectedIndex();

        model.gammaHetero = heteroCombo.getSelectedIndex() == 1 || heteroCombo.getSelectedIndex() == 3;

        model.invarHetero = heteroCombo.getSelectedIndex() == 2 || heteroCombo.getSelectedIndex() == 3;

        model.gammaCategories = gammaCatCombo.getSelectedIndex() + 4;

        if (codingCombo.getSelectedIndex() == 0) {
            model.codonHeteroPattern = null;
        } else if (codingCombo.getSelectedIndex() == 1) {
            model.codonHeteroPattern = "112";
        } else {
            model.codonHeteroPattern = "123";
        }

        model.unlinkedSubstitutionModel = substUnlinkCheck.isSelected();
        model.unlinkedHeterogeneityModel = heteroUnlinkCheck.isSelected();
        model.unlinkedFrequencyModel = freqsUnlinkCheck.isSelected();

        model.hasSetFixedSubstitutionRate = hasSetFixedSubstitutionRate;
        model.fixedSubstitutionRate = fixedSubstitutionRateCheck.isSelected();
        model.meanSubstitutionRate = substitutionRateField.getValue();

        boolean fixed = fixedSubstitutionRateCheck.isSelected();
        if (!warningShown && !fixed && model.maximumTipHeight == 0.0) {
            JOptionPane.showMessageDialog(frame,
                    "You have chosen to sample substitution rates but all \n"+
                            "the sequences have the same date. In order for this to \n"+
                            "work, a strong prior is required on the substitution\n"+
                            "rate or the root of the tree.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            warningShown = true;
        }

    }

    public void selectionChanged() {

        int[] selRows = modelTable.getSelectedRows();
        if (selRows == null || selRows.length == 0) {
        } else {
            currentModel = options.partitionModels.get(selRows[0]);
        }
    }

    public JComponent getExportableComponent() {

        return this;
    }

    private Action setSRD06Action = new AbstractAction("Use SRD06 Model") {
        public void actionPerformed(ActionEvent actionEvent) {
            setSRD06Model();
        }
    };

    class ModelTableModel extends AbstractTableModel {

        /**
         *
         */
        private static final long serialVersionUID = -6707994233020715574L;
        String[] columnNames = { "Model" };

        public ModelTableModel() {
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            if (options == null) return 0;
            return options.partitionModel.size();
        }

        public Object getValueAt(int row, int col) {
            PartitionModel model = options.partitionModel.get(row);
            switch (col) {
                case 0: return model.getName();
                default:
                    throw new IllegalArgumentException("unknown column, " + col);
            }
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public Class getColumnClass(int c) {
            if (getRowCount() == 0) {
                return Object.class;
            }
            return getValueAt(0, c).getClass();
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();

            buffer.append(getColumnName(0));
            for (int j = 1; j < getColumnCount(); j++) {
                buffer.append("\t");
                buffer.append(getColumnName(j));
            }
            buffer.append("\n");

            for (int i = 0; i < getRowCount(); i++) {
                buffer.append(getValueAt(i, 0));
                for (int j = 1; j < getColumnCount(); j++) {
                    buffer.append("\t");
                    buffer.append(getValueAt(i, j));
                }
                buffer.append("\n");
            }

            return buffer.toString();
        }
    };

}