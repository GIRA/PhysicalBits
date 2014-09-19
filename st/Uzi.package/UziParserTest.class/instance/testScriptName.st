as yet unclassified
testScriptName 
	| pp |
	pp := UziParser new scriptName.
	self assert: (pp parse: '#foo') value == #foo.