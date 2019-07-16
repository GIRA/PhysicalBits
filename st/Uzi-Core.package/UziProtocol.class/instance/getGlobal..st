accessing-globals
getGlobal: globalName
	^ self globals
		at: globalName
		ifAbsentPut: [
			UziGlobalWatcher
				name: globalName
				number: (self getGlobalNumber: globalName)]