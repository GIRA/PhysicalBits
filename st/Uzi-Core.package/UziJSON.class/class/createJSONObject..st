encode
createJSONObject: anObject
	(anObject isString 
			or: [anObject == nil
			or: [anObject == true
			or: [anObject == false]]])
		ifTrue: [^ anObject].
	(anObject isNumber and: [(anObject isKindOf: UziNumberNode) not])
		ifTrue: [	"HACK(Richo): Special case because JSON encodes Infinity as null"
			anObject isInfinite
				ifTrue: [^ Dictionary new
					at: '___INF___' put: (anObject > 0 ifTrue: [1] ifFalse: [-1]);
					yourself].
			^ anObject asFloat].
	anObject isCollection
		ifTrue: [^ anObject collect: [:each | self createJSONObject: each] as: Array].
		
	^ self createDictionary: anObject