initialize-release
initialize
	super initialize.
	instructions := UziDebuggerInstruction 
		group: innerScript instructions
		startingAt: start
		script: self