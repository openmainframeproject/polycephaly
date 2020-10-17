package com.zos.java.utlities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
/**
* @author gedgingt
* @version v4.0.0
* Date 12/24/2018
*
* SPDX-License-Identifier: Apache-2.0 
*/
public class RunShell {
	
	public static int executeShell(String shellCmd) {
		int RC = 0;
		Process proc = null;

		try {
			Runtime rt = Runtime.getRuntime();
			proc = rt.exec(shellCmd);
			proc.waitFor();
			RC = proc.exitValue();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			RC = 8;
			e.printStackTrace();
			System.out.println("**RunShell had an exception failed RC=$RC, problem with $shellCmd \n");
		}
		return RC;
	}

	public static void gitGc(Path directory) throws IOException, InterruptedException {
		runCommand(directory, "git", "gc");
	}

	public static void runCommand(Path directory, String... command) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder().command(command).directory(directory.toFile());
		Process p = pb.start();
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
		outputGobbler.start();
		errorGobbler.start();
		int exit = p.waitFor();
		errorGobbler.join();
		outputGobbler.join();
		if (exit != 0) {
			throw new AssertionError(String.format("runCommand returned %d", exit));
		}
	}

	private static class StreamGobbler extends Thread {

		InputStream is;
		String type;

		private StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(type + "> " + line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

}
