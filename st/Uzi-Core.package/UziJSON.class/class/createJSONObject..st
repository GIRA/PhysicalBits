encode
createJSONObject: anObject
	(anObject isString 
			or: [anObject == nil
			or: [anObject == true
			or: [anObject == false]]])
		ifTrue: [^ anObject].
	anObject isNumber
		ifTrue: [	"HACK(Richo): Special case because JSON encodes Infinity and NaN as null"
			anObject isInfinite
				ifTrue: [^ Dictionary new
					at: '___INF___' put: (anObject > 0 ifTrue: [1] ifFalse: [-1]);
					yourself].
			anObject isNaN
				ifTrue: [^ Dictionary new
					at: '___NAN___' put: 0;
					yourself].
			^ anObject asFloat].
	anObject isCollection
		ifTrue: [^ anObject collect: [:each | self createJSONObject: each] as: Array].
		
	^ self createDictionary: anObject