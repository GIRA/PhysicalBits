bytecodes
visitJNZBytecode: bytecode
	stack pop ~= 0
		ifTrue: [ip := ip + bytecode argument]