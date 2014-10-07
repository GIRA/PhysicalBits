accessing
nextBytecode
	| bytecode |
	bytecode := script bytecodes at: ip + 1.
	ip := ip + 1.
	^ bytecode