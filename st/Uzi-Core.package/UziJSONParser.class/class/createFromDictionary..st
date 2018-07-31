as yet unclassified
createFromDictionary: dict
	| class instance |
	class := Smalltalk at: (dict at: 'type') asSymbol.
	instance := class basicNew.
	(class allInstVarNames select: [:each | dict includesKey: each])
		do: [:key || value |
			value := dict at: key.
			instance instVarNamed: key 
				put: (self createObject: value)].
	^ instance