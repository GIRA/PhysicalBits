instance creation
named: primitiveName	
	^ self code: (primitiveName caseOf: {
		[#read] -> [0].
		[#write] -> [1].
		[#toggle] -> [2].
		[#servoDegrees] -> [3].
		[#servoWrite] -> [4].
		[#multiply] -> [5].
		[#add] -> [6].
		[#divide] -> [7].
		[#subtract] -> [8].
		[#equals] -> [9].
		[#notEquals] -> [10].
		[#greaterThan] -> [11].
		[#greaterThanOrEquals] -> [12].
		[#lessThan] -> [13].
		[#lessThanOrEquals] -> [14].
		[#negate] -> [15].
		[#sin] -> [16].
		[#cos] -> [17].
		[#tan] -> [18]
	} otherwise: [UziPrimitiveNotFoundError signal: primitiveName])