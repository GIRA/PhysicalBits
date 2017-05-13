variables
registerLocal: local value: value
	^ locals detect: [:v | v name = local] 
		ifNone: [locals add: (UziVariable name: local value: value)]