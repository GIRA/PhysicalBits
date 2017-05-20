event handling
update: scriptIndex pc: pcInt fp: fpInt stack: anOrderedCollection
	self index = scriptIndex
		ifTrue: [pc := pcInt.
			fp := fpInt.
			stack := anOrderedCollection]