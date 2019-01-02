updating
update: scriptIndex pc: pcInt fp: fpInt stack: aFloatArray
	index := scriptIndex.
	pc := pcInt.
	fp := fpInt.
	stack := aFloatArray.
	self flag: #Richo. "Remove this trigger event because is redundant with protocol.
	I think the only subscriber is the UziDebuggerMorph, which could easily be fixed by 
	subscribing to the protocol."
	self triggerEvent: UziProtocolEvents debuggerUpdate