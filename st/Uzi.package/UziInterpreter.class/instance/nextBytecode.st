accessing
nextBytecode
	| bytecode |
	bytecode := script bytecodeAt: ip.
	ip := ip + 1.
	^ bytecode