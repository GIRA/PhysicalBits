testing
isTask
	^ node ifNil: [true] ifNotNil: [:n | n isTask]