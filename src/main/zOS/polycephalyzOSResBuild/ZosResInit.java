package polycephalyzOSResBuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

import com.ibm.jzos.ZFile;

public class ZosResInit {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static void execute(String[] args) throws Exception {

		InputStream zosPropsFile = null;
		Properties zosProps = new Properties();
		InputStream zosVariablesFile = null;
		Properties zosVariables = new Properties();
		String[] resvol = new String[10];
		String[] resvolUnit = new String[10];
		int i = 0;
		
		if (args.length < 2) {
			System.out.println("USAGE: ZosCopyFiles ConfigurationDirectory PropertyFile");
			System.exit(1);
		}
		
		try {
			/*
			 * args[0] = Jenkins workspace dir / conf
			 * args[1] = zosBuild.zProperties file
			 * 
			 * zosVariables in zosBuild.zProperties is the z/OS variables zProperty file name
			 */
			zosPropsFile = new FileInputStream(args[0] + "/" + args[1]);
			zosProps.load(zosPropsFile);
			
			zosVariablesFile = new FileInputStream(args[0] + "/" + zosProps.getProperty("zosVariables").toString());
			zosVariables.load(zosVariablesFile);
			
			File zosDataInputList = new File(args[0] + "/" + zosProps.getProperty("zosDataInputList").toString());
			if (zosDataInputList.exists()) {
				zosDataInputList.delete();
			}
			
			int resNumberofVolumes = Integer.parseInt(zosVariables.getProperty("ResVolumeNumber"))+1;
			
			//System.out.println("resVolumeNumber = @" + resNumberofVolumes + "@");
			for(i=1; i< resNumberofVolumes; i++) {
				
				resvol[i] = zosVariables.getProperty("ResVolumePrefix").toString() + zosVariables.getProperty("ResVolumeSeq").toString() + i;
				resvolUnit[i] = zosVariables.getProperty(resvol[i] + "unit").toString().trim();
				//System.out.println("resvol[" + i + "] = " + resvol[i] +"@ on device " + resvolUnit[i] + "@");
				zosVariables.setProperty("RESVOL" + i,resvol[i]);
				zosVariables.setProperty("RESVOL" + i+"unit",resvolUnit[i]);
			}
			zosVariables.store(new FileOutputStream(args[0] + "/" + zosProps.getProperty("zosVariables").toString()), null);
			
			/* 
			 * Create the temporary PDS for the IEBCOPY steps
			 * Create member COPYALL with the default COPY statements
			 */
			String pdsDSN = zosVariables.getProperty("TEMPPDS").trim();
			String dl4Vol = zosVariables.getProperty("DL4").trim();
			String copyUnit = zosVariables.getProperty("SYSDA").trim();
			String pdsDD  = "PDSDD";
			ZFile zFileOut = null;
			
			// Allocate the output dataset using BPXWDYN.
			if (ZFile.dsExists("//'" + pdsDSN + "'")) {
				ZFile.bpxwdyn("alloc fi(" + pdsDD + ") da(" + pdsDSN + ") shr msg(wtp)");
			} else {
				ZFile.bpxwdyn("alloc fi(" + pdsDD + ") da(" + pdsDSN + ") new catalog lrecl(80) recfm(F,B) dir(100) dsorg(PO) tracks space(50,5) unit(" + copyUnit + ") vol(" + dl4Vol + ") msg(wtp)");
			}
			
            zFileOut = new ZFile("//DD:" + pdsDD + "(COPYALL)", "wb,type=record,noseek");
            byte[] recBuf = ZosUtilities.COPYCMD.getBytes();
            zFileOut.write(recBuf);
            zFileOut.close();
            ZFile.bpxwdyn("free fi(" + pdsDD + ") msg(2)");
			
		} catch(Exception rce) {
			System.out.println(rce.toString());
			throw rce;
		} finally {
		}
		
		//System.out.println("** configuration property = " + zosProps);
	}

}
