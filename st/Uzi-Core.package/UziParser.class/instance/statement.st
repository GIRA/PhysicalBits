grammar-statements
statement
	^ ws , 
		(assignment / return / conditional / while / expressionStatement) , 
		ws
		map: [:ign1 :stmt :ign2 | stmt]