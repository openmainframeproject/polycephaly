/*  REXX */                                                                 
/*                                                                          
    myConnectType = 1 = HWI_CPC                                             
    myConnectType = 2 = HWI_IMAGE                                           
    myConnectType = 3 = HWI_CAPREC                                          
    myConnectType = 4 = HWI_RESET_ACTPROF                                   
    myConnectType = 5 = HWI_IMAGE_ACTPROF                                   
    myConnectType = 6 = HWI_LOAD_ACTPROF                                    
    myConnectType = 7 = HWI_IMAGE_GROUP      
    
    SPDX-License-Identifier: Apache-2.0 
    SPDX-License-Identifier: CC-BY-4.0
*/                                                                          
myConnectType = 1                                                           
myConnectTypeValue  = 'IBM390PS.P00XXXXX' /* 17-char CPC name */            
                                                                            
address bcpii                                                               
        "hwiconn Retcode myInConnectToken myOutConnectToken myConnectType   
            myConnectTypeValue myDiag."                                     
                                                                            
Say 'RC = ' RC 'RetCode = ' Retcode                                         
If (RC <> 0) | (Retcode <> 0) Then                                          
     Say 'Service failed with REXX RC = 'RC' and API Retcode = 'Retcode'.'  
     If (RC=Hwi_REXXParmSyntaxError | Retcode<>0) Then                      
       Do                                                                   
         Say ' Diag_index=' myDiag.DIAG_INDEX                               
         Say ' Diag_key=' myDiag.DIAG_KEY                                   
         Say ' Diag_actual=' myDiag.DIAG_ACTUAL                             
         Say ' Diag_expected=' myDiag.DIAG_EXPECTED                         
         Say ' Diag_commerr=' myDiag.DIAG_COMMERR                           
         Say ' Diag_text=' myDiag.DIAG_TEXT                                 
       End                                                                  
  End                                                                       
                                                                            
/*                                                                          
    myListType = 1 = HWI_LIST_CPCS                                          
    myListType = 2 = HWI_LIST_IMAGES                                        
    myListType = 3 = HWI_LIST_EVENTS                                        
    myListType = 4 = HWI_LIST_CAPRECS                                       
    myListType = 5 = HWI_LIST_LOCALCPC                                      
    myListType = 6 = HWI_LIST_LOCALIMAGE                                    
    myListType = 7 = HWI_LIST_RESET_ACTPROF                                 
    myListType = 8 = HWI_LIST_IMAGE_ACTPROF                                 
    myListType = 9 = HWI_LIST_LOAD_ACTPROF                                  
    myListType = A = HWI_LIST_IMAGEGROUPS                                   
*/                                                                          
/********************************************************************/      
/*  Call #1  */                                                             
/********************************************************************/      
    myListType = 1                                                          
                                                                            
address bcpii                                                               
    "hwilist RetCode myOutConnectToken myListType myAnswerArea. myDiag."    
                                                                            
Say 'RC = ' RC 'RetCode = ' Retcode                                         
  Do                                                                        
    Say 'Number of items returned = 'myAnswerArea.0                         
    If myAnswerArea.0 > 0 Then                                              
      Do n=1 to myAnswerArea.0                                              
        Say 'Image #'n' = 'myAnswerArea.n                                   
      End                                                                   
  End                                                                       
/********************************************************************/      
/*  Call #2  */                                                             
/********************************************************************/      
    myListType = 2                                                          
                                                                            
address bcpii                                                               
    "hwilist RetCode myOutConnectToken myListType myAnswerArea. myDiag."    
                                                                            
Say 'RC = ' RC 'RetCode = ' Retcode                                         
  Do                                                                        
    Say 'Number of items returned = 'myAnswerArea.0                         
    If myAnswerArea.0 > 0 Then                                              
      Do n=1 to myAnswerArea.0                                              
        Say 'Image #'n' = 'myAnswerArea.n                                   
      End                                                                   
  End                                                                       
/********************************************************************/      
/*  Call #3  */                                                             
/********************************************************************/      
    myListType = 3                                                          
                                                                            
address bcpii                                                               
    "hwilist RetCode myOutConnectToken myListType myAnswerArea. myDiag."    
                                                                            
Say 'RC = ' RC 'RetCode = ' Retcode                                         
  Do                                                                        
    Say 'Number of items returned = 'myAnswerArea.0                         
    If myAnswerArea.0 > 0 Then                                              
      Do n=1 to myAnswerArea.0                                              
        Say 'Image #'n' = 'myAnswerArea.n                                   
      End                                                                   
  End                                                                       
/********************************************************************/      
/*  Call #4  */                                                             
/********************************************************************/      
    myListType = 4                                                          
                                                                            
address bcpii                                                               
    "hwilist RetCode myOutConnectToken myListType myAnswerArea. myDiag."    
                                                                            
Say 'RC = ' RC 'RetCode = ' Retcode                                         
  Do                                                                        
    Say 'Number of items returned = 'myAnswerArea.0                         
    If myAnswerArea.0 > 0 Then                                              
      Do n=1 to myAnswerArea.0                                              
        Say 'Image #'n' = 'myAnswerArea.n                                   
      End                                                                   
  End                                                                       
/********************************************************************/      
/*  Call #5  */                                                             
/********************************************************************/      
    myListType = 5                                                          
                                                                              
address bcpii                                                                 
    "hwilist RetCode myOutConnectToken myListType myAnswerArea. myDiag."      
                                                                              
Say 'RC = ' RC 'RetCode = ' Retcode                                           
  Do                                                                          
    Say 'Number of items returned = 'myAnswerArea.0                           
    If myAnswerArea.0 > 0 Then                                                
      Do n=1 to myAnswerArea.0                                                
        Say 'Image #'n' = 'myAnswerArea.n                                     
      End                                                                     
  End                                                                         
/********************************************************************/        
/*  Call #6  */                                                               
/********************************************************************/        
    myListType = 6                                                            
                                                                              
address bcpii                                                                 
    "hwilist RetCode myOutConnectToken myListType myAnswerArea. myDiag."      
                                                                              
Say 'RC = ' RC 'RetCode = ' Retcode                                           
  Do                                                                          
    Say 'Number of items returned = 'myAnswerArea.0                           
    If myAnswerArea.0 > 0 Then                                            
      Do n=1 to myAnswerArea.0                                            
        Say 'Image #'n' = 'myAnswerArea.n                                 
      End                                                                 
  End                                                                     
/********************************************************************/    
/*  Call #7  */                                                           
/********************************************************************/    
    myListType = 7                                                        
                                                                          
address bcpii                                                             
    "hwilist RetCode myOutConnectToken myListType myAnswerArea. myDiag."  
                                                                          
Say 'RC = ' RC 'RetCode = ' Retcode                                       
  Do                                                                      
    Say 'Number of items returned = 'myAnswerArea.0                       
    If myAnswerArea.0 > 0 Then                                            
      Do n=1 to myAnswerArea.0                                            
        Say 'Image #'n' = 'myAnswerArea.n                                 
      End                                                                 
  End                                                                     
/********************************************************************/    
/*  Call #8  */                                                           
/********************************************************************/         
    myListType = 8                                                             
                                                                               
address bcpii                                                                  
    "hwilist RetCode myOutConnectToken myListType myAnswerArea. myDiag."       
                                                                               
Say 'RC = ' RC 'RetCode = ' Retcode                                            
  Do                                                                           
    Say 'Number of items returned = 'myAnswerArea.0                            
    If myAnswerArea.0 > 0 Then                                                 
      Do n=1 to myAnswerArea.0                                                 
        Say 'Image #'n' = 'myAnswerArea.n                                      
      End                                                                      
  End                                                                          
/********************************************************************/         
/*  Call #9  */                                                                
/********************************************************************/         
    myListType = 9                                                             
                                                                               
address bcpii                                                                  
    "hwilist RetCode myOutConnectToken myListType myAnswerArea. myDiag."       
                                                                               
Say 'RC = ' RC 'RetCode = ' Retcode                                            
  Do                                                                            
    Say 'Number of items returned = 'myAnswerArea.0                             
    If myAnswerArea.0 > 0 Then                                                  
      Do n=1 to myAnswerArea.0                                                  
        Say 'Image #'n' = 'myAnswerArea.n                                       
      End                                                                       
  End                                                                           
/********************************************************************/          
/*  Call #A  */                                                                 
/********************************************************************/          
    myListType = A                                                              
                                                                                
address bcpii                                                                   
    "hwilist RetCode myOutConnectToken myListType myAnswerArea. myDiag."        
                                                                                
Say 'RC = ' RC 'RetCode = ' Retcode                                             
  Do                                                                            
    Say 'Number of items returned = 'myAnswerArea.0                             
    If myAnswerArea.0 > 0 Then                                                  
      Do n=1 to myAnswerArea.0                                                  
        Say 'Image #'n' = 'myAnswerArea.n                                       
      End                                                                       
  End                                                                           

