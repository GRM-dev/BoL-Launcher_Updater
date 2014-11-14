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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import pl.grm.boll.lib.FileOperation;

public class Updater {
	private static final String	APP_DATA			= System.getenv("APPDATA");
	private static final String	BOL_CONF_PATH		= APP_DATA + "\\BOL\\";
	private static final String	LOG_FILE_NAME		= "updater.log";
	private static final String	SERVER_LINK			= "http://grm-dev.pl/";
	private static final String	SERVER_VERSION_LINK	= SERVER_LINK + "bol/version.ini";
	private static String		jarFileAbsPath;
	private static String		version;
	private static String		fileName;
	private static String		launcherPId;
	private static String		launcherDirPath;
	private static Logger		logger;
	private static FileHandler	fHandler;
	
	public static void main(String[] args) {
		setupLogger();
		try {
			assignArgs(args);
			killExec("taskkill /pid " + launcherPId);
			Thread.sleep(4000L);
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		catch (InterruptedException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		checkoutServerVersion();
		downloadNewLauncher();
		madeBackup();
		if (moveNewLauncherFromTemp()) {
			updateConfig();
			deleteBackupFile();
		} else {
			restoreBackup();
		}
		runLauncher();
	}
	
	/**
	 * Configure Logger to log infos & warnings
	 */
	private static void setupLogger() {
		logger = Logger.getLogger(Updater.class.getName());
		try {
			fHandler = new FileHandler(BOL_CONF_PATH + LOG_FILE_NAME, 1048476, 1, true);
			logger.addHandler(fHandler);
			SimpleFormatter formatter = new SimpleFormatter();
			fHandler.setFormatter(formatter);
		}
		catch (SecurityException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	/**
	 * Credit args to fields
	 * 
	 * @param args
	 * @return true if successufully assigned.
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
			url = new URL(SERVER_VERSION_LINK);
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
	}
	
	/**
	 * Downloads new Launcher.
	 */
	private static void downloadNewLauncher() {
		fileName = "BoL-Launcher-" + version + "-SNAPSHOT.jar";
		try {
			URL website = new URL(SERVER_LINK + "jenkins/artifacts/" + fileName);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos;
			fos = new FileOutputStream(fileName);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
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
	 * delete (with '_old') file
	 */
	private static void deleteBackupFile() {
		String fileNameC = jarFileAbsPath.substring(0, jarFileAbsPath.length() - 4);
		File file = new File(fileNameC + "_old.jar");
		file.delete();
		try {
			File file2 = new File(FileOperation.getCurrentJar(Updater.class));
			file2.deleteOnExit();
		}
		catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	/**
	 * replace old Launcher with new one
	 * 
	 * @return
	 */
	private static boolean moveNewLauncherFromTemp() {
		InputStream inStream = null;
		OutputStream outStream = null;
		try {
			File fromFile = new File(BOL_CONF_PATH + fileName);
			File toFile = new File(launcherDirPath + "\\" + fileName);
			logger.info("New file: " + launcherDirPath + "\\" + fileName);
			inStream = new FileInputStream(fromFile);
			outStream = new FileOutputStream(toFile);
			
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}
			inStream.close();
			outStream.close();
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
	 * Update config ini with launcher param version
	 */
	private static void updateConfig() {
		try {
			FileOperation.writeConfigParamLauncher(FileOperation.readConfigFile(Updater.class),
					"version", version);
		}
		catch (IOException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
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
			Process process = processBuilder.start();
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
}