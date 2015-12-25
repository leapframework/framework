select * from 		    
( 			    
	( 			    
		select sus.ID from f_U_c_ sus			
	)  				
	union  				
	( 			    
		select sus.ID from f_U_c_ sus  	     	
	) 		    
) results;

--multi-unions
select * from t
union
select * from t
union
select * from t;