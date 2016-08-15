encoding bytecodes
visitJMPBytecode: bytecode
	stream 
		nextPut: 16rFF;
		nextPut: bytecode argument