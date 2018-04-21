instance creation
counter: variable
start: startExpression 
stop: stopExpression
body: body

	^ self counter: variable
		start: startExpression
		stop: stopExpression
		step: nil
		body: body