instance creation
name: name 
state: state 
delay: delay 
literals: literals 
variables: variables
bytecodes: bytecodes
	^ self basicNew
		initializeWithName: name
		state: state
		delay: delay
		literals: literals 
		variables: variables
		bytecodes: bytecodes