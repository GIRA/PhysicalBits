instance creation
counter: counter
start: start
stop: stop
step: step 
body: body

	^ self basicNew 
		initializeWithCounter: counter
		start: start
		stop: stop
		step: (step ifNil: [UziNumberLiteralNode value: 1])
		body: body