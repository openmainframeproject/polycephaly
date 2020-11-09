VSAMINIT TITLE 'VSAM ROUTINE TO INITIALIZE VSAM KSDS, RRDS, ESDS'               
VSAMINIT CSECT                                                                  
* ***************************************************************               
*                  V-S-A-M-I-N-I-T                              *               
* ***************************************************************               
*                                                               *               
*     DISCLAIMER:  THIS CODE IS PROVIDED WITHOUT CHARGE. THERE  *               
*          ARE NO WARRANTIES PROVIDED WITH THE CODE AND ITS     *               
*          USE IS SOLELY THE RESPONSIBILITY OF THE USER.        *               
*                                                               *               
* ***************************************************************               
*                                                               *               
*     PROGRAM WHICH WILL INITIALIZE A VSAM CLUSTER              *               
*          - KSDS, RRDS OR ESDS BECAUSE THEY CAN NOT BE OPENED  *               
*            FOR INPUT OR UPDATE UNTIL THEY ARE INITIALIZED.    *               
*          - LOAD A RECORD (SEQUENTIAL LOAD MODE) AND           *               
*          - (OPTIONALLY) DELETE IT. THIS ALLOWS THE VSAM FILE  *               
*             (EXCEPT ESDS) TO BE EMPTY BUT ABLE TO BE          *               
*             PROCESSED IN OTHER THAN LOAD MODE.                *               
*               KSDS AND RRDS CAN BE DELETED                    *               
*               ESDS FLAGGED AS DELETED (X'FF' IN FIRST BYTE)   *               
*          - THE INIT CONTROL CARD IS RECUIRED.  ONLY ONE       *               
*            VALUE FOR KEY-CHARCATER AND FILL-CHARACTER ARE     *               
*            ALLOWED FOR EACH SET OF VSAM FILES. THE VSAM FILES *               
*            ARE CODED AS DD-CARDS WITH DDNAMES WHICH BEGIN     *               
*            WITH THE 4-CHARACTERS "VSAM".                      *               
*          - UP TO 500 FILES CAN BE INITIALIZED IN A RUN        *               
*                                                                               
*      //*     SAMPLE JCL FOR VSAMINIT                                          
*      //*                                                                      
*      //INIT01 EXEC PGM=VSAMINIT                                               
*      //STEPLIB  DD  DISP=SHR,DSN=your-loadlib                                 
*      //VSAM0001 DD  DISP=SHR,DSN=vsam-dsname-1                                
*      //VSAM0002 DD  DISP=SHR,DSN=vsam-dsname-2                                
*      //SYSIN    DD  *                                                         
*      INIT KEY-CHARACTER=C' '  FILL-CHARACTER=C'9'  DELETE=YES                 
*      //SYSPRINT DD  SYSOUT=*                                                  
*      //SYSUDUMP DD  SYSOUT=*                                                  
*      //*                                                                      
*                                                               *               
*     //STEP  EXEC PGM=VSAMINIT                                 *               
*     //SYSPRINT DD SYSOUT=*                 (LOG AND REPORT)   *               
*     //VSAMXXXX DD DSN=VSAM-DATASET         (VSAM FILE(S))     *               
*     //VSAMYYYY DD DSN=VSAM-DATASET         (VSAM FILE(S))     *               
*     //VSAM.... DD DSN=VSAM-DATASET         (VSAM FILE(S))     *               
*     //SYSIN    DD *                        (COMMANDS)         *               
*     INIT KEY-CHARACTER=C'C'  FILL-CHARACTER=C' '  DELETE=YES  *               
*     /*                                                        *               
*                                                               *               
*     CONTROL CARD OPTIONS     COLS DESCRIPTION                 *               
*        ------------          ---- --------------------------  *               
*        INIT                  1- 4 CONTROL CARD LITERAL        *               
*        BLANK                    5                             *               
*        KEY-CHARACTER=C'      6-21 KEY CHARACTER LITERAL       *               
*     OR KEY-CHARACTER=X'      6-21 KEY HEX-CHAR LITERAL        *               
*        KEY-CHARCATER-VALUE     22 KEY VALUE (1 CHARACTER)     *               
*     OR KEY-HEX-VALUE        22-23 KEY VALUE (2 HEX-CHARS)     *               
*        TRAILING QUOTE          23 IF CHARACTER MODE           *               
*        TRAILING QUOTE          24 IF HEXADECIMAL MODE         *               
*        BLANK                   25                             *               
*        FILL-CHARACTER=C'    26-42 FILL CHARACTER LITERAL      *               
*     OR FILL-CHARACTER=X'    26-42 FILL HEX-CHAR LITERAL       *               
*        FILL-CHARCATER-VALUE    43 FILL VALUE (1 CHARACTER)    *               
*     OR FILL-HEX-VALUE       43-44 FILL VALUE (2 HEX-CHARS)    *               
*        TRAILING QUOTE          44 IF CHARACTER MODE           *               
*        TRAILING QUOTE          45 IF HEXADECIMAL MODE         *               
*        BLANK                   46                             *               
*        DELETE=YES/NO        47-53 DELETE= KEYWORD             *               
*        YES                  54-56 YES FOR DELETE RECORD OPTION*               
*        NO                   54-56 NO  FOR DELETE RECORD OPTION*               
*        BLANK                57-80 IGNORED                     *               
*                                                               *               
* ***************************************************************               
         STM   R14,R12,12(R13)    SAVE REGISTERS                                
         LR    R12,R15            GET BASE REGISTER                             
         USING VSAMINIT,R12       ESTABLISH BASE                                
         LR    R11,R13            SAVE AREA CHAIN                               
         LA    R13,SAVE             "                                           
         USING SAVE,R13           ESTABLISH SECOND BASE                         
         ST    R11,4(R13)           "                                           
         ST    R13,8(R11)           "                                           
********************************************************************            
         B     NOPARM             parm is not working, skip it.                 
         L     R8,0(R1)           A(PARM POINTER)                               
         L     R8,9(R8)           A(PARM DATA)                                  
         USING PARM,R8            ADDRESS DATA                                  
         CLC   PLENGTH,=X'0000'   ANY DATA                                      
         BE    NOPARM             NO, SKIP IT                                   
         MVC   TRACE(1),PFLAG1    MOVE FLAG                                     
         DROP  R8                                                               
NOPARM   DS    0H                                                               
****************************************PRINT FILE******************            
*                                                                               
         OPEN  (SYSPRINT,(OUTPUT)) OPEN PRINT/LOG FILE                          
*                                                                               
         BAL   R8,GETTIME             GET DATE & TIME                           
*                                                                               
*>>>>>   MVC   TITLE1+67(5),DATE      DATE = YYDDD                              
*>>>>>   MVC   TITLE1+73(6),TIME      TIME = HHMMSS                             
         MVC   TITLE2+67(5),DATE      DATE = YYDDD                              
         MVC   TITLE2+73(6),TIME      TIME = HHMMSS                             
*                                                                               
         PUT   SYSPRINT,TITLE0        WRITE HEADER                              
         PUT   SYSPRINT,TITLE1        WRITE HEADER                              
         PUT   SYSPRINT,TITLE2        WRITE HEADER                              
*                                                                               
****************************************GET DDNAMES*****************            
*                                                                               
         L     R2,=A(DDNAMES)     GET LIST OF DDNAMES                           
         USING DDTAB,R2                                                         
         SR    R4,R4              COUNT OF DDNAMES                              
         L     R5,16(R0)          A(CVT)                                        
         L     R5,0(R5)           A(TCB WORDS)                                  
         L     R5,4(R5)           A(CURRENT TCB)                                
         L     R5,12(R5)          A(TIOT)                                       
         LA    R5,00(R5)          A(TIOT)                                       
         LA    R5,24(R5)          A(DD-ENTRIES)                                 
DDLOOP   CLC   4(4,R5),=C'VSAM'   GOOD NAME                                     
         BNE   DDLOOP1            NO                                            
         MVC   DDNAME(8),4(R5)    MOVE DDNAME TO TABLE                          
*                                                                               
         MVC   VSAMACB+40(8),0(R2)  DDNAME TO ACB                               
         MVC   DCB+40(8),0(R2)      DDNAME TO DCB                               
*                                                                               
         RDJFCB (DCB,(INPUT))      READ THE JFCB                                
*                                                                               
         LTR   R15,R15                                                          
         BNZ   NOJFCB                                                           
         MVC   DDDSNAM(44),JFCB                                                 
NOJFCB   DS    0H                                                               
*                                                                               
         OPEN  (VSAMACB,(OUTPUT))                                               
*                                                                               
TRYKSDS  TESTCB ACB=VSAMACB,ATRB=KSDS,ERET=TRYRRDS                              
*                                                                               
         BE    ISKSDS                                                           
         B     TRYRRDS                                                          
ISKSDS   MVC   DDTYPE,=C'KSDS'                                                  
         CLOSE (VSAMACB,)                                                       
         B     TESTX                                                            
*                                                                               
TRYRRDS  TESTCB ACB=VSAMACB,ATRB=RRDS,ERET=TRYRRDSV                             
*                                                                               
         BE    ISRRDS                                                           
         B     TRYRRDSV                                                         
ISRRDS   MVC   DDTYPE,=C'RRDS'                                                  
         CLOSE (VSAMACB,)                                                       
         B     TESTX                                                            
*                                                                               
TRYRRDSV TESTCB ACB=VSAMACB,ATRB=VRRDS,ERET=TRYESDS                             
*                                                                               
         BE    ISRRDSV                                                          
         B     TRYESDS                                                          
ISRRDSV  MVC   DDTYPE,=C'RRDV'                                                  
         CLOSE (VSAMACB,)                                                       
         B     TESTX                                                            
*                                                                               
TRYESDS  TESTCB ACB=VSAMACB,ATRB=ESDS,ERET=NOTVSAM                              
*                                                                               
         BE    ISESDS                                                           
         B     NOTVSAM                                                          
ISESDS   MVC   DDTYPE,=C'ESDS'                                                  
         CLOSE (VSAMACB,)                                                       
         B     TESTX                                                            
NOTVSAM  DS    0H                                                               
         MVC   RETCODE,=F'4'          SET RC = 4                                
         MVC   DDSTATUS(8),=C'NOT-VSAM' NON-VSAM                                
         CVD   R15,WORK               GET RC FROM TEST                          
         OI    WORK+7,X'0F'           SIGN IS +                                 
         UNPK  MSG004+46(3),WORK+6(2) PUT IT IN MESSAGE                         
*                                                                               
         CLOSE (VSAMACB,)                                                       
*                                                                               
         MVC   MSG004+16(8),0(R2)                                               
         MVC   DDNAME,=C'        '                                              
*                                                                               
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSG004+67(5),DATE      DATE = YYDDD                              
         MVC   MSG004+73(6),TIME      TIME = HHMMSS                             
*                                                                               
         PUT   SYSPRINT,MSG004                                                  
         B     DDLOOP1                                                          
TESTX    LA    R4,1(R4)                                                         
*                                                                               
         LA    R2,DDTABL(R2)      NEXT SLOT                                     
DDLOOP1  SR    R3,R3              CLEAR R3                                      
         IC    R3,0(R5)           L'TIOT ENTRY                                  
         AR    R5,R3              POINT AT NEXT ENTRY                           
         CLI   0(R5),X'00'        AT END OF TIOT                                
         BNE   DDLOOP             NO, PROCESS ENTRY                             
         LTR   R4,R4              ANY NAMES IN TABLE                            
         BNZ   DDLOOP2            YES                                           
         ABEND 1,DUMP             NO, JUST ABEND HERE                           
DDLOOP2  DS    0H                 YES, KEEP GOING                               
         SPACE 2                                                                
****************************************INPUT RECORD****************            
***      NOW GET THE INPUT RECORD                                 **            
********************************************************************            
         OPEN  (SYSIN,(INPUT))    OPEN INPUT FILE                               
         GET   SYSIN,REQUEST      GRAB A REQUEST RECORD                         
         CLOSE (SYSIN,)            CLOSE SYSIN                                  
         CLC   OPCODE(4),=C'INIT'                                               
         BE    SYSIN1                                                           
*                                                                               
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSG001+67(5),DATE      DATE = YYDDD                              
         MVC   MSG001+73(6),TIME      TIME = HHMMSS                             
*                                                                               
         PUT   SYSPRINT,MSG001                                                  
EODAD    MVC   RETCODE,=F'16'      INVALID INPUT RECORD                         
         B     DONE                                                             
SYSIN1   DS    0H                                                               
*                                                                               
*        EDIT THE INPUT RECORD...GET KEY-CHAR AND FILL-CHAR AS 1-BYTE           
*                                                                               
SYSIN2X  CLI   KEYCHART,C'X'       HEX INPUT                                    
         BNE   SYSIN2C             NO                                           
         LA    R15,KEYVAL1         YES, A(KEY CHARS)                            
         BAL   R14,TESTHEX              TEST THEM                               
         B     SYSIN2OK            RETURN TO 0(R14)                             
         B     SYSIN2XX            RETURN TO 4(R14)                             
SYSIN2C  CLI   KEYCHART,C'C'       CHAR INPUT                                   
         BE    SYSIN2CC            YES                                          
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSG001A+67(5),DATE      DATE = YYDDD                             
         MVC   MSG001A+73(6),TIME      TIME = HHMMSS                            
*                                                                               
         PUT   SYSPRINT,MSG001A    ERROR                                        
         B     EODAD               EXIT                                         
SYSIN2XX DS    0H                                                               
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSG001B+67(5),DATE      DATE = YYDDD                             
         MVC   MSG001B+73(6),TIME      TIME = HHMMSS                            
*                                                                               
         PUT   SYSPRINT,MSG001B    ERROR                                        
         B     EODAD               EXIT                                         
SYSIN2OK MVC   KEYBYTE(1),WORK     HEX IS OK                                    
         B     SYSIN3X                                                          
SYSIN2CC MVC   KEYBYTE,KEYVAL1     CHAR IS OK                                   
*                                                                               
*        AND THE FILL CHARACTER                                                 
*                                                                               
SYSIN3X  CLI   FILCHART,C'X'       HEX INPUT                                    
         BNE   SYSIN3C             NO                                           
         LA    R15,FILVAL1         YES, A(KEY CHARS)                            
         BAL   R14,TESTHEX              TEST THEM                               
         B     SYSIN3OK            RETURN TO 0(R14)                             
         B     SYSIN3XX            RETURN TO 4(R14)                             
SYSIN3C  CLI   FILCHART,C'C'       CHAR INPUT                                   
         BE    SYSIN3CC            YES                                          
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSG001C+67(5),DATE      DATE = YYDDD                             
         MVC   MSG001C+73(6),TIME      TIME = HHMMSS                            
*                                                                               
         PUT   SYSPRINT,MSG001C    ERROR                                        
         B     EODAD               EXIT                                         
SYSIN3XX DS    0H                                                               
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSG001D+67(5),DATE      DATE = YYDDD                             
         MVC   MSG001D+73(6),TIME      TIME = HHMMSS                            
*                                                                               
         PUT   SYSPRINT,MSG001D    ERROR                                        
         B     EODAD               EXIT                                         
SYSIN3OK MVC   FILLBYTE(1),WORK    HEX IS OK                                    
         B     SYSIN4                                                           
SYSIN3CC MVC   FILLBYTE,KEYVAL1    CHAR IS OK                                   
*                                                                               
*        CHECK THE DELETE OPTION                                                
*                                                                               
SYSIN4   DS    0H                                                               
*                                                                               
         CLC   DELVAL,=C'YES'                                                   
         BE    SYSIN5                                                           
         CLC   DELVAL,=C'NO '                                                   
         BE    SYSIN5                                                           
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSG001E+67(5),DATE      DATE = YYDDD                             
         MVC   MSG001E+73(6),TIME      TIME = HHMMSS                            
*                                                                               
         PUT   SYSPRINT,MSG001E    ERROR                                        
         B     EODAD               EXIT                                         
*                                                                               
SYSIN5   DS    0H                                                               
*                                                                               
*        START LOOP THROUGH THE FILE                                            
*                                                                               
********************************************************************            
***      PROCESS THE FILES                                        **            
********************************************************************            
*                                                                               
         L     R2,=A(DDNAMES)         A(DDNAME TABLE)                           
         B     TESTFILE                                                         
NEXTFILE DS    0H                                                               
         LA    R2,DDTABL(R2)          NEXT ENTRY                                
TESTFILE CLC   DDNAME,=CL8'        '  ALL DONE                                  
         BE    DONE                   YES, EXIT PROGRAM                         
*                                                                               
         MVC   MSG002+25(8),DDNAME    DDNAME TO MESSAGE                         
         MVC   MSG002+35(4),DDTYPE    DDNAME TO MESSAGE                         
*                                                                               
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSG002+67(5),DATE      DATE = YYDDD                              
         MVC   MSG002+73(6),TIME      TIME = HHMMSS                             
*                                                                               
         PUT   SYSPRINT,MSG002        INDICATE FILE TO PROCESS                  
*                                                                               
         MVC   MSG003+25(44),=CL44' '                                           
         CLI   DDDSNAM,C' '                                                     
         BE    NODSNAME                                                         
         MVC   MSG003+25(38),DDDSNAM  DSNAME TO MESSAGE                         
*                                                                               
         PUT   SYSPRINT,MSG003        INDICATE DSNAME OF FILE                   
*                                                                               
NODSNAME DS    0H                                                               
*                                                                               
         MVC   VSAMACB+40(8),DDNAME   DDNAME TO ACB                             
*                                                                               
         MODCB ACB=VSAMACB,STRNO=1    LOAD MODE REQUIREMENT                     
*                                                                               
         OPEN  (VSAMACB,(OUTPUT))     OPEN LOAD ACB                             
*                                                                               
         ST    R15,ERR15              SAVE RETURN CODE                          
         XC    FDBACK(4),FDBACK       ZERO FEEDBACK CODE                        
         LTR   R15,R15                CHECK RETURN CODE                         
         BZ    OPENOKL                BRANCH ON OK                              
         MVI   ERROR,C'Y'             SET ERROR FLAG                            
         LA    R7,MSG011              POINT AT MESSAGE                          
         BAL   R8,SHOWERR             GET DATA FOR MESSAGE                      
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSG011+67(5),DATE      DATE = YYDDD                              
         MVC   MSG011+73(6),TIME      TIME = HHMMSS                             
*                                                                               
         PUT   SYSPRINT,MSG011        DISPLAY ERROR                             
         CLC   RETCODE(4),=F'8'       IS RC ALREADY 8 OR MORE                   
         BNL   NEXTFILE               YES, KEEP RC                              
         MVC   RETCODE,=F'8'          SET RC = 8                                
         B     NEXTFILE               GET OUT, IN SAD SHAPE                     
OPENOKL  DS    0H                                                               
         MVI   OPENFLAG,C'Y'          ACB OPEN                                  
*                                                                               
*        FILL THE KEY AND RECORD AREA                                           
*                                                                               
         MVC   KEY(1),KEYBYTE       KEY CHAR                                    
         MVC   KEY+1(255),KEY        TO FULL KEY                                
         L     R14,=A(RECORD)       THEN                                        
         MVC   0(1,R14),FILLBYTE    FILL CHAR                                   
         MVC   1(255,R14),0(R14)    TO FULL RECORD                              
         LA    R15,256(R14)          PROPAGATE                                  
         LA    R1,127                 THE                                       
RECORDX  MVC   0(256,R15),0(R14)       FILL-CHAR                                
         LA    R14,256(R14)             THROUGHOUT                              
         LA    R15,256(R15)              THE                                    
         BCT   R1,RECORDX                 RECORD                                
*                                                                               
*        GET THE ATTRIBUTES OF THIS VSAM DATASET                                
*                                                                               
         SHOWCB ACB=VSAMACB,AREA=VSAMDATA,                             X        
               FIELDS=(KEYLEN,LRECL,RKP),LENGTH=12                              
*                                                                               
         L     R4,VSAMKEYL            GET KEY-LENGTH                            
         L     R5,VSAMLREC                LRECL                                 
         L     R6,VSAMRKP                 RKP                                   
*                                                                               
*        PUT REAL VALUES IN THE RPL(S)                                          
*                                                                               
         MODCB RPL=RPLLODK,KEYLEN=(4),RECLEN=(5),AREALEN=(5)                    
*                                                                               
         MODCB RPL=RPLLODS,RECLEN=(5),AREALEN=(5)                               
*                                                                               
         MODCB RPL=RPLDIR,RECLEN=(5),AREALEN=(5),KEYLEN=(4)                     
*                                                                               
*        DETERMINE IF THE KEY IS IMBEDDED IN THE DATA RECORD                    
*                                                                               
         CLC   VSAMKEYL(4),=F'0'      KEYED RECORD                              
         BE    NOKEY                  NO                                        
         CLC   DDTYPE(4),=C'RRDV'     RRDS (VARIABLE)                           
         BE    NOKEY                  NO                                        
         L     R15,=A(RECORD)         A(RECORD)                                 
         L     R14,VSAMKEYL           L(KEY)                                    
         BCTR  R14,R0                 L(KEY) - 1                                
         A     R15,VSAMRKP            START OF KEY                              
         EX    R14,STUFFKEY           STUFF KEY INTO RECORD                     
STUFFKEY MVC   0(0,R15),KEY           MOVE THE KEY VALUE                        
         B     KEYSDONE                                                         
NOKEY    DS    0H                                                               
         MVC   RECNO(4),=F'1'         RECORD NUMBER = 1                         
KEYSDONE DS    0H                                                               
         CLC   DDTYPE(4),=C'ESDS'     ESDS AND DELETE = LOGICAL                 
         BNE   KEYSDONX               YES, DO DELETE CODE                       
         CLC   DELVAL(3),=C'YES'      DELETE=YES ON CONTROL CARD                
         BNE   KEYSDONX               YES, DO DELETE CODE                       
         L     R15,=A(RECORD)              A(RECORD)                            
         MVI   0(R15),X'FF'                LOGICALLY FOR ESDS                   
KEYSDONX DS    0H                                                               
*                                                                               
         BAL   R8,SEQMODE             SET SEQUENTIAL MODE                       
         BAL   R8,PUTMODE             SET ACB FOR WRITING                       
*                                                                               
         CLC   DDTYPE,=C'ESDS'        ESDS LOADED SEQUENTIALLY                  
         BE    PUTLODS                     YES                                  
*                                                                               
         PUT   RPL=RPLLODK            WRITE THE KSDS, RRDS RECORD               
*                                                                               
         B     PUTEXIT                                                          
*                                                                               
PUTLODS  PUT   RPL=RPLLODS            WRITE THE ESDS RECORD                     
*                                                                               
PUTEXIT  DS    0H                                                               
*                                                                               
         ST    R15,ERR15              SAVE RETURN CODE                          
         LTR   R15,R15                CHECK THE RETURN CODE                     
         BZ    DELETIT                BRANCH ON OK                              
         MVI   ERROR,C'Y'             SET ERROR FLAG                            
*                                                                               
         CLC   DDTYPE,=C'ESDS'        ESDS LOADED SEQUENTIALLY                  
         BE    SHOWLODS                    YES                                  
*                                                                               
         SHOWCB RPL=RPLLODK,          GRAB INFO. FOR KSDS, RRDV        X        
               AREA=FDBACK,LENGTH=4,FIELDS=FDBK                                 
*                                                                               
         B     SHOWEXIT                                                         
*                                                                               
SHOWLODS SHOWCB RPL=RPLLODS,          GRAB INFO. FOR ESDS, RRDS        X        
               AREA=FDBACK,LENGTH=4,FIELDS=FDBK                                 
*                                                                               
SHOWEXIT DS    0H                                                               
*                                                                               
         LA    R7,MSG005              ADDRESS OF MESSAGE                        
         BAL   R8,SHOWERR             GET DATA FOR MESSAGE                      
*                                                                               
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSG005+67(5),DATE      DATE = YYDDD                              
         MVC   MSG005+73(6),TIME      TIME = HHMMSS                             
*                                                                               
         PUT   SYSPRINT,MSG005        INDICATE WRITE ERROR                      
*                                                                               
         B     DISPI                  CLEAN UP AND LOOP                         
*                                                                               
DELETIT  DS    0H                                                               
*                                                                               
         CLC   DDTYPE,=C'ESDS'        ESDS CAN'T BE DELETED                     
         BE    DISPI                       YES                                  
*                                                                               
*        WAS THE DELETE OPTION TURNED ON                                        
*                                                                               
         CLC   DELVAL(3),=C'YES'      DELETE=YES ON CONTROL CARD                
         BE    DELETE                 YES, DO DELETE CODE                       
*                                                                               
         B     DISPI                  CLEAN UP AND LOOP                         
*                                                                               
*        DELETE RECORD JUST INSERTED IN VSAM FILE                               
*                                                                               
DELETE   EQU   *                                                                
*                                                                               
         CLOSE (VSAMACB,)             CLOSE LOAD MODE                           
*                                                                               
         OPEN  (VSAMACB,(UPDAT))      OPEN UPDATE MODE                          
*                                                                               
         CLC   DDTYPE,=C'RRDS'        RRDS                                      
         BE    SETRECNO                OR                                       
         CLC   DDTYPE,=C'RRDV'        RRDS (VARIABLE)                           
         BE    SETRECNO                => USE RECNO FOR KEY                     
*                                                                               
         B     SKPRECNO               KSDS => KEY                               
*                                                                               
SETRECNO MVC   RECNO,=F'1'            RECORD NUMBER 1 FOR RRDS                  
*                                                                               
SKPRECNO BAL   R8,DIRMODE             SET DIRECT MODE                           
*                                                                               
         POINT RPL=RPLDIR             LOCATE RECORD -DIRECT                     
*                                                                               
         ST    R15,ERR15              SAVE RETURN CODE                          
         LTR   R15,R15                CHECK RETURN CODE                         
         BZ    DELETE0                OK, GO ON TO NEXT STEP                    
         MVI   ERROR,C'Y'             SET ERROR FLAG                            
*                                                                               
         SHOWCB RPL=RPLDIR,           GRAB INFO.                       X        
               AREA=FDBACK,LENGTH=4,FIELDS=FDBK                                 
*                                                                               
         CLC   RETCODE(4),=F'8'       IS RC ALREADY 8 OR MORE                   
         BNL   POINTERR               YES, KEEP RC                              
         MVC   RETCODE,=F'8'          SET RC = 8                                
         B     POINTERR               GO SHOW ERROR CONDITION                   
DELETE0  EQU   *                                                                
*                                                                               
         GET   RPL=RPLDIR             FIRST FETCH THE RECORD                    
*                                                                               
         ST    R15,ERR15              SAVE RETURN CODE                          
         LTR   R15,R15                CHECK RETURN CODE                         
         BZ    DELETE1                BRANCH ON OK                              
         MVI   ERROR,C'Y'             SET ERROR FLAG                            
*                                                                               
         SHOWCB RPL=RPLDIR,           GRAB INFO.                       X        
               AREA=FDBACK,LENGTH=4,FIELDS=FDBK                                 
*                                                                               
         CLC   RETCODE(4),=F'8'       IS RC ALREADY 8 OR MORE                   
         BNL   GETITERR               YES, KEEP RC                              
         MVC   RETCODE,=F'8'          SET RC = 8                                
         B     GETITERR                                                         
*                                                                               
DELETE1  EQU   *                                                                
*                                                                               
         ERASE RPL=RPLDIR             ERASE THE RECORD                          
*                                                                               
         ST    R15,ERR15              SAVE RETURN CODE                          
         LTR   R15,R15                CHECK RETURN CODE                         
         BZ    DISPI                  BRANCH ON OK                              
         MVI   ERROR,C'Y'             SET ERROR FLAG                            
*                                                                               
         SHOWCB RPL=RPLDIR,           GRAB INFO.                       X        
               AREA=FDBACK,LENGTH=4,FIELDS=FDBK                                 
*                                                                               
         CLC   RETCODE(4),=F'8'       IS RC ALREADY 8 OR MORE                   
         BNL   ERASEERR               YES, KEEP RC                              
         MVC   RETCODE,=F'8'          SET,RC = 8                                
         B     ERRCOMON                                                         
*                                                                               
POINTERR LA    R7,MSG007              ADDRESS OF MESSAGE AREA                   
         B     ERRCOMON                                                         
GETITERR LA    R7,MSG008              ADDRESS OF MESSAGE AREA                   
         B     ERRCOMON                                                         
ERASEERR LA    R7,MSG009              ADDRESS OF MESSAGE AREA                   
ERRCOMON BAL   R8,SHOWERR             GET ERROR INFORMATION                     
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   67(5,R7),DATE          DATE = YYDDD                              
         MVC   73(6,R7),TIME          TIME = HHMMSS                             
*                                                                               
         PUT   SYSPRINT,(R7)          INDICATE ERASE ERROR                      
         B     DISPI                  CLEAN UP AND LOOP                         
*                                                                               
*        TIME TO FINISH UP THE REQUEST                                          
*                                                                               
DISPI    EQU   *                      ALL IS OK HERE FROM WRITE                 
*                                                                               
         CLI   OPENFLAG,C'Y'          ACB OPEN                                  
         BNE   DISPI1                 NO, SKIP CLOSE                            
*                                                                               
         CLOSE (VSAMACB,)             CLOSE DCB                                 
*                                                                               
         MVI   OPENFLAG,C'N'          SET CLOSED FLSG                           
*                                                                               
DISPI1   CLI   ERROR,C'Y'             ERROR                                     
         BE    DISPI2                 YES, SKIP OK MESSAGE                      
*                                                                               
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSG010+67(5),DATE      DATE = YYDDD                              
         MVC   MSG010+73(6),TIME      TIME = HHMMSS                             
*                                                                               
         PUT   SYSPRINT,MSG010        INDICATE INITIALIZE OK                    
*                                                                               
         MVC   DDSTATUS(8),=C'OK      '                                         
         B     NEXTFILE               GO TRY AGAIN                              
*                                                                               
DISPI2   MVI   ERROR,C'N'             ERROR = NO                                
         MVC   DDSTATUS(8),=C'INIT-ERR'                                         
         B     NEXTFILE               GO TRY NEXT                               
*                                                                               
DONE     EQU   *                      EODAD SHOULD NOT HAPPEN                   
*                                                                               
         BAL   R8,GETTIME             GET DATE & TIME                           
         MVC   MSGOK+67(5),DATE       DATE = YYDDD                              
         MVC   MSGOK+73(6),TIME       TIME = HHMMSS                             
*                                                                               
         L     R15,RETCODE            GET RC                                    
         CVD   R15,WORK               PACKED DECIMAL                            
         OI    WORK+7,X'0F'           + SIGN                                    
         UNPK  MSGOK+33(3),WORK+6(2)  PUT IN MSG AS NNN                         
*                                                                               
         PUT   SYSPRINT,MSGOK         FINAL MESSAGE                             
*                                                                               
         CLOSE (SYSIN,,SYSPRINT,)     CLOSE FILES                               
*                                                                               
         L     R13,4(R13)             A(OLD SA)                                 
         L     R14,12(R13)            R14                                       
         L     R15,RETCODE            R15                                       
         LM    R0,R12,20(R13)         R0 - R12                                  
         BR    R14                    RETURN                                    
*                                                                               
********************************************************************            
*        CODE TO FETCH TIME AND DATE VALUES                        *            
********************************************************************            
GETTIME  DS    0H                                                               
*                                                                               
         TIME  DEC                    GET TIME AND DATE                         
*                                                                               
         STM   R0,R1,TIMEDATE         SAVE TIME/DATE                            
         OI    TIMEDATE+3,X'0F'       GOOD SIGN IN IT                           
         UNPK  DATE(5),TIMEDATE+5(3)  GET DATE AS YYDDD                         
         UNPK  TIMEWORK(7),TIMEDATE+0(4)  GET TIME AS HHMMSST                   
         MVC   TIME(6),TIMEWORK       SAVE JUST WHAT IS NEEDED                  
         BR    R8                                                               
*                                                                               
********************************************************************            
*        CODE TO EDIT 2-HEX DIGITS TO REPRESENT A CHARACTER        *            
********************************************************************            
TESTHEX  CLI   0(R15),C'9'         VALIDATE CHAR                                
         BH    4(R14)              ERROR                                        
         CLI   0(R15),C'0'         VALIDATE CHAR                                
         BNL   TESTHEX4                                                         
         CLI   0(R15),C'F'         VALIDATE CHAR                                
         BH    4(R14)              ERROR                                        
         CLI   0(R15),C'A'         VALIDATE CHAR                                
         BL    4(R14)              ERROR                                        
TESTHEX4 CLI   1(R15),C'9'         VALIDATE CHAR                                
         BH    4(R14)              ERROR                                        
         CLI   1(R15),C'0'         VALIDATE CHAR                                
         BNL   TESTHEX9                                                         
         CLI   1(R15),C'F'         VALIDATE CHAR                                
         BH    4(R14)              ERROR                                        
         CLI   1(R15),C'A'         VALIDATE CHAR                                
         BL    4(R14)              ERROR                                        
TESTHEX9 TR    0(2,R15),TRTAB-193  CHANGE VALUES TO PACK THEM                   
         PACK  WORK(2),0(3,R15)    PACK 2-HEX DIGITS TO 1-BYTE                  
         B     0(R14)              RETURN OK                                    
*******************************************************************             
*                                                                               
SEQMODE  MODCB ACB=VSAMACB,MACRF=(SEQ)           RESET FOR SEQUENTIAL           
         BR    R8                                RETURN                         
*                                                                               
DIRMODE  MODCB ACB=VSAMACB,MACRF=(DIR,KEY,OUT)   RESET FOR DIRECT               
         BR    R8                                RETURN                         
*                                                                               
PUTMODE  MODCB ACB=VSAMACB,MACRF=(OUT)           RESET FOR OUTPUT               
         BR    R8                                RETURN                         
*                                                                               
SHOWERR  EQU   *                                                                
*                                                                               
*        R7=START OF MSG AREA; R8=RETURN ADDRESS                                
*        ERR15=RETURN CODE   ; FDBACK=FEEDBACK KEY                              
*                                                                               
         L     R1,ERR15               GET RETURN CODE                           
         CVD   R1,WORK                MAKE IT PACKED-DECIMAL                    
         OI    WORK+7,X'0F'           SIGNED WITH A KISS                        
         UNPK  33(3,R7),WORK+6(2)     ZONED DECIMAL TO PRINT                    
         L     R1,FDBACK              GET FEEDBACK CODE                         
         CVD   R1,WORK                MAKE IT PACKED-DECIMAL                    
         OI    WORK+7,X'0F'           SIGNED WITH A KISS                        
         UNPK  42(3,R7),WORK+6(2)     ZONED DECIMAL TO PRINT                    
         BR    R8                     RETURN                                    
         EJECT                                                                  
*                                                                               
******************DATA CONSTANTS AND AREAS**************************            
ERR15    DC    F'0'                   RETURN CODE                               
RETCODE  DC    F'0'                   RETURN CODE FROM STEP                     
WORK     DC    D'0'                   WORK AREA FOR CONVERSIONS                 
FDBACK   DC    F'0'                   FEEDBACK CODE AREA                        
TIMEDATE DC    D'0'                   WORK AREA FOR 00YYDDDFHHMMSSTH            
TIMEWORK DC    CL7' '                 WORK AREA FOR TIME AS HHMMSST             
ERROR    DC    CL1'N'                 ERROR FLAG                                
TIME     DC    CL6' '                 WORK AREA FOR TIME HH:MM:SS               
DATE     DC    CL5' '                 WORK AREA FOR DATE YY.DDD                 
OPENFLAG DC    CL1'N'                 SET FLAG FOR DCB NOT OPEN                 
KEYBYTE  DC    X'00'                  KEY CHARACTER VALUE                       
FILLBYTE DC    X'00'                  FILL CHARACTER VALUE                      
TRACE    DC    C'N'                   TRACE FLAG                                
RBA      DC    F'0'                   RBA OF RECORD TO ERASE                    
         DS    0F                     ALLIGN KEYFIELD/RECNO FIELD               
KEY      DC    CL256' '               DEFAULT KEY FIELD                         
         ORG   KEY                                                              
RECNO    DC    F'0'                   RECNO FOR RRDS                            
         DS    CL252                  FILLER                                    
************************** TRANSLATE VALID CHARS ONLY **************            
TRTAB    DC    X'AABBCCDDEEFF000000000000000000'   C1 - CF                      
         DC  X'00000000000000000000000000000000'   D0 - DF                      
         DC  X'00000000000000000000000000000000'   E0 - EF                      
         DC  X'00112233445566778899'               F0 - F9                      
************************** SAVE AREA + SECOND BASE *****************            
SAVE     DC    18F'0'                 SAVE AREA                                 
************************** INPUT RECORD AREA ***********************            
REQUEST  DS    0CL80                  REQUEST READ AREA                         
OPCODE   DS    CL4                    REQUEST TYPE = 'INIT'                     
         DS    CL1                                                              
KEYCHAR  DS    CL14                   LITERAL - KEY-CHARACTER=                  
KEYCHART DS    CL1                             C(HAR) OR X(HEX)                 
         DS    CL1                             '                                
KEYVAL1  DS    CL1                             FIRST CHAR                       
KEYVAL2  DS    CL1                             2ND CHAR OR '                    
         DS    CL1                             ' OR BLANK                       
         DS    CL1                                                              
FILCHAR  DS    CL15                   LITERAL - FILL-CHARACTER=                 
FILCHART DS    CL1                             C(HAR) OR X(HEX)                 
         DS    CL1                             '                                
FILVAL1  DS    CL1                             FIRST CHAR                       
FILVAL2  DS    CL1                             2ND CHAR OR '                    
         DS    CL1                             ' OR BLANK                       
         DS    CL1                                                              
DELCHAR  DS    CL7                    LITERAL - DELETE=                         
DELVAL   DS    CL3                             YES OR NO                        
         DS    CL24                                                             
************************** VSAM PARAMETERS *************************            
VSAMDATA DS    0F                     VSAM DATA FROM SHOWCB                     
VSAMKEYL DC    F'0'                      KEY LENGTH                             
VSAMLREC DC    F'0'                      MAXIMUM RECORD LENGTH                  
VSAMRKP  DC    F'0'                      RELATIVE KEY POSITION                  
*                                                                               
TITLE0   DC    CL80' **************************************************X        
               **************** V1.0 4/96 *'                                    
TITLE1   DC    CL80' *************** VSAM FILE INITIALIZATION ROUTINE *X        
               **************  YYDDD HHMMSS'                                    
TITLE2   DC    CL80' --------------------------------------------------X        
               -------------- '                                                 
MSG001   DC    CL80' MSG001- INVALID CONTROL CARD - RUN ABORTED '               
MSG001A  DC    CL80' MSG001- INVALID KEY-CHARACTER TYPE - RUN ABORTED '         
MSG001B  DC    CL80' MSG001- INVALID KEY-CHARACTER - RUN ABORTED '              
MSG001C  DC    CL80' MSG001- INVALID FILL-CHARACTER TYPE - RUN ABORTED'         
MSG001D  DC    CL80' MSG001- INVALID FILL-CHARACTER - RUN ABORTED '             
MSG001E  DC    CL80' MSG001- INVALID DELETE OPTION - RUN ABORTED '              
MSG002   DC    CL80' MSG002- INITIALIZE FILE XXXXXXXX (XXXX) STARTED '          
MSG003   DC    CL80' MSG003-            DSN: '                                  
MSG004   DC    CL80' MSG004- DDNAME XXXXXXXX NOT A VSAM FILE   RC=000 '         
MSG005   DC    CL80' MSG005- PUT RECORD ERROR     RC=000 FDBK=000 '             
MSG007   DC    CL80' MSG007- POINT RECORD ERROR   RC=000 FDBK=000 '             
MSG008   DC    CL80' MSG007- GET RECORD ERROR     RC=000 FDBK=000 '             
MSG009   DC    CL80' MSG007- ERASE RECORD ERROR   RC=000 FDBK=000 '             
MSG010   DC    CL80' MSG010- VSAM FILE WAS INITIALIZED '                        
MSG011   DC    CL80' MSG011- OPEN FILE FAILED     RC=000 FDBK=000 '             
MSGOK    DC    CL80' PROCESSING COMPLETED ------- RC=000.'                      
         EJECT                                                                  
SYSIN    DCB   DDNAME=SYSIN,DSORG=PS,MACRF=GM,EODAD=EODAD,             X        
               RECFM=FB,LRECL=80                                                
*                                                                               
SYSPRINT DCB   DDNAME=SYSPRINT,DSORG=PS,MACRF=PM,                      X        
               RECFM=FB,LRECL=79,BLKSIZE=79                                     
*                                                                               
DCB      DCB   DDNAME=XXXXX,DSORG=PS,MACRF=GL,EXLST=EXLST                       
*                                                                               
EXLST    DS    0F                                                               
         DC    X'87'            JFCB ENTRY + END-OF-EXLST                       
         DC    AL3(JFCB)        A(JFCB)                                         
*                                                                               
JFCB     DC    22D'0'           176 BYTES                                       
*                                                                               
VSAMACB  ACB   AM=VSAM,                                                X        
               BUFND=5,                                                X        
               BUFNI=2,                                                X        
               DDNAME=VSAMXXXX,                                        X        
               MACRF=(KEY,NDF,DIR,SEQ,NCI,OUT,NFX,NIS,NRM,NSR,NUB),    X        
               STRNO=1                                                          
*                                                                               
*        RPL TO LOAD USING KEYED RECORD (KEY OF RECORD)                         
*                                                                               
RPLLODK  RPL   ACB=VSAMACB,AM=VSAM,AREA=RECORD,ARG=KEY,                X        
               AREALEN=80,KEYLEN=5,RECLEN=80,                          X        
               OPTCD=(KEY,SEQ,SYN,NUP,MVE)                                      
*                                                                               
*        RPL TO LOAD SEQUENTIALLY USING RECORD NUMBER                           
*                                                                               
RPLLODS  RPL   ACB=VSAMACB,AM=VSAM,AREA=RECORD,                        X        
               AREALEN=80,RECLEN=80,                                   X        
               OPTCD=(ADR,SEQ,ARD,FWD,SYN,MVE)                                  
*                                                                               
*        RPL TO DELETE RECORD USING IT(S) KEY OR NUMBER                         
*                                                                               
RPLDIR   RPL   ACB=VSAMACB,AM=VSAM,AREA=RECORD,ARG=KEY,                X        
               AREALEN=80,KEYLEN=4,RECLEN=80,                          X        
               OPTCD=(KEY,DIR,UPD,ARD,FWD,SYN,FKS,KEQ,MVE)                      
*                                                                               
************************** LITERAL POOL ****************************            
         LTORG                                                                  
************************** DDNAME TABLE ****************************            
DDNAMES  DC    500CL68' '             500 DDNAMES + VSAM TYPE                   
************************** DATA AREA *******************************            
         DS    0D                     ALLIGN ON DOUBLEWORD AGAIN                
RECORD   DC    4096XL8'0000000000000000' RECORD READ AREA                       
************************** REGISTER NAMES **************************            
R0       EQU   0                                                                
R1       EQU   1                                                                
R2       EQU   2                                                                
R3       EQU   3                                                                
R4       EQU   4                                                                
R5       EQU   5                                                                
R6       EQU   6                                                                
R7       EQU   7                                                                
R8       EQU   8                                                                
R9       EQU   9                                                                
R10      EQU   10                                                               
R11      EQU   11                                                               
R12      EQU   12                                                               
R13      EQU   13                                                               
R14      EQU   14                                                               
R15      EQU   15                                                               
*                                                                               
*        DEFINITION OF TABLE WITH VSAM DDNAMES AND ATTRIBUTES                   
*                                                                               
DDTAB    DSECT                                                                  
DDNAME   DS    CL8                                                              
DDTYPE   DS    CL4                                                              
DDRECFM  DS    CL2                                                              
DDLRECL  DS    H                                                                
DDDSNAM  DS    CL44                                                             
DDSTATUS DS    CL8                                                              
DDTABL   EQU   *-DDNAME                                                         
*                                                                               
*        DEFINITION OF PARM FIELD                                               
*                                                                               
PARM     DSECT                                                                  
PLENGTH  DS    H                                                                
PFLAG1   DS    CL1                                                              
PFLAG2   DS    CL1                                                              
PREST    DS    CL98                                                             
*                                                                               
         END                                                                    
