instance creation
named: primitiveName
	^ primitiveName caseOf: {
		[#read] -> [self code: 0].
		[#write] -> [self code: 1].
		[#toggle] -> [self code: 2].
		[#servoWrite] -> [self code: 5]
	} otherwise: [UziPrimitiveNotFoundError signal: primitiveName]