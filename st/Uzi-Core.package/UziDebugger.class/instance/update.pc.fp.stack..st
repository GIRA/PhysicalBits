event handling
update: scriptIndex pc: pcInt fp: fpInt stack: aFloatArray
	"If the program suddenly changed we should stop receiving updates"
	self program ~= self currentProgram
		ifTrue: [self close]
		ifFalse: [index := scriptIndex.
			pc := pcInt.
			fp := fpInt.
			stack := aFloatArray.
			UziTranscript log: 'Debugger update {1}' format: { Time millisecondClockValue }.
			self triggerEvent: #debuggerUpdate]