HelloWld CSECT                
         STM   14,12,12(13)  
         BALR  12,0          
         USING *,12          
         ST    13,SAVE+4      
         LA    13,SAVE        
         WTO   'HELLO WORLD!' 
         L     13,SAVE+4      
         LM    14,12,12(13)  
         BR    14            
SAVE     DS    18F            
         END   