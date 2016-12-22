tests
testScriptName 
	| pp |
	pp := parser scriptName.
	self assert: (pp parse: '#foo') value == #foo.