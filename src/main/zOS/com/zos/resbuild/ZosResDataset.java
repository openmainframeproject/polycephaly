package com.zos.resbuild;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.ibm.jzos.AccessMethodServices;
import com.ibm.jzos.CatalogSearch;
import com.ibm.jzos.ZFile;

public class ZosResDataset {
	/**
	 * Sample program that uses CatalogSearch, LOCATE and OBTAIN to display information about
	 * datasets matching a filter key.  The filter key is given as an argument to main().
	 * <p>
	 * The sample program first uses {@link CatalogSearch} to get a list of datasets matching the supplied
	 * filter key.  Then, for each dataset, {@link ZFile#locateDSN(String)} is used to get the first entry
	 * of the list of MVS volumes that contain the dataset.  Finally {@link ZFile#obtainDSN(String, String)}
	 * is used to obtain the format 1 DSCB information for the dataset.
	 * <p>
	 * If the complete lookup cannot be completed for a dataset (e.g. the volume not being mounted) a message
	 * is written and the dataset is skipped.
	 * <p/>
	 * @since 2.1.0
	 */
	
	
	public static void catSearch(String confDir, String zProperty) throws Exception {
		
		System.out.println("running ZosResDataset search routine");
		
		InputStream zosPropsFile = null;
		Properties zosProps = new Properties();
		File zosFile = null;
	    BufferedReader zosBR = null; 
		BufferedWriter propsWrite = null; 
        String[] dataset = null;
		
		try {
			
			zosPropsFile = new FileInputStream(confDir + zProperty);
			zosProps.load(zosPropsFile);
			
			//System.out.println("** configuration property = " + zosProps);
			
			zosFile = new File(confDir + zosProps.getProperty("zosExclude").toString());
			zosBR = new BufferedReader(new FileReader(zosFile)); 
			
			propsWrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(confDir + zosProps.getProperty("zosDataInputList").toString())));
			
			List<String> excludedDatasets = new ArrayList<String>();
			String exline = null;
			
	        while ((exline = zosBR.readLine()) != null) {
	        	if (exline.isEmpty() || exline.startsWith("/*")) {
	        		// System.out.println("Excluding Datasets found blank line or comment found, skipping");
	        	} else { 
	        		excludedDatasets.add(exline);
	        	}
	        }
	        zosBR.close();
	        
	        /** 
	         * process the z/OS PDS files
	         */
	        zosFile = new File(confDir + zosProps.getProperty("zosDatasets").toString());
	        zosBR = new BufferedReader(new FileReader(zosFile)); 
	        for(String zline; (zline = zosBR.readLine()) != null; ) {
				if (zline.isEmpty() || zline.startsWith("/*")) {
					// System.out.println("Include Datasets found blank line or comment found, skipping");
				} else {
					dataset = zline.trim().split(",");
					listCatalog(dataset, excludedDatasets, propsWrite);
				}
		    }
		    zosBR.close();
		    
	        /** 
	         * process the z/OS zFS files
	         */
		    zosFile = new File(confDir + zosProps.getProperty("zosOMVS").toString());
		    zosBR = new BufferedReader(new FileReader(zosFile)); 
		    for(String ussline; (ussline = zosBR.readLine()) != null; ) {
				if (ussline.isEmpty() || ussline.startsWith("/*")) {
					// System.out.println("Include Datasets found blank line or comment found, skipping");
				} else {
					//System.out.println("processing line=" + line + "@");
					dataset = ussline.trim().split(",");
					listCatalog(dataset, null, propsWrite);
				}
		    }
		    zosBR.close();
		    
		} catch(Exception rce) {
			System.out.println(rce.toString());
			//throw rce;
		} finally {
			propsWrite.flush();
			propsWrite.close();
		}
	}

	private static void listCatalog(String[] dataset, List<String> excludedDatasets, BufferedWriter propsWrite) {
		
		boolean excludedDataset = false;
		String sourceDataset = null;
		String destDataset = null;
		String memberProp = null;
		String resvolProp = null;

    	sourceDataset = dataset[0];
		CatalogSearch catSearch = new CatalogSearch(sourceDataset, 64000);
		try {
			catSearch.search();
			while (catSearch.hasNext()) {
				CatalogSearch.Entry entry = (CatalogSearch.Entry)catSearch.next();
				if (entry.isDatasetEntry()) {
					excludedDataset = false;
					if (excludedDatasets != null ) {
						for (String temp : excludedDatasets) {
							if (entry.getName().contains(temp)) {
								System.out.println("matchFound excluding @" + entry.getName() + "@ because of " + temp + "@");
								excludedDataset = true;
							} 
						} 
					}

					if (!excludedDataset) {
						switch (dataset.length)  {
						case 1:
							//System.out.println("#1 " + dataset.length + " for sourceDataset " + sourceDataset + "@");
							destDataset = "=";
							memberProp	= "*";
							resvolProp 	= "RESVOL1";
							break;
						case 2:
							//System.out.println("#2 " + dataset.length + " for sourceDataset " + sourceDataset + " destDataset " + dataset[1] + "@");
							destDataset = dataset[1];
							memberProp	= "*";
							resvolProp	= "RESVOL1";
							break;
						case 3:
							//System.out.println("#3 " + dataset.length + " for sourceDataset " + sourceDataset + " destDataset " + dataset[1] + " memberProp " + dataset[2] + "@");
							destDataset = dataset[1];
							memberProp 	= dataset[2];
							resvolProp	="RESVOL1";
							break;
						case 4:
							//System.out.println("#4 " + dataset.length + " for sourceDataset " + sourceDataset + " destDataset " + dataset[1] + " memberProp " + dataset[2] + " resvol " + dataset[3] +"@");
							destDataset = dataset[1];
							memberProp 	= dataset[2];
							resvolProp	= dataset[3];
							break;
						default :
							System.out.println("processing case default for dataset");
							/**
							 * error should only be three parms
							 */
							break;
						}
						if (entry.getType() != 'D') {
							propsWrite.write(entry.getName() + "," + destDataset + "," + memberProp + "," + resvolProp + "\n");
							System.out.println(entry.getName() + "," + destDataset + "," + memberProp + "," + resvolProp + " - entry = " + entry.getType());
						}
					}
				}
			}
		} catch (Exception ce) {
			System.out.println(ce.toString());
		}
	}
	

	public static void idcamsListCat(String[] args) throws Exception {
		PrintWriter writer = new PrintWriter(System.out);
		if (args.length < 1) {
			writer.println("USAGE: CatalogSearchSample <filter_key> [entry_types]");
			writer.flush();
			System.exit(1);
		}
		
		String filterKey = args[0].toUpperCase();
		writer.println("Performing Catalog Search with filter key: " + filterKey);
		
		
	    AccessMethodServices ams = new AccessMethodServices();
	    ams.addInputLine("LISTC LEVEL(" + filterKey + ")");
	    int rc = ams.execute();
	    System.out.println("RC = " + rc +" ");
	    System.out.println("IDCAMS output:");
	    System.out.println(ams.getOutputLines());
	}
	
	public static void allocateDataset(String[] args) throws Exception {
	

	    String ddname = ZFile.allocDummyDDName();
	    ZFile.bpxwdyn("alloc fi(" + ddname +
	                  ") da(&&IDTEMP) new delete reuse msg(2)");
	    AccessMethodServices ams = new AccessMethodServices();
	    ams.setOutputDDName(ddname);
	    ams.addInputLine("LISTC LEVEL(BILLING)");
	    int rc = ams.execute();
	    System.out.println("RC = " + rc +" ");
	    ZFile.bpxwdyn("free fi(" + ddname + ") msg(2)");
	}
	
}