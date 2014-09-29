script building
scriptNamed: name state: state delay: delay literals: literals variables: variables bytecodes: aBlockClosure 
	^ UziScript
		name: name
		state: state
		delay: delay
		literals: literals
		variables: variables
		bytecodes: (self buildBytecodes: aBlockClosure )