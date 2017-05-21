event handling
update: scriptIndex pc: pcInt fp: fpInt stack: aFloatArray
	self index = scriptIndex
		ifTrue: [pc := pcInt.
			fp := fpInt.
			stack := aFloatArray]