grammar-statements
statement
	^ ws , 
		(assignment / return / conditional / 
		startTask / stopTask /
		while / doWhile / until / doUntil /
		expressionStatement) , 
		ws
		map: [:ign1 :stmt :ign2 | stmt]