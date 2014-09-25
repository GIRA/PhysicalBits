visiting
visitScriptNode: node
	^ UziScript
		name: node name
		state: node state
		delay: (self visit: node tickingRate)
		bytecodes: (self visitCollection: node body)