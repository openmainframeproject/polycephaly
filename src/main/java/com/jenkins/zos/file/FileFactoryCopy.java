package com.jenkins.zos.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import com.ibm.jzos.FileFactory;
/**
* @author gedgingt
* @version v4.0.0
* Date 12/24/2018
*
* SPDX-License-Identifier: Apache-2.0 
* SPDX-License-Identifier: CC-BY-4.0
*/ 

/**
 * Sample program that uses the FileFactory class to copy a text file or dataset.
 * The input and output file names are given as arguments to main, 
 * and they main be either POSIX (HFS) file names or MVS dataset names.
 * <p>
 * If the target file is an MVS dataset, then its LRECL should be compatible
 * with the source file/dataset. 
 * <p>
 * Example file names:
 * <ul>
 * <li>/etc/profile</li>
 * <li>//DD:INPUT</li>
 * <li>//'SYS1.MACLIB(ABEND)'</li>
 * <li>//MY.DATASET</li>
 * </ul>
 * 
 * @see com.ibm.jzos.FileFactory
 */
public class FileFactoryCopy {

	   public static void copy(String[] args) throws IOException {
	    	if (args.length != 2) {
	    		System.out.println("Usage: inputfileOrDataset outputFileOrDataset");
	    		System.exit(8);
	    	}
	        BufferedReader rdr = null;
	        BufferedWriter wtr = null;
	        long count = 0;
			try {
				rdr = FileFactory.newBufferedReader(args[0]);
				wtr = FileFactory.newBufferedWriter(args[1]);
			
				String line;
				while ((line = rdr.readLine()) != null) {
					wtr.write(line);
					wtr.write("\n");
					count++;
				}
				System.out.println("Copied " + count + " lines from: " + args[0] + " to: " + args[1] );
			} finally {
				if (wtr != null) wtr.close();
				if (rdr != null) rdr.close();
			}
	    }
}
