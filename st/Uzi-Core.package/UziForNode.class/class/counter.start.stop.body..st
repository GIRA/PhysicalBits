instance creation
counter: variableDeclaration
start: startExpression 
stop: stopExpression
body: body

	^ self counter: variableDeclaration
		start: startExpression
		stop: stopExpression
		step: nil
		body: body