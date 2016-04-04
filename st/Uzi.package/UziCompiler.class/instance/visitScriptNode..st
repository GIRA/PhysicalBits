visiting
visitScriptNode: node
	literals := OrderedCollection new.
	variables := OrderedCollection new.
	bytecodes := OrderedCollection new.
	self visitCollection: node body.
	bytecodes add: UziEndBytecode new.
	^ UziScript
		name: node name
		ticking: node state = #ticking
		delay: (self visit: node tickingRate ifNil: 0)
		literals: literals asArray
		variables: variables asArray
		bytecodes: bytecodes asArray