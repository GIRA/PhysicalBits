visiting
visitScriptNode: node
	literals := OrderedCollection new.
	variables := OrderedCollection new.
	bytecodes := OrderedCollection new.
	self visitCollection: node body.
	bytecodes add: UziEndBytecode new.
	^ UziScript
		name: node name
		state: node state
		delay: (self visit: node tickingRate)
		literals: literals asArray
		variables: variables asArray
		bytecodes: bytecodes asArray