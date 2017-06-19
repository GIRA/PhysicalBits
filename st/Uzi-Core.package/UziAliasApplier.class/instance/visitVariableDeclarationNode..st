visiting
visitVariableDeclarationNode: node
	^ (node class 
		name: ((self isLocal: node name) 
			ifTrue: [node name]
			ifFalse: [alias , node name])
		value: node value)
		token: node token