visiting
visitScriptNode: node
	instructions := OrderedCollection new.
	self visitCollection: node body.
	^ UziScript
		name: node name
		ticking: node state = #ticking
		delay: (self visit: node tickingRate ifNil: 0)
		instructions: instructions asArray