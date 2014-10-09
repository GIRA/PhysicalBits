encoding scripts
writeBytecodes: anUziScript
	anUziScript bytecodes do: [:bytecode | self visit: bytecode]