as yet unclassified
writeBytecodes: anUziScript
	anUziScript bytecodes do: [:bytecode | self visit: bytecode]