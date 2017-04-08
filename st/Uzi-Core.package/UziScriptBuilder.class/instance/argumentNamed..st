accessing
argumentNamed: argName
	^ args 
		detect: [:each | each name = argName]
		ifNone: [UziVariableNotFoundError signal: argName].