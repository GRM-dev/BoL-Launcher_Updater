package pl.grm.bol.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import pl.grm.bol.lib.BLog;
import pl.grm.bol.lib.Config;
import pl.grm.bol.lib.FileOperation;

public class Updater {
	public static final String		LOG_FILE_NAME	= "updater.log";
	private static String			jarFileAbsPath;
	private static String			version;
	private static String			fileName;
	private static String			launcherPId;
	private static String			launcherDirPath;
	private static BLog				logger;
	private static UpdaterDialog	dialog;
	private static JProgressBar		progressBar;
	private static boolean			updated;
	
	public static void main(String[] args) {
		logger = new BLog(LOG_FILE_NAME);
		try {
			logger.info("Updater Started");
			assignArgs(args);
			dialog = new UpdaterDialog();
			progressBar = dialog.getProgressBar();
			progressBar.setValue(5);
			killExec("taskkill /pid " + launcherPId);
			progressBar.setValue(10);
			Thread.sleep(4000L);
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		catch (InterruptedException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		progressBar.setValue(20);
		checkoutServerVersion();
		progressBar.setValue(30);
		downloadNewLauncher();
		progressBar.setValue(55);
		madeBackup();
		progressBar.setValue(60);
		if (saveNewLauncher()) {
			progressBar.setValue(68);
			updateConfig();
			progressBar.setValue(77);
			deleteBackupFile();
			progressBar.setString("Updated");
			updated = true;
		} else {
			progressBar.setString("Not updated");
			restoreBackup();
			updated = false;
		}
		progressBar.setValue(90);
		runLauncher();
		progressBar.setValue(100);
		try {
			Thread.sleep(1000L);
		}
		catch (InterruptedException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		String upd = null;
		if (updated) {
			upd = "Success";
		} else {
			upd = "Failed";
		}
		JOptionPane.showMessageDialog(dialog, "Update" + upd, "Update Finished",
				JOptionPane.PLAIN_MESSAGE);
		dialog.dispose();
	}
	
	/**
	 * Assign args to fields
	 * 
	 * @param args
	 * @throws IOException
	 */
	private static void assignArgs(String[] args) throws IOException {
		if (args.length != 3) { throw new IOException("Bad arguments!"); }
		jarFileAbsPath = args[0];
		launcherPId = args[1];
		launcherDirPath = args[2];
		if (jarFileAbsPath.contains("/BoL-Launcher_Client/bin/")) { throw new IOException(
				"You are propably running it from Eclipse!"); }
	}
	
	/**
	 * Kills specified process of Launcher
	 * 
	 * @param processString
	 * @return list Of Process
	 * @throws IOException
	 */
	private static ArrayList<String> killExec(String processString) throws IOException {
		String outStr = "";
		ArrayList<String> processOutList = new ArrayList<String>();
		int i = -1;
		Process p = Runtime.getRuntime().exec(processString);
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
	 * Check version of launcher on the web server.
	 */
	private static void checkoutServerVersion() {
		Ini sIni = new Ini();
		URL url;
		try {
			url = new URL(Config.SERVER_VERSION_LINK);
			sIni.load(url);
		}
		catch (MalformedURLException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		catch (InvalidFileFormatException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		version = sIni.get("Launcher", "last_version");
		logger.info("New version: " + version);
	}
	
	/**
	 * Downloads new Launcher.
	 */
	private static void downloadNewLauncher() {
		fileName = "BoL-Launcher-" + version + "-SNAPSHOT.jar";
		logger.info("Downloadin file: " + fileName);
		try {
			URL website = new URL(Config.SERVER_SITE_LINK + "jenkins/artifacts/" + fileName);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos;
			fos = new FileOutputStream(Config.BOL_MAIN_PATH + fileName);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			logger.info("New launcher downloaded.");
			fos.close();
		}
		catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		catch (MalformedURLException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	/**
	 * change the name of old file by adding '_old'
	 */
	private static synchronized void madeBackup() {
		String fileNameC = jarFileAbsPath.substring(0, jarFileAbsPath.length() - 4);
		File oldFile = new File(jarFileAbsPath);
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
		String fileNameC = jarFileAbsPath.substring(0, jarFileAbsPath.length() - 4);
		fileNameC = fileNameC.concat("_old.jar");
		File oldFile = new File(fileNameC);
		File newFile = new File(jarFileAbsPath);
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
		String fileNameC = jarFileAbsPath.substring(0, jarFileAbsPath.length() - 4);
		File file = new File(fileNameC + "_old.jar");
		file.delete();
		try {
			File file2 = new File(FileOperation.getCurrentJarPath(Updater.class));
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
			FileOperation.writeConfigParamLauncher(FileOperation.readConfigFile(version),
					"version", version);
		}
		catch (IOException | IllegalArgumentException | SecurityException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	/**
	 * Runs the Launcher
	 */
	private static void runLauncher() {
		String separator = System.getProperty("file.separator");
		String javaPath = System.getProperty("java.home") + separator + "bin" + separator + "java";
		File dir = new File(launcherDirPath);
		ProcessBuilder processBuilder = new ProcessBuilder(javaPath, "-jar", launcherDirPath + "\\"
				+ fileName);
		try {
			processBuilder.directory(dir);
			processBuilder.start();
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
}
