executing
executeBytecode: anUziBytecode 
	| code arg |
	code := anUziBytecode code.
	arg := anUziBytecode argument.
	
	code caseOf: {
		[0] -> [self executePushLiteral: arg].
		[1] -> [self executePushLocal: arg].
		[2] -> [self executePushGlobal: arg].
		[3] -> [self executeSetLocal: arg].
		[4] -> [self executeSetGlobal: arg].
		[5] -> [self executePrimitiveCall: arg].
		[6] -> [self executeScriptCall: arg].
		[7] -> [self executePop: arg].
		[8] -> [self executeJmp: arg].
		[9] -> [self executeJne: arg].
		[10] -> [self executeJnz: arg].
		"11 ... 14: Unknown"
		[15] -> [self executeExtend: arg]
	}