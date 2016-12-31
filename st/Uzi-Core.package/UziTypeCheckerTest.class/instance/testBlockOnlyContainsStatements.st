tests
testBlockOnlyContainsStatements
	self assertInvalid: '#foo [4]'.	
	self assertInvalid: '#foo [a]'.
	self assertInvalid: '#foo [4 read]'.
	self assertInvalid: '#foo [3 > 4]'.
	self assertInvalid: '#foo [D13]'.
	self assertInvalid: '#foo [5 sin]'.
	self assertInvalid: '#foo [3 > 4 ifTrue: [4]]'.
	self assertInvalid: '#foo [3 > 4 ifFalse: [4]]'.
	self assertInvalid: '#foo [3 > 4 ifTrue: [3] ifFalse: [3 on]]'.
	self assertInvalid: '#foo [3 > 4 ifTrue: [3 off] ifFalse: [3]]'.