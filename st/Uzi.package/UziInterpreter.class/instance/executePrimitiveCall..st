bytecodes
executePrimitiveCall: argument
	argument caseOf: {
		[0] -> [self executeGetValuePrimitive].
		[1] -> [self executeSetValuePrimitive].
		[2] -> [self executeGetModePrimitive].
		[3] -> [self executeSetModePrimitive].
		
	}