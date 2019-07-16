accessing-globals
getGlobal: globalName
	^ self globals
		at: globalName
		ifAbsentPut: [
			"self setGlobal: globalName report: true."
			UziGlobalWatcher
				name: globalName
				number: (self getGlobalNumber: globalName)]