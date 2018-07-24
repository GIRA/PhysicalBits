grammar-binary
fixPrecedence: nodes operators: operators
	nodes size < 3 ifTrue: [^ nodes].
	^ Array streamContents: [:result || left |
		left := nodes first.
		nodes allButFirst pairsDo: [:op :right |
			(operators isNil or: [operators includes: op])
				ifTrue: [| expr |
					expr := self buildBinaryExpression: op 
						left: left 
						right: right.
					expr token: (PPToken 
						on: left token collection
						start: left token start
						stop: right token stop
					value: { left token . op . right token }).
					left := expr]
				ifFalse: [result nextPut: left; nextPut: op.
					left := right]].
		result nextPut: left]