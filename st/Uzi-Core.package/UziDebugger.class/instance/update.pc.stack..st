event handling
update: scriptIndex pc: anInteger stack: anOrderedCollection
	self index = scriptIndex
		ifTrue: [pc := anInteger.
			stack := anOrderedCollection]