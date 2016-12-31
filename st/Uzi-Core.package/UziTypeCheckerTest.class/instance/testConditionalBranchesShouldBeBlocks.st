tests
testConditionalBranchesShouldBeBlocks
	self assertInvalid: '#foo [D13 read ifTrue: D13 off]'.
	self assertInvalid: '#foo [D13 read ifFalse: D13 on]'.
	self assertInvalid: '#foo [D13 read ifTrue: [D13 off] ifFalse: D13 on]'.
	self assertInvalid: '#foo [D13 read ifTrue: D13 off ifFalse: [D13 on]]'.
	self assertInvalid: '#foo [D13 read ifFalse: [D13 on] ifTrue: D13 off]'.
	self assertInvalid: '#foo [D13 read ifFalse: D13 on ifTrue: [D13 off]]'.
	self assertInvalid: '#foo [D13 read ifTrue: D13 off ifFalse: D13 on]'.
	self assertInvalid: '#foo [D13 read ifFalse: D13 on ifTrue: D13 off]'.