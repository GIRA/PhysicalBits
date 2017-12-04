variables
registerLocal: local value: value
	^ locals detect: [:v | v name = local] 
		ifNone: [program registerGlobal: value.
			locals add: (UziVariable name: local value: value)]