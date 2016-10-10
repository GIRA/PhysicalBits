private
registerVariable: arg value: value
	^ variables detect: [:v | v name = arg] 
		ifNone: [variables add: (UziVariable name: arg value: value)]