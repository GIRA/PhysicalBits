as yet unclassified
register: programOrString test: testName class: aClass
	| src programO0 programO1 |
	src := programOrString isString 
		ifTrue: [programOrString]
		ifFalse: [programOrString node
			ifNotNil: [:node | node token
				ifNil: [UziParseNodePrinter new print: node]
				ifNotNil: [:token | token collection]]].
	
	src ifNotNil: [
		programO0 := Uzi compileString: src using: UziCompiler new.
		programO1 := Uzi compileString: src using: UziOptimizingCompiler new.
	
		stats add: {
			#className -> aClass asString.
			#testName -> testName.
		
			#'instructionCount.O0' -> programO0 instructions size.
			#'globalCount.O0' -> programO0 globals size.
			#'encodedSize.O0' -> (Uzi encode: programO0) size.
			
			#'instructionCount.O1' -> programO1 instructions size.
			#'globalCount.O1' -> programO1 globals size.
			#'encodedSize.O1' -> (Uzi encode: programO1) size.
			
			#'instructionCount.DIFF' -> (programO1 instructions size - programO0 instructions size).
			#'globalCount.DIFF' -> (programO1 globals size - programO0 globals size).
			#'encodedSize.DIFF' -> ((Uzi encode: programO1) size - (Uzi encode: programO0) size)
		}]