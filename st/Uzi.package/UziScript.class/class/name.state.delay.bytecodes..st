instance creation
name: name state: state delay: delay bytecodes: bytecodes
	^ self basicNew
		initializeWithName: name
		state: state
		delay: delay
		bytecodes: bytecodes