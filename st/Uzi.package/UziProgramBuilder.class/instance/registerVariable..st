private
registerVariable: variableValue
	^ variables 
		detect: [:v | v value = variableValue]
		ifNone: [variables add: (UziVariable value: variableValue)]