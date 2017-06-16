tests
testStopScript
	| ast body pp |
	pp := parser.
	ast := pp parse: '@main [@foo stop]'.
	body := ast scripts first body.
	self assert: #foo equals: body statements first script name.