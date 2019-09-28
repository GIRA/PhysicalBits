as yet unclassified
register: programOrString test: testName class: aClass
	| src program |
	programOrString isString 
		ifTrue: [src := programOrString. program := Uzi compileString: src]
		ifFalse: [program := programOrString.
			src := program node
				ifNotNil: [:node |
					node token
						ifNil: [UziParseNodePrinter new print: node]
						ifNotNil: [:token | token collection]]].
	
	stats add: {
		#className -> aClass asString.
		#testName -> testName.
		#instructionCount -> program instructions size.
		#globalCount -> program globals size.
		#encodedSize -> (Uzi encode: program) size.
	}.