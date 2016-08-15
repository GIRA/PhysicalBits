visiting
visitScriptNode: node
	bytecodes := OrderedCollection new.
	self visitCollection: node body.
	^ UziScript
		name: node name
		ticking: node state = #ticking
		delay: (self visit: node tickingRate ifNil: 0)
		bytecodes: bytecodes asArray