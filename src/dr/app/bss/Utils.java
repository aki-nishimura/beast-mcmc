package dr.app.bss;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import dr.evolution.io.NewickImporter;
import dr.evolution.io.NexusImporter;
import dr.evolution.tree.NodeRef;
import dr.evolution.tree.Tree;
import dr.evolution.util.MutableTaxonList;
import dr.evolution.util.Taxon;
import dr.evomodel.tree.TreeModel;

public class Utils {

	// /////////////////
	// ---CONSTANTS---//
	// /////////////////

	// public static final int TREE_MODEL_ELEMENT = 0;
	public static final int BRANCH_MODEL_ELEMENT = 1;
	public static final int SITE_RATE_MODEL_ELEMENT = 2;
	public static final int BRANCH_RATE_MODEL_ELEMENT = 3;
	public static final int FREQUENCY_MODEL_ELEMENT = 4;
	public static final String ABSOLUTE_HEIGHT = "absoluteHeight";

	// ///////////////////////////////
	// ---GENERAL UTILITY METHODS---//
	// ///////////////////////////////

	public static void centreLine(String line, int pageWidth) {
		int n = pageWidth - line.length();
		int n1 = n / 2;
		for (int i = 0; i < n1; i++) {
			System.out.print(" ");
		}
		System.out.println(line);
	}

	public static void printMap(Map<?, ?> mp) {
		Iterator<?> it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Entry<?, ?> pairs = (Entry<?, ?>) it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
			// it.remove(); // avoids a ConcurrentModificationException
		}
	}// END: printMap

	public static int arrayIndex(String[] array, String element) {

		List<String> vector = new ArrayList<String>();
		for (int i = 0; i < array.length; i++) {
			vector.add(array[i]);
		}

		return vector.indexOf(element);
	}// END: arrayIndex

	public static ArrayList<TreeModel> treesToList(PartitionDataList dataList) {

		ArrayList<TreeModel> treeModelsList = new ArrayList<TreeModel>();
		for (PartitionData data : dataList) {
			treeModelsList.add(data.createTreeModel());
		}

		return treeModelsList;
	}// END: treesToList

	public static boolean taxonExists(Taxon taxon, MutableTaxonList taxonList) {

		boolean exists = false;
		for (Taxon taxon2 : taxonList) {

			if (taxon.equals(taxon2)) {
				exists = true;
				break;
			}

		}

		return exists;
	}// END: taxonExists

	public static double getAbsoluteTaxonHeight(Taxon taxon, TreeModel tree) {

		double height = 0.0;
		for (int i = 0; i < tree.getExternalNodeCount(); i++) {

			NodeRef externalNode = tree.getExternalNode(i);
			Taxon externalNodeTaxon = tree.getNodeTaxon(externalNode);

			if (externalNodeTaxon.equals(taxon)) {
				height = tree.getNodeHeight(externalNode);
			}
		}// END: external node loop

		return height;
	}// END: getAbsoluteTaxonHeight

	public static boolean isTreeModelInList(TreeModel treeModel,
			ArrayList<TreeModel> treeModelList) {

		boolean exists = false;

		for (TreeModel treeModel2 : treeModelList) {

			if (treeModel.getNewick().equalsIgnoreCase(treeModel2.getNewick())) {
				exists = true;
				break;
			}

		}

		return exists;
	}// END: isTreeModelInList

	public static int treeModelIsIdenticalWith(TreeModel treeModel,
			ArrayList<TreeModel> treeModelList) {

		int index = -Integer.MAX_VALUE;

		for (TreeModel treeModel2 : treeModelList) {

			if (treeModel.getNewick().equalsIgnoreCase(treeModel2.getNewick())) {
				index = treeModelList.indexOf(treeModel2);
				break;
			}

		}

		return index;
	}// END: treeModelIsIdenticalWith

	public static boolean isElementInList(PartitionData data,
			ArrayList<PartitionData> partitionList, int elementIndex) {

		boolean exists = false;

		switch (elementIndex) {

		case BRANCH_RATE_MODEL_ELEMENT:

			for (PartitionData data2 : partitionList) {
				if(clockRateModelToString(data).equalsIgnoreCase(clockRateModelToString(data2))) {
					exists = true;
					break;
				}
			}
			
			break;

		case FREQUENCY_MODEL_ELEMENT:

			for (PartitionData data2 : partitionList) {
				if(frequencyModelToString(data).equalsIgnoreCase(frequencyModelToString(data2))) {
					exists = true;
					break;
				}
			}

			break;

		case BRANCH_MODEL_ELEMENT:

			for (PartitionData data2 : partitionList) {
				if(branchSubstitutionModelToString(data).equalsIgnoreCase(branchSubstitutionModelToString(data2))) {
					exists = true;
					break;
				}
			}
			
			break;

		case SITE_RATE_MODEL_ELEMENT:

			for (PartitionData data2 : partitionList) {
				if(siteRateModelToString(data).equalsIgnoreCase(siteRateModelToString(data2))) {
					exists = true;
					break;
				}
			}

			break;

		default:

			throw new RuntimeException("Unknown element");

		}// END: switch

		return exists;
	}// END: isModelInList

	public static int isIdenticalWith(PartitionData data,
			ArrayList<PartitionData> partitionList, int elementIndex) {

		int index = -Integer.MAX_VALUE;

		switch (elementIndex) {

		case BRANCH_RATE_MODEL_ELEMENT:

			for (PartitionData data2 : partitionList) {
				if(clockRateModelToString(data).equalsIgnoreCase(clockRateModelToString(data2))) {
					index = partitionList.indexOf(data2);
					break;
				}
			}
			
			break;

		case FREQUENCY_MODEL_ELEMENT:

			for (PartitionData data2 : partitionList) {
				if(frequencyModelToString(data).equalsIgnoreCase(frequencyModelToString(data2))) {
					index = partitionList.indexOf(data2);
					break;
				}
			}
			
			break;

		case BRANCH_MODEL_ELEMENT:

			for (PartitionData data2 : partitionList) {
				if(branchSubstitutionModelToString(data).equalsIgnoreCase(branchSubstitutionModelToString(data2))) {
					index = partitionList.indexOf(data2);
					break;
				}
			}
			
			break;

		case SITE_RATE_MODEL_ELEMENT:

			for (PartitionData data2 : partitionList) {
				if(siteRateModelToString(data).equalsIgnoreCase(siteRateModelToString(data2))) {
					index = partitionList.indexOf(data2);
					break;
				}
			}

			break;

		default:

			throw new RuntimeException("Unknown element");

		}// END: switch

		return index;
	}// END: isIdenticalWith

	// /////////////////////////
	// ---TO STRING METHODS---//
	// /////////////////////////
	
	public static String clockRateModelToString(PartitionData data) {
		
		String string = PartitionData.clockModels[data.clockModelIndex];
		
		string += (" ( ");
		for (int i = 0; i < data.clockParameterIndices[data.clockModelIndex].length; i++) {
			string += data.clockParameterValues[data.clockParameterIndices[data.clockModelIndex][i]];
			string +=" ";
		}// END: indices loop
		string +=")";
		
		return string;
	}

	public static String frequencyModelToString(PartitionData data) {
		
		String string = PartitionData.frequencyModels[data.frequencyModelIndex];
		
		string += (" ( ");
		for (int i = 0; i < data.frequencyParameterIndices[data.frequencyModelIndex].length; i++) {
			string += data.frequencyParameterValues[data.frequencyParameterIndices[data.frequencyModelIndex][i]];
			string +=" ";
		}// END: indices loop
		string +=")";
		
		return string;
	}
	
	public static String branchSubstitutionModelToString(PartitionData data) {
		
		String string = PartitionData.substitutionModels[data.substitutionModelIndex];
		
		string += (" ( ");
		for (int i = 0; i < data.substitutionParameterIndices[data.substitutionModelIndex].length; i++) {
			string += data.substitutionParameterValues[data.substitutionParameterIndices[data.substitutionModelIndex][i]];
			string +=" ";
		}// END: indices loop
		string +=")";
		
		return string;
	}
	
	public static String siteRateModelToString(PartitionData data) {
		
		String string = PartitionData.siteRateModels[data.siteRateModelIndex];
		
		string += (" ( ");
		for (int i = 0; i < data.siteRateModelParameterIndices[data.siteRateModelIndex].length; i++) {
			string += data.siteRateModelParameterValues[data.siteRateModelParameterIndices[data.siteRateModelIndex][i]];
			string +=" ";
		}// END: indices loop
		string +=")";
		
		return string;
	}
	
	// /////////////////
	// ---GUI UTILS---//
	// /////////////////

	public static int getTabbedPaneComponentIndex(JTabbedPane tabbedPane,
			String title) {

		int index = -Integer.MAX_VALUE;

		int count = tabbedPane.getTabCount();
		for (int i = 0; i < count; i++) {
			if (tabbedPane.getTitleAt(i).toString().equalsIgnoreCase(title)) {
				index = i;
				break;
			}// END: title check

		}// END: i loop

		return index;
	}// END: getComponentIndex

	public static Frame getActiveFrame() {
		Frame result = null;
		Frame[] frames = Frame.getFrames();
		for (int i = 0; i < frames.length; i++) {
			Frame frame = frames[i];
			if (frame.isVisible()) {
				result = frame;
				break;
			}
		}
		return result;
	}

	public static String getMultipleWritePath(File outFile,
			String defaultExtension, int i) {

		String path = outFile.getParent();
		String[] nameArray = outFile.getName().split("\\.", 2);
		String name = ((i == 0) ? nameArray[0] : nameArray[0] + i);

		String extension = (nameArray.length == 1) ? (defaultExtension)
				: (nameArray[1]);
		String fullPath = path + System.getProperty("file.separator") + name
				+ "." + extension;

		return fullPath;
	}// END: getMultipleWritePath

	public static String getWritePath(File outFile, String defaultExtension) {

		String path = outFile.getParent();
		String[] nameArray = outFile.getName().split("\\.", 2);
		String name = nameArray[0];

		String extension = (nameArray.length == 1) ? (defaultExtension)
				: (nameArray[1]);
		String fullPath = path + System.getProperty("file.separator") + name
				+ "." + extension;

		return fullPath;
	}// END: getWritePath

	// ////////////////////////////////
	// ---EXCEPTION HANDLING UTILS---//
	// ////////////////////////////////

	public static void handleException(final Throwable e, final String message) {

		final Thread t = Thread.currentThread();

		if (SwingUtilities.isEventDispatchThread()) {
			showExceptionDialog(t, e, message);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					showExceptionDialog(t, e, message);
				}
			});
		}// END: edt check
	}// END: uncaughtException

	public static void handleException(final Throwable e) {

		final Thread t = Thread.currentThread();

		if (SwingUtilities.isEventDispatchThread()) {
			showExceptionDialog(t, e);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					showExceptionDialog(t, e);
				}
			});
		}// END: edt check
	}// END: uncaughtException

	private static void showExceptionDialog(Thread t, Throwable e) {

		String msg = String.format("Unexpected problem on thread %s: %s",
				t.getName(), e.getMessage());

		logException(t, e);

		JOptionPane.showMessageDialog(Utils.getActiveFrame(), //
				msg, //
				"Error", //
				JOptionPane.ERROR_MESSAGE, //
				BeagleSequenceSimulatorApp.errorIcon);
	}// END: showExceptionDialog

	private static void showExceptionDialog(Thread t, Throwable e,
			String message) {

		String msg = String.format("Unexpected problem on thread %s: %s" + "\n"
				+ message, t.getName(), e.getMessage());

		logException(t, e);

		JOptionPane.showMessageDialog(Utils.getActiveFrame(), //
				msg, //
				"Error", //
				JOptionPane.ERROR_MESSAGE, //
				BeagleSequenceSimulatorApp.errorIcon);
	}// END: showExceptionDialog

	private static void logException(Thread t, Throwable e) {
		e.printStackTrace();
	}// END: logException

	// ///////////////////////
	// ---DEBUGGING UTILS---//
	// ///////////////////////

	public static void printArray(int[] x) {
		for (int i = 0; i < x.length; i++) {
			System.out.println(x[i]);
		}
	}// END: printArray

	public static void printArray(String[] x) {
		for (int i = 0; i < x.length; i++) {
			System.out.println(x[i]);
		}
	}// END: printArray

	public static void print2DArray(double[][] array) {
		for (int row = 0; row < array.length; row++) {
			for (int col = 0; col < array[row].length; col++) {
				System.out.print(array[row][col] + " ");
			}
			System.out.print("\n");
		}
	}// END: print2DArray

	public static void printBranchSubstitutionModel(PartitionData data) {
		System.out.print("\tBranch Substitution model: ");
		System.out.print(branchSubstitutionModelToString(data));
		System.out.print("\n");
	}// END: printBranchSubstitutionModel

	public static void printClockRateModel(PartitionData data) {
		System.out.print("\tClock rate model: ");
		System.out.print(clockRateModelToString(data));
		System.out.print("\n");
	}// END: printClockRateModel

	public static void printFrequencyModel(PartitionData data) {
		System.out.print("\tFrequency model: ");
		System.out.print(frequencyModelToString(data));
		System.out.print("\n");
	}// END: printFrequencyModel
	
	public static void printSiteRateModel(PartitionData data) {
		System.out.print("\tSite rate model: ");
		System.out.print(siteRateModelToString(data));
		System.out.print("\n");
	}// END: printFrequencyModel
	
	public static void printPartitionData(PartitionData data) {

		System.out.println("\tTree model: " + data.treeFile);
		System.out.println("\tData type: "+ PartitionData.dataTypes[data.dataTypeIndex]);
		System.out.println("\tFrom: " + data.from);
		System.out.println("\tTo: " + data.to);
		System.out.println("\tEvery: " + data.every);
		printBranchSubstitutionModel(data);
		printClockRateModel(data);
		printFrequencyModel(data);
	    printSiteRateModel(data);
		
	}// END: printPartitionData

	public static void printPartitionDataList(PartitionDataList dataList) {

		// printTaxonList(dataList);
		System.out.println("\tReplications: " + dataList.siteCount);

		int row = 1;
		for (PartitionData data : dataList) {

			System.out.println("Partition: " + row);

			printPartitionData(data);

			row++;
		}// END: data list loop

	}// END: printDataList

	public static void printForestList(PartitionDataList dataList) {

		for (File treeFile : dataList.forestList) {
			System.out.println(treeFile);
		}

	}// END: printForestList

	public static void printTaxonList(PartitionDataList dataList) {
		for (int i = 0; i < dataList.taxonList.getTaxonCount(); i++) {

			System.out.println(dataList.taxonList.getTaxon(i).getId()
					+ ": "
					+ dataList.taxonList.getTaxon(i).getAttribute(
							Utils.ABSOLUTE_HEIGHT));

		}// END: taxon loop
	}// END: printTaxonList

	public static TreeModel importTreeFromFile(File file) {

		TreeModel treeModel = null;

		try {

			BufferedReader reader = new BufferedReader(new FileReader(file));

			String line = reader.readLine();

			Tree tree = null;

			if (line.toUpperCase().startsWith("#NEXUS")) {

				NexusImporter importer = new NexusImporter(reader);
				tree = importer.importTree(null);

			} else {

				NewickImporter importer = new NewickImporter(reader);
				tree = importer.importTree(null);

			}

			reader.close();
			treeModel = new TreeModel(tree);

		} catch (Exception e) {
			Utils.handleException(e);
		}// END: try-catch block

		return treeModel;

	}// END: importTreeFromFile

}// END: class
