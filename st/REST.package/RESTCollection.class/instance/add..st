accessing-elements
add: aRESTResource
	| key |
	[key := (rnd nextInt: SmallInteger maxVal) asString asSymbol]
		doWhileTrue: [children includesKey: key].
	self at: key
		put: aRESTResource