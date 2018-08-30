decode
createFromDictionary: dict
	| class instance |
	dict keys = #('___INF___')
		ifTrue: ["HACK(Richo): Special case because JSON encodes Infinity as null"
			^ Float infinity * (dict at: '___INF___')].
	class := Smalltalk at: (dict at: '__class__') asSymbol.
	instance := class new.
	(class allInstVarNames select: [:each | dict includesKey: each])
		do: [:key || value |
			value := self createObject: (dict at: key).
			instance instVarNamed: key put: value].
	^ instance