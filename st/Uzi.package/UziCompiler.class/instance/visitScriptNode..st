visiting
visitScriptNode: node
	^ UziScript
		name: node name
		state: node state
		delay: (self visit: node tickingRate)
		body: (self visitCollection: node body)