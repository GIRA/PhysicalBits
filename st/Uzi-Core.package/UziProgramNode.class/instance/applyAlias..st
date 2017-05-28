converting
applyAlias: alias
	^ (self class
		variableDefinitions: (variableDefinitions 
			collect: [:each | each applyAlias: alias]) 
		scripts: (scripts 
			collect: [:each | each applyAlias: alias]))
		token: token