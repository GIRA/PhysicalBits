encode
createDictionary: anObject
	| class dict instVarNames |
	dict := Dictionary new.	
	class := anObject class.
	dict at: '__class__' put: class name.
	instVarNames := class allInstVarNames.
	(class respondsTo: #jsonExcludedInstVarNames)
		ifTrue: [instVarNames := instVarNames copyWithoutAll: class jsonExcludedInstVarNames].
	instVarNames
		do: [:key || value |
			value := anObject instVarNamed: key.
			dict at: key put: (self createJSONObject: value)].
	^ dict