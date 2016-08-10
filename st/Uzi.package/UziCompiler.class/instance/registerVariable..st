private
registerVariable: node
	^ variables
		detect: [:var | var value = node value] 
		ifNone: [variables add: (UziVariable value: node value)]