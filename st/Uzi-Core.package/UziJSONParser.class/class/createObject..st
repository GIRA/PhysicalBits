as yet unclassified
createObject: json
	json isDictionary 
		ifTrue: [^ self createFromDictionary: json].
	json isArray
		ifTrue: [^ self createFromArray: json].
	^ json
		