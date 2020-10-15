package com.zos.resbuild;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

import com.ibm.jzos.FileFactory;
import com.ibm.jzos.MvsJobSubmitter;

public class SubmitJob {

	/**
	 * A sample main method that submits JCL read from a file and then polls its
	 * status (for up to a minute) until it is complete. The first argument to
	 * main can be a Unix file/path name or a "//DATASET.NAME".
	 */
	public static void main(String[] args) throws IOException {

		if (args.length < 1) {
			throw new IllegalArgumentException("Missing main argument: filename");
		}

		String jobname = null;
		MvsJobSubmitter jobSubmitter = new MvsJobSubmitter();
		BufferedReader rdr = FileFactory.newBufferedReader(args[0]);
		System.out.println(" args[0] = " + args[0] + " ");

		try {
			String line;
			while ((line = rdr.readLine()) != null) {

				if (jobname == null) {
					StringTokenizer tok = new StringTokenizer(line);
					String jobToken = tok.nextToken();

					if (jobToken.startsWith("//")) {
						jobname = jobToken.substring(2);
					}
				}

				jobSubmitter.write(line);
			}
		} finally {
			if (rdr != null) {
				rdr.close();
			}
		}

		// Submits the job to the internal reader
		jobSubmitter.close();

	}
}
