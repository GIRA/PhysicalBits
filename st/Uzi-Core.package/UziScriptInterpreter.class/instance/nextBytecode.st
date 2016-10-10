accessing
nextBytecode
	| bytecode |
	bytecode := script instructionAt: ip.
	ip := ip + 1.
	^ bytecode