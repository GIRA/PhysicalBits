instance creation
named: primitiveName
	^ primitiveName caseOf: {
		[#read] -> [self code: 0].
		[#write] -> [self code: 1].
		[#toggle] -> [self code: 2].
		[#add] -> [self code: 3].
		[#subtract] -> [self code: 4].
		[#multiply] -> [self code: 5].
		[#divide] -> [self code: 6].
		[#sin] -> [self code: 7].
		[#cos] -> [self code: 8].
		[#tan] -> [self code: 9].
		[#arcSin] -> [self code: 10].
		[#arcCos] -> [self code: 11].
		[#arcTan] -> [self code: 12].
		[#squared] -> [self code: 13].
		[#sqrt] -> [self code: 14].
	
	} otherwise: [UziPrimitiveNotFoundError signal: primitiveName]