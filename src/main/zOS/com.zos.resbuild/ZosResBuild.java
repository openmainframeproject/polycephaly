package com.zos.resbuid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import com.ibm.jzos.ZFile;

/**
* @author gedgingt
* @version v4.0.0
* Date 11/04/2020
*
* SPDX-License-Identifier: Apache-2.0 
*/

public class ZosResBuild {

	public static void execute(String[] args) throws Exception {

		if (args.length < 1) {
			System.out.println("USAGE: ZosCopyFiles ConfigurationDirectory PropertyFile");
			System.exit(1);
		}

		InputStream zosPropsFile = null;
		Properties zosProps = new Properties();
		
		File r1File = null;
		BufferedReader r1List = null;
		File r2File = null;
		BufferedReader r2 = null;
		String destDataset = null;
		String copyVol = null;
        String[] datasets = null;
		String[] splitDest = null;
		
		int datasetCount = 0;
		int maxRC = 0;
		int rc = 0;
		int dfdssCount = 1;
		
		InputStream zosVariablesFile = null;
		Properties zosVariables = new Properties();
		
		String[] splitNodeSrc = null;
		String[] splitNodeDest = null;
		String[] sourceNodes = null;
		String[] splitMembers = null;
		
		String pdsDD  = "PDSDD";
		ZFile zFilePDS = null;
		String pdsMember = null;
		
		try {

			zosPropsFile = new FileInputStream(args[0] + "/" + args[1]);
			zosProps.load(zosPropsFile);
			
			//System.out.println("** configuration property = " + zosProps);
			/**
			 * process the Datasets
			 * 
			 * sourceDataset		= 
			 * fromDataset			= 
			 * toDataset			=
			 * dataInputList		= contains the contents of /dataInputList.zProperties
			 * genericList			= used when toDataset = "generic", which includes HLQ and excludes in /zosGenericHLQ.zProperties file
			 * genericFromDataset	= /zosGenericHLQ the fromDataset pattern to be replaced
			 * genericToDataset		= /zosGenericHLQ the toDataset pattern to replace the fromDataset pattern, if match
			 * exline				= the input read line from the zProperty files
			 */
			String sourceDataset = null;
			String members = null;
			String fromDataset = null;
			String toDataset = null;
			String tmpSrc = null;
			
			zosVariablesFile = new FileInputStream(args[0] + "/" + zosProps.getProperty("zosVariables").toString());
			zosVariables.load(zosVariablesFile);
			
			//System.out.println("zosVariables = @" + zosVariables + "@");
			
	        String exline = null;
	        /**
	         * generic*Datasets are used when the To dataset = generic
	         */
	       
			int splitNodeDestCount = 0;
			int splitNodeSrcCount = 0;
			int i = 0;
			Boolean splitNodeDestAsteriskFound = false;
			Boolean splitNodeSrcAsteriskFound = false;
			
			String pdsDSN = zosVariables.getProperty("TEMPPDS").trim();

			r1File = new File(args[0] + "/" + zosProps.getProperty("zosDataInputList").toString());
			r1List = new BufferedReader(new FileReader(r1File));
			
			String[] datasetsProcessed = new String[5000];
			int zCount = 0;
			
			for (String line; (line = r1List.readLine()) != null;) {
				line.trim();
				datasets = line.split(",");
				if (datasets[3].contains("generic")) {
					//* skip only keeping non-generic dataset entries
				} else {
					datasetsProcessed[zCount] = datasets[0];
					zCount++;
				}
			}
			 r1List.close();
	        
			 ZFile.bpxwdyn("alloc fi(" + pdsDD + ") da(" + pdsDSN + ") shr msg(wtp)");
			 
	        /**
	         * datasetCount			= number of datasets processed
	         * datasets				= line from dataInputList.zProperties, generated from the zOSResDataset process, should always be 3 parts
	         * 			sourceDataset, destDataset, members
	         * sourceDataset		= dataset name to copied from, should be a full qualified dataset name
	         * destDataset			= will be the "fromDataset pattern/toDataset pattern" conversion
	         * 			if "generic" process with generic destDataset and exclude zProperty file, if !found, then process normally
	         * 			"=" means to remove the HLQ including the "."
	         * 			if !"*", then replace the entire dataset with the fromDataset
	         * 			"*" with length = 1, then 
	         * 								
	         * splitDest			= split of destDataset by "/"
	         * member				= can be one of three parms
	         * 			"*" - copy all members of PDS to toDataset
	         * 			"zProperty" file, copy only members listed in the zProperty file
	         * 			"USS" invoke DFDSS to copy zFS from source to dest zFS
	         * copyVol	= dataset output volume 
	         * 		
	         */
			 
			r1File = new File(args[0] + "/" + zosProps.getProperty("zosDataInputList").toString());
			r1List = new BufferedReader(new FileReader(r1File));
			Boolean hasDatasetBeenProcessed = false; 
			
			for (String line; (line = r1List.readLine()) != null;) {
				System.out.println("--------------------Processing dataset @" + line.trim() + "@------------------");
				datasetCount++;
				line.trim();
				datasets = line.split(",");
				
				if (datasets.length != 4) {
					/**
					 *  There should be three parts separated by a comma from the ResDataset process
					 */
					System.out.println("Major problem. **Dataset split length missing parms " + datasets.length);
				} else {
					sourceDataset = datasets[0];
					destDataset = datasets[1];
					members = datasets[2];
					copyVol = datasets[3];
					splitDest = destDataset.split("/");
					hasDatasetBeenProcessed = false; 
					
					if (copyVol.contains("generic")) {
						if (Arrays.asList(datasetsProcessed).contains(datasets[0])) {
							System.out.println("@" + datasets[0] + "@ found in datasets already processed - skipping");
							hasDatasetBeenProcessed = true;
						}
						copyVol = "RESVOL1";
					}
					
					if (!hasDatasetBeenProcessed) {
						System.out.println("@" + datasets[0] + "@ not found in datasets already processed");
						if (destDataset.contains("=")) {
							/**
							 *  If destDataset is an equal and only an equal, then removing the HLQ of KRGPRE.
							 */
							toDataset = sourceDataset.replaceFirst(zosVariables.getProperty("KRGPRE").trim(),"");
							System.out.println("@" + zosVariables.getProperty("KRGPRE") + "@ HLQ removal - Copying from dataset " + sourceDataset + " to " + toDataset);
						} else {
							/**
							 *  If destDataset is NOT an equal, then split the destDataset into separate parts by /
							 *  Must be a slash with two parts
							 */
		
							if (splitDest.length !=2 )  {
								System.out.println("Major problem. **DestDataset split length missing parms " + splitDest.length);
							} else { 
								fromDataset = splitDest[0];
							}
		
							/*
							 * If the From Dataset doesn't contain an asterisk, then it is a HLQ replacement
							 * 
							 * If the From Datasets contains an asterisk, then 
							 * 		Is the From Dataset length = 1, not a generic replacement, but a full dataset replacement
							 * 		otherwise, special handling, because multiple generics either in the From or To dataset
							 */
							if (!fromDataset.contains("*")) {
								toDataset = sourceDataset.replaceFirst(fromDataset, splitDest[1]);
								System.out.println("Not * - Copying from dataset " + sourceDataset + " to " + toDataset);
							} else {
								if (fromDataset.contains("*") && (fromDataset.length() == 1)) {
									toDataset = splitDest[1];
									System.out.println("replace all - Copying from dataset " + sourceDataset + " to " + toDataset);
								} else {
									/*
									 * sourceDataset		= fully qualified dataset
									 * destDataset			= conversion pattern  fromDataset/toDatset pattern
									 * splitDest			= split of destDataset by "/", into its parts
									 * fromDataset			= split of destDataset by "/", splitDest[0]
									 * toDataset			= split of destDataset by "/", splitDest[1]
									 * splitNodeDest		= reused to split toDataset by "." into nodes
									 * splitNodeSrc			= split fromDataset by "." into nodes		
									 * sourceNodes			= split of the sourceDataset by "." into nodes
									 * 
									 * Logic
									 * 		determine the to/from pattern and update the sourceDataset, saving into toDataset for copy processing
									 * 
									 * 		sample configurations
									 * 		case(1)	ZOS22.IZD.*.SIZD* to SYSM.IZD.SIZD*
									 * 				splitDest[0] = ZOS22.IZD.*.SIZD*
									 * 				splitDest[1] = SYSM.IZD.SIZD*
									 * 				 			 = ZOS22.IZD.V3R1M0.SIZDEXEC to SYSM.IZD.SIZDEXEC				
									 * 
									 * 		case(2)	SMPE.CPWR.*.SLCX* to SYSM.CPWR.SLCX*  
									 * 				splitDest[0] = SMPE.CPWR.*.SLCX*
									 * 				splitDest[1] = SYSM.CPWR.SLCX*
									 * 							 = SMPE.CPWR.MLCX170.SLCXAUTH to SYSM.CPWR.SLCXAUTH 
									 * 
									 * 		case(3)	ZOS22.OMVS.*.DEV to OMVS.&RESVOL2.*.DEV
									 * 				splitDest[0] = ZOS22.OMVS.*.DEV
									 * 				splitDest[1] = OMVS.&RESVOL2.*.DEV
									 * 							 = ZOS22.OMVS.A000.DEV  to OMVS.&RESVOL2.A000.DEV
									 * 
									 * 		case(4)	MQ.V710.S* to MQ.*.V701.S*
									 * 				splitDest[0] = MQ.V710.S*
									 * 				splitDest[1] = MQ.*.V701.S*
									 * 							 = MQ.V710.SCSQANLC to MQ.*.V701.SCSQANLC
									 * 
									 * 
									 */
									//System.out.println("*******  Special handling, multiple nodes replacement @" + sourceDataset + "@ to @" + destDataset + "@" );
									splitNodeSrc = splitDest[0].split("\\.");
									splitNodeDest = splitDest[1].split("\\.");
									sourceNodes = sourceDataset.split("\\.");
		
									/*
									 *		sample configurations
									 * 		case(1)	ZOS22.IZD.*.SIZD* to SYSM.IZD.SIZD*
									 * 				splitNodeSrc 	= splitDest[0] 		= ZOS22.IZD.*.SIZD*
									 * 				splitNodeDest	= splitDest[1] 		= SYSM.IZD.SIZD*
									 * 				sourceNodes 	= sourceDataset		= ZOS22.IZD.V3R1M0.SIZDEXEC	
									 * 				 			 	= ZOS22.IZD.V3R1M0.SIZDEXEC to SYSM.IZD.SIZDEXEC		
									 * 
									 * 		case(2)	SMPE.CPWR.*.SLCX* to SYSM.CPWR.SLCX*  
									 * 				splitNodeSrc	= splitDest[0] 		= SMPE.CPWR.*.SLCX*
									 * 				splitNodeDest	= splitDest[1] 		= SYSM.CPWR.SLCX*
									 * 				sourceNodes 	= sourceDataset		= SMPE.CPWR.MLCX170.SLCXAUTH
									 * 							 	= SMPE.CPWR.MLCX170.SLCXAUTH to SYSM.CPWR.SLCXAUTH 
									 * 
									 */
		
									
									if (splitNodeDest[(splitNodeDest.length-1)].contains("*") && splitNodeDest[(splitNodeDest.length-1)].length() != 1 ) {
										System.out.println("splitNodeDest[(splitNodeDest.length-1)].contains(*) && splitNodeDest[(splitNodeDest.length-1)].length() != 1 " );
										
										if (splitNodeDest[(splitNodeDest.length-1)].equalsIgnoreCase(splitNodeSrc[(splitNodeSrc.length-1)])) {
											splitNodeSrc = sourceDataset.split("\\.");
											tmpSrc = splitDest[1].replaceAll("\\*","");
											toDataset = tmpSrc.replaceAll(splitNodeDest[(splitNodeDest.length-1)], sourceNodes[(splitNodeSrc.length-1)]);
										} else {
											System.out.println("**ERROR** generic pattern exception in " + splitNodeDest[(splitNodeDest.length-1)]);
										}
									}  else {
										splitNodeDestCount = 0;
										splitNodeSrcCount = 0;
										splitNodeDestAsteriskFound = false;
										splitNodeSrcAsteriskFound = false;
										/*
										 * 	case(3)	ZOS22.OMVS.*.DEV to OMVS.&RESVOL2.*.DEV
										 * 			splitDest[0] = ZOS22.OMVS.*.DEV
										 * 			splitDest[1] = OMVS.RESVOL2.*.DEV
										 *						 = ZOS22.OMVS.A000.DEV  to OMVS.RESVOL2.A000.DEV
										 */
										for(i=0; i< splitNodeDest.length; i++){
											if (splitNodeDest[i].contains("*")) {
												splitNodeDestCount = i;
												splitNodeDestAsteriskFound = true;
											}
								        }
										for(i=0; i< splitNodeSrc.length; i++){
											if (splitNodeSrc[i].contains("*")) {
												splitNodeSrcCount = i;
												splitNodeSrcAsteriskFound = true;
											}
								        }
										if ((splitNodeDestAsteriskFound) && (splitNodeSrcAsteriskFound)) {
											/** 
											 *  sourceDataset			 splitNodeSrc		splitNodeDest
											 *  sourceNodes
											 *  ZOS22.OMVS.A000.DEV     ZOS22.OMVS.*.DEV   	OMVS.RESVOL2.*.DEV 
											 * 
											 * 	splitDest[1] = OMVS.RESVOL2.*.DEV
											 */
											System.out.println("((splitNodeDestAsteriskFound) && (splitNodeSrcAsteriskFound))" );
											
											if (splitNodeDest[splitNodeDestCount].length() == 1) {
												toDataset = splitDest[1].replaceAll("\\*", sourceNodes[splitNodeSrcCount]);
											} else {
												toDataset = splitNodeDest[0];
												for(i=1; i< splitNodeDest.length; i++){
													if (splitNodeDest[i].contains("*")) {
														toDataset = toDataset + "." + sourceNodes[splitNodeSrcCount];
													} else {
														toDataset = toDataset + "." + splitNodeDest[i];
													}
												}
									        }
										}
									}
								}
							}
						}
					}
				}
				
				if (!hasDatasetBeenProcessed) {
					try {
						if (members.equalsIgnoreCase("uss")) {
							System.out.println("Invoking DFDSS to copy " + sourceDataset + " to " + toDataset + " on volume " + zosVariables.getProperty(copyVol).trim());
							
							ZFile zFileOut = null;
							
				            zFileOut = new ZFile("//DD:" + pdsDD + "(DSS" + dfdssCount + ")", "wb,type=record,noseek");
				            byte[] recBuf = ZosUtilities.DFDSS_DATASET.getBytes();
				            zFileOut.write(recBuf);
				            
				            recBuf = ZosUtilities.padRight("           INCLUDE(" + sourceDataset + ".**))         -",80).getBytes();
				            zFileOut.write(recBuf);
				            
				            recBuf = ZosUtilities.DFDSS_ALLDATA.getBytes();
				            zFileOut.write(recBuf);
				            recBuf = ZosUtilities.DFDSS_TOLENQF.getBytes();
				            zFileOut.write(recBuf);
				            recBuf = ZosUtilities.DFDSS_RENAMEU_S.getBytes();
				            zFileOut.write(recBuf);
				            
				            recBuf = ZosUtilities.padRight("            (" + sourceDataset + ",           -",80).getBytes();
				            zFileOut.write(recBuf);
				            if (toDataset.contains(copyVol)) {
				            	toDataset.replaceAll(copyVol, zosVariables.getProperty(copyVol).trim());
				            	System.out.println(" replacing " + copyVol + " with " + zosVariables.getProperty(copyVol).trim());
				            }
				            recBuf = ZosUtilities.padRight("            " + toDataset + "),               -",80).getBytes();
				            zFileOut.write(recBuf);
				            
				            recBuf = ZosUtilities.DFDSS_RENAMEU_E.getBytes();
				            zFileOut.write(recBuf);
				            recBuf = ZosUtilities.padRight("    OUTDYNAM(" + zosVariables.getProperty(copyVol).trim() + ")  - ",80).getBytes();
				            zFileOut.write(recBuf);
				            recBuf = ZosUtilities.DFDSS_CATALOG.getBytes();
				            zFileOut.write(recBuf);
				            zFileOut.close();
				            dfdssCount++;
				            /*
				             * 
				             */
						} else { 
							if (members.equals("*")) {
								System.out.println("Copying members " + members + " from PDS " + sourceDataset + " to " + toDataset + "." + zosVariables.getProperty(copyVol).trim() + " with " + pdsDSN + "(COPYALL) on volume " + zosVariables.getProperty(copyVol).trim());
								ExecProgram.iebcopy(new String[] {sourceDataset, toDataset + "." + zosVariables.getProperty(copyVol).trim(), pdsDSN + "(COPYALL)", zosVariables.getProperty(copyVol).trim(),zosVariables.getProperty("SYSDA").trim() });
							} else {
								
								splitMembers = members.split("\\.");
								pdsMember = splitMembers[0].toUpperCase();
					            zFilePDS = new ZFile("//DD:" + pdsDD + "(" + pdsMember + ")", "wb,type=record,noseek");
								r2File = new File(args[0] + "/" + members);
								r2 = new BufferedReader(new FileReader(r2File));
								byte[] recBuf = ZosUtilities.COPYCMD.getBytes();
								zFilePDS.write(recBuf);
						        while ((exline = r2.readLine()) != null) {
						            recBuf = ZosUtilities.padRight(" S M=(" + exline.trim() + ")",80).getBytes();
						            zFilePDS.write(recBuf);
						        }
						        zFilePDS.close();
					            rc = ExecProgram.iebcopy(new String[] {sourceDataset, toDataset + "." + zosVariables.getProperty(copyVol).trim(), pdsDSN + "(" + pdsMember + ")", zosVariables.getProperty(copyVol).trim(), zosVariables.getProperty("SYSDA").trim()});
					            if (rc > maxRC) {
					            	maxRC = rc;
					            }
							}
						}
					} catch (Exception ex) {
						System.out.println(ex.toString());
					}
				}
			}
			System.out.println("processed " + datasetCount + " datasets");
			r1List.close();

			if (maxRC > 0) {
				System.exit(maxRC);
			}

		} catch (Exception rce) {
			System.out.println(rce.toString());
			//throw rce;
		} finally {
			ZFile.bpxwdyn("free fi(" + pdsDD + ") msg(2)");
		}
	}
}
