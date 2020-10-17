package com.zos.resbuid;

public class ZosUtilities {
	
	public static String COPYCMD 		= padRight("  COPY INDD=SYSUT1,OUTDD=SYSUT2", 80);
	public static String DFDSS_DATASET 	= padRight("  COPY DATASET(                             - ", 80);
	public static String DFDSS_ALLDATA 	= padRight("       ALLDATA(*)                           - ", 80);
	public static String DFDSS_TOLENQF 	= padRight("       TOL(ENQF)                            - ", 80);
	public static String DFDSS_RENAMEU_S = padRight("   RENAMEU(                                 - ", 80);
	public static String DFDSS_RENAMEU_E = padRight("           )                                - ", 80);
	public static String DFDSS_CATALOG	 = padRight("   CATALOG                                    ", 80);
	
	public static String padRight(String s, int n) {
	     return String.format("%1$-" + n + "s", s);  
	}

	public static String padLeft(String s, int n) {
	    return String.format("%1$" + n + "s", s);  
	}
}
