grammar-binary
fixPrecedence: nodes operators: operators
	| left op result right stream last expr |
	nodes size < 3 ifTrue: [^ nodes].
	result := OrderedCollection new.
	stream := nodes readStream.
	left := stream next.
	last := nodes size - 1 / 2.
	1 to: last do: [:i |
		op := stream next.
		right := stream next.
		(operators isNil or: [operators includes: op])
			ifTrue: [expr := self buildBinaryExpression: op left: left right: right.
				expr token: (PPToken 
					on: left token collection
					start: left token start
					stop: right token stop
					value: { left token . op . right token }).
				left := expr]
			ifFalse: [result add: left; add: op.
				left := right].
		i = last ifTrue: [result add: left]].
	^ result