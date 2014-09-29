script building
scriptNamed: name 
state: state 
delay: delay 
literals: literals 
variables: variables 
bytecodes: aBlock

	^ UziScript
		name: name
		state: state
		delay: delay
		literals: literals
		variables: variables
		bytecodes: (aBlock value: self new) asArray