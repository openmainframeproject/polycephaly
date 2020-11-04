package com.zos.resbuid;

import java.io.File;

import com.ibm.dbb.build.BuildException;
import com.ibm.dbb.build.CopyToHFS;
import com.ibm.dbb.build.DDStatement;
import com.ibm.dbb.build.MVSExec;
import com.ibm.jzos.ZFile;

/**
* @author gedgingt
* @version v4.0.0
* Date 11/04/2020
*
* SPDX-License-Identifier: Apache-2.0 
*/

public class ExecProgram {

	public static void main(String[] args) {

		/* 
		 * setup the defaults for tempCreateOptions, likeCreateOptions and oldOptions
		 */
	}
	
	public static int iebcopy(String[] args) {
		
		/*
		 * args[0] = sysut1 dataset
		 * args[1] = sysut2 dataset
		 * args[2] = sysin dataset
		 * args[3] = output dataset volume
		 * args[4] = copy output device type
		 */
		
		String tempCreateOptions = "tracks space(5,5) unit(vio) blksize(0) lrecl(80) recfm(f,b) new";
		String likeCreateOptions = "like(" + args[0] + ") new keep vol(" + args[3] + ") storclas(nonsms) unit(" + args[4] + ")";
		String oldOptions = "old keep vol(" + args[3] + ")";
		
		int rc = 0;
		String logFile = "/tmp/" + args[0] + ".log";
		//int maxRC = 8;
		
		try {
			System.out.println("iebcopy @" + args[0] + "@" + args[1] + "@" + args[2] + "@" + args[3] + "@");
			// define the MVSExec command to execute IEBCOPY
			String sysut2DDname = ZFile.allocDummyDDName();
			MVSExec iebcopy = new MVSExec()
			 			   .pgm("IEBCOPY");
			try {
				System.out.println("allocating dataset " + args[1]);
				ZFile.bpxwdyn("alloc fi(" + sysut2DDname + ") da(" + args[1] + ") reuse " + likeCreateOptions);
				ZFile.bpxwdyn("free fi(" + sysut2DDname + ") ");
			} catch (Exception e) {
				//System.out.println(args[1] + "@ dataset exists, continue");
				//e.printStackTrace();
			}
			
			iebcopy.dd(new DDStatement().name("SYSUT1").dsn(args[0]).options("shr").report(true));
			iebcopy.dd(new DDStatement().name("SYSUT2").dsn(args[1]).options(oldOptions).report(true));
			iebcopy.dd(new DDStatement().name("SYSIN").dsn(args[2]).options("shr").report(true));
			iebcopy.dd(new DDStatement().name("SYSPRINT").options(tempCreateOptions));
			
			iebcopy.copy(new CopyToHFS().ddName("SYSPRINT").file(new File("/tmp/" + args[0] + ".log")).hfsEncoding("IBM-1047"));
			
			//System.out.println("Executing IEBCOPY ");
			rc = iebcopy.execute();
			File file = new File(logFile);
			if (rc == 0) {
				//System.out.println("Finished executing IEBCOPY, return code = " + rc);
		        try {
		        	file.delete();
		        } catch (Exception e1) {}
			} else {
				System.out.println("\u2600\u2600\u2600\u2600*** IEBCOPY failed, return code = " + rc + " check logfile in " + logFile + "\u2600\u2600\u2600****");
	        	//ZFilePrint.main(new String[] {"SYSPRINT"});
	        	//file.delete();
			}


			//if (rc > maxRC)
			//	   throw new BuildException("Return code " + rc + " from iebcopy exceeded maxRC " + maxRC);
			
			// add a copy command to the compile command to copy the SYSPRINT from the temporary dataset to an HFS log file
			//System.out.println("Copying iebcopy logfile to /iebcopy.log");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("MVSExec for IEBCOPY finished with RC=" + rc);
		return rc;
	}
	
	public static void icfdsk(String[] args) {
		/*
		 * args[0] = sysut1 dataset
		 * args[1] = sysut2 dataset
		 * args[2] = copy parms
		 * args[3] = output dataset volume
		 */
		
		String tempCreateOptions = "tracks space(5,5) unit(vio) blksize(80) lrecl(80) recfm(f,b) new";
		String likeCreateOptions = "like(" + args[0] + ") new,keep vol(" + args[3] + ")";
		int rc = 0;
		int maxRC = 8;
		
		try {
			// define the MVSExec command to execute IEBCOPY
			MVSExec ickdsf = new MVSExec()
			 			   .pgm("ICKDSF");
			ickdsf.dd(new DDStatement().name("SYSIN").dsn(args[3]).options("shr").report(true));
			ickdsf.dd(new DDStatement().name("SYSUT1").dsn(args[1]).options("shr").report(true));
			ickdsf.dd(new DDStatement().name("SYSUT2").dsn(args[2]).options(likeCreateOptions).report(true));
			ickdsf.dd(new DDStatement().name("SYSPRINT").options(tempCreateOptions));
			
			// add a copy command to the compile command to copy the SYSPRINT from the temporary dataset to an HFS log file
			ickdsf.copy(new CopyToHFS().ddName("SYSPRINT").file(new File(args[0] + "/ickdsf.log")).hfsEncoding("IBM-1047"));
			
			rc = ickdsf.execute();
			if (rc > maxRC)
				   throw new BuildException("Return code " + rc + " from iebcopy exceeded maxRC " + maxRC);
			
		} catch (BuildException e) {
			e.printStackTrace();
		}
		System.out.println("MVSExec for IEBCOPY finished");
	}
	
	public static void adrdssu(String[] args) {
		/*
		 * args[0] = sysut1 dataset
		 * args[1] = sysut2 dataset
		 * args[2] = copy parms
		 * args[3] = output dataset volume
		 */
		
		String tempCreateOptions = "tracks space(5,5) unit(vio) blksize(80) lrecl(80) recfm(f,b) new";
		int rc = 0;
		int maxRC = 8;
		
		try {
			// define the MVSExec command to execute IEBCOPY
			MVSExec adrdssu = new MVSExec()
			 			   .pgm("ADRDSSU");
			adrdssu.dd(new DDStatement().name("SYSIN").dsn(args[3]).options("shr").report(true));
			adrdssu.dd(new DDStatement().name("SYSPRINT").options(tempCreateOptions));
			
			// add a copy command to the compile command to copy the SYSPRINT from the temporary dataset to an HFS log file
			adrdssu.copy(new CopyToHFS().ddName("SYSPRINT").file(new File(args[0] + "/ickdsf.log")).hfsEncoding("IBM-1047"));
			
			rc = adrdssu.execute();
			if (rc > maxRC)
				   throw new BuildException("Return code " + rc + " from iebcopy exceeded maxRC " + maxRC);
			
		} catch (BuildException e) {
			e.printStackTrace();
		}
		System.out.println("MVSExec for ADRDSSU finished");
	}
}
