visiting
visitVariableDeclarationNode: node
	script ifNil: ["Globals can only be initialized with constants or numbers"
		program registerGlobal: node name value: node value value.
		^ self].
	node value 
		ifNil: [script registerLocal: node name]
		ifNotNil: [:value | 
			value isNumber
				ifTrue: [script registerLocal: node name value: value value]
				ifFalse: ["Register the local with default value and then compile an assignment"
					script registerLocal: node name.
					self visit: value.
					(script pop: node name) node: node]]