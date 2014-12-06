package pl.grm.bol.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import pl.grm.bol.lib.BLog;
import pl.grm.bol.lib.Config;
import pl.grm.bol.lib.FileOperation;
import pl.grm.bol.lib.TypeOfProject;
import pl.grm.bol.lib.net.UpdateFrame;

public class LauncherUpdater {
	public static final String	LOG_FILE_NAME	= "launcher_updater.log";
	private static String		launcherJarAbsPath;
	private static String		sVersion;
	private static String		launcherPId;
	private static String		launcherDirPath;
	private static BLog			logger;
	private static UpdateFrame	updateDialog;
	
	public static void main(String[] args) {
		logger = new BLog(LOG_FILE_NAME);
		try {
			logger.info("Updater Started");
			if (args.length != 3) {
				for (String i : args) {
					logger.info("Param: " + i);
				}
				throw new IOException("Bad arguments!");
			}
			launcherJarAbsPath = args[0];
			launcherPId = args[1];
			launcherDirPath = args[2];
			if (launcherJarAbsPath.contains("/BoL-Launcher_Client/bin/")
					|| launcherJarAbsPath.contains("/BoL-Launcher_Client/build/")) { throw new Exception(
					"You are propably running it from Eclipse!"); }
			updateDialog = new UpdateFrame("Launcher update", TypeOfProject.UPDATER, logger);
			killExec("taskkill /pid " + launcherPId);
			Thread.sleep(4000L);
			updateDialog.setVisible(true);
			updateDialog.getButtonUpdate().setEnabled(true);
			updateDialog.setUpdaterObj(LauncherUpdater.class);
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	/**
	 * Method invoked after downloading Launcher file
	 */
	public static void saveUpdatedLauncher(String sVersion) {
		logger.info("Saving new file");
		logger.info(launcherDirPath);
		LauncherUpdater.sVersion = sVersion;
		madeBackup();
		String updated = null;
		if (saveNewLauncher()) {
			deleteBackupFile();
			updateConfig();
			updated = "Success";
		} else {
			restoreBackup();
			updated = "Failed";
		}
		runLauncher();
		try {
			Thread.sleep(1000L);
		}
		catch (InterruptedException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		JOptionPane.showMessageDialog(updateDialog, "Update" + updated, "Update Finished",
				JOptionPane.PLAIN_MESSAGE);
		logger.info("Update " + updated);
		updateDialog.dispose();
	}
	
	/**
	 * Kills specified process of Launcher
	 * 
	 * @param processStringPID
	 * @return list Of Process
	 * @throws IOException
	 */
	private static ArrayList<String> killExec(String processStringPID) throws IOException {
		String outStr = "";
		ArrayList<String> processOutList = new ArrayList<String>();
		int i = -1;
		Process p = Runtime.getRuntime().exec(processStringPID);
		InputStream in = p.getInputStream();
		x11 : while ((i = in.read()) != -1) {
			if ((char) i == '\n') {
				processOutList.add((outStr));
				outStr = "";
				continue x11;
			}
			outStr += (char) i;
		}
		return processOutList;
	}
	
	/**
	 * change the name of old file by adding '_old'
	 */
	private static synchronized void madeBackup() {
		String fileNameC = launcherJarAbsPath.substring(0, launcherJarAbsPath.length() - 4);
		File oldFile = new File(launcherJarAbsPath);
		File newFile = new File(fileNameC + "_old.jar");
		if (!newFile.exists()) {
			if (oldFile.renameTo(newFile)) {
				logger.info("Old file backuped.");
			} else {
				logger.info("Smth went wrong.");
			}
		} else {
			logger.info("File Exists");
		}
	}
	
	/**
	 * Restore backuped file '_old'
	 */
	private static void restoreBackup() {
		String fileNameC = launcherJarAbsPath.substring(0, launcherJarAbsPath.length() - 4);
		fileNameC = fileNameC.concat("_old.jar");
		File oldFile = new File(fileNameC);
		File newFile = new File(launcherJarAbsPath);
		if (!newFile.exists()) {
			if (oldFile.renameTo(newFile)) {
				logger.info("Backup restored!");
			} else {
				logger.info("Smth went wrong.");
			}
		} else {
			logger.info("File Exists");
		}
	}
	
	/**
	 * replace old Launcher with new one
	 * 
	 * @return
	 */
	private static boolean saveNewLauncher() {
		InputStream inStream = null;
		OutputStream outStream = null;
		String fileName = "BoL-Launcher-" + sVersion + Config.RELEASE_TYPE;
		try {
			File fromFile = new File(Config.BOL_MAIN_PATH + fileName);
			File toFile = new File(launcherDirPath + "\\" + fileName);
			logger.info("New launcher file: " + launcherDirPath + "\\" + fileName);
			inStream = new FileInputStream(fromFile);
			outStream = new FileOutputStream(toFile);
			
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}
			inStream.close();
			outStream.close();
			logger.info("Deleting temp fles.");
			fromFile.delete();
			logger.info("Launcher updated successfully!");
			return true;
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		return false;
	}
	
	/**
	 * delete (with '_old') file
	 */
	private static void deleteBackupFile() {
		String fileNameC = launcherJarAbsPath.substring(0, launcherJarAbsPath.length() - 4);
		File file = new File(fileNameC + "_old.jar");
		logger.info("Deleting backup");
		file.delete();
		try {
			File file2 = new File(FileOperation.getCurrentJarPath(LauncherUpdater.class));
			file2.deleteOnExit();
		}
		catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	/**
	 * Update config ini with launcher param version
	 */
	private static void updateConfig() {
		try {
			FileOperation.writeConfigParamLauncher(FileOperation.readConfigFile(sVersion),
					"version", sVersion);
		}
		catch (IOException | IllegalArgumentException | SecurityException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	/**
	 * Runs the Launcher
	 */
	private static void runLauncher() {
		File dir = new File(launcherDirPath);
		logger.info("Starting new launcher");
		ProcessBuilder processBuilder = new ProcessBuilder(Config.JAVA_PATH, "-jar",
				launcherDirPath + "\\" + sVersion);
		try {
			processBuilder.directory(dir);
			processBuilder.start();
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
}
