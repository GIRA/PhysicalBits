visiting
visitVariableNode: node
	^ (node class 
		named: ((self isLocal: node name) 
			ifTrue: [node name]
			ifFalse: [alias , node name]))
		token: node token