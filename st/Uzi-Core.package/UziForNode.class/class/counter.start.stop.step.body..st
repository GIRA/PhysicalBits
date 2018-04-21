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
		step: (step ifNil: [UziNumberNode value: 1])
		body: body