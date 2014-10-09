encoding bytecodes
writeBytecode: code argument: arg 
	"First 4 bits = code
	Second 4 bits = arg"
	stream nextPut: ((code bitShift: 4) bitOr: arg)
	