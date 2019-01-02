event handling
update: scriptIndex pc: pcInt fp: fpInt stack: aFloatArray
	index := scriptIndex.
	pc := pcInt.
	fp := fpInt.
	stack := aFloatArray.
	self triggerEvent: #debuggerUpdate