//DEFZFS   EXEC PGM=IDCAMS,REGION=8M                        
//SYSPRINT DD  SYSOUT=*                                     
//SYSIN    DD  *                                            
 SET MAXCC=0                                                
 DEFINE CLUSTER (NAME(OMVS.USR.LPP.TOOLS) -                 
                CYLINDERS(1000 50)  -                       
                SHAREOPTIONS(2 3) -                         
                LINEAR -                                    
                )                                           
//*                                                         
//*  FORMAT THE VSAM LINEAR AS A ZFS DATASET                
//FORMZFS  EXEC PGM=IOEAGFMT,REGION=0M,                     
//         PARM=('-aggregate OMVS.USR.LPP.TOOLS -compat')   
//SYSPRINT DD  SYSOUT=*                                     
//STDOUT   DD  SYSOUT=*                                     
