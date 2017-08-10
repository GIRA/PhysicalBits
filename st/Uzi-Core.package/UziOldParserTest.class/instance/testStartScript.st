tests
testStartScript
	| ast body pp |
	pp := parser.
	ast := pp parse: '@main [@foo start]'.
	body := ast scripts first body.
	self assert: #foo equals: body statements first scriptName.