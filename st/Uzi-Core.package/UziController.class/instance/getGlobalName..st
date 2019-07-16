accessing-globals
getGlobalName: globalNumber
	^ runningProgram 
		ifNotNil: [:program | (program allGlobals 
			at: globalNumber + 1
			ifAbsent: [nil])
			ifNotNil: [:global | global name]]