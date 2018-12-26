printing
annotatedStack
	^ String streamContents: [:stream | self printStackOn: stream]