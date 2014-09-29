instance creation
name: name state: state delay: delay bytecodes: bytecodes
	^ self name: name 
		state: state 
		delay: delay 
		literals: #() 
		variables: #() 
		bytecodes: bytecodes