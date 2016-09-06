instance creation
named: primitiveName	
	^ self code: (primitiveName caseOf: {
		[#read] -> [0].
		[#write] -> [1].
		[#toggle] -> [2].
		[#servoDegrees] -> [3].
		[#servoWrite] -> [4].
		[#multiply] -> [5].
		[#add] -> [6]
	} otherwise: [UziPrimitiveNotFoundError signal: primitiveName])