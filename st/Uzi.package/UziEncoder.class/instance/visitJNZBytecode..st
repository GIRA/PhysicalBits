encoding bytecodes
visitJNZBytecode: bytecode  
	stream
		nextPut: 16rF2;
		nextPut: bytecode argument