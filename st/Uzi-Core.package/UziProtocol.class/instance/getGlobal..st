accessing-globals
getGlobal: globalNumber
	^ self globals
		at: globalNumber
		ifAbsentPut: [
			self setGlobal: globalNumber report: true.
			UziGlobal 
				name: (self getGlobalName: globalNumber) 
				number: globalNumber]