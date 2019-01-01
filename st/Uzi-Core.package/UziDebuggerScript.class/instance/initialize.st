initialize-release
initialize
	super initialize.
	instructions := UziDebuggerInstruction 
		group: script instructions
		startingAt: start
		script: self