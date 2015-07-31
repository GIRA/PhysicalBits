instance creation
named: primitiveName
	^ primitiveName caseOf: {
		[#getValue] -> [self code: 0].
		[#setValue] -> [self code: 1].
		[#toggle] -> [self code: 2]
	} otherwise: [UziPrimitiveNotFoundError signal: primitiveName]