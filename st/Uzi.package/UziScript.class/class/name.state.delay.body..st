instance creation
name: name state: state delay: delay body: body
	^ self basicNew
		initializeWithName: name
		state: state
		delay: delay
		body: body