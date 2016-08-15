encoding bytecodes
writeBytecodes: anUziScript
	"We need to write the bytecode count first, so we use a temporary stream to store the bytecodes
	and then we just count how many we wrote."
	| bytecodes |
	bytecodes := self 
		withStream: #[] writeStream
		do: [self visitCollection: anUziScript bytecodes].
	stream nextPut: bytecodes size.
	stream nextPutAll: bytecodes
	