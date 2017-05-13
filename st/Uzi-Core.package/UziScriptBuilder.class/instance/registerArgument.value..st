variables
registerArgument: arg value: value
	^ args detect: [:v | v name = arg] 
		ifNone: [args add: (UziVariable name: arg value: value)]