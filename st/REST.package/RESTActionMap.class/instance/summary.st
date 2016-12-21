accessing
summary
	| json |
	json := super summary.
	children keysAndValuesDo: [:key :value || condition |
		condition := conditions at: key ifAbsent: [true].
		(condition cull: self)
			ifTrue: [json at: key put: value summary]].
	^ json