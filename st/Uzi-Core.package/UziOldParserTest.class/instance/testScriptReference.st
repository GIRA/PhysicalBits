tests
testScriptReference
	| pp msg |
	pp := parser scriptReference.
	msg := pp parse: '@foo'.
	self assert: #foo equals: msg name