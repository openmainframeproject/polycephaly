/* REXX */                                                            
ListType = HWI_LIST_CPCS;                                             
Address BCPII "HWILIST Retcode ConnectToken ListType AnswerArea.      
DiagArea."                                                            
If Retcode = 3841 Then                                                
    Say "HWI_AUTH_FAILURE"                                            
                                                                      
If RC = 0 & retcode = 0 Then                                          
  Do                                                                  
    ConnectType = HWI_CPC                                             
      Do i = 1 To AnswerArea.0                                        
        Say "CPC" i ":" AnswerArea.i                                  
        InConnectToken = 0                                            
        Address BCPII "HWICONN Retcode InConnectToken OutConnectToken 
        ConnectType AnswerArea.i DiagArea."                           
        If RC = 0  & retcode = 0 Then                                 
        Say "Connected to CPC "AnswerArea.i"."        
      End                                              
End                                                    
Else Do                                                
        Say "rc = " rc " Retcode = " Retcode           
End                                                                    
