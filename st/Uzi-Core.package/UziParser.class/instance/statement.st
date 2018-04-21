grammar-statements
statement
	^ ws , 
		(variableDeclaration /
		assignment / return / conditional / 
		startTask / stopTask / pauseTask / resumeTask /
		while / doWhile / until / doUntil / repeat / forever /
		yield /
		expressionStatement) , 
		ws
		map: [:ign1 :stmt :ign2 | stmt]