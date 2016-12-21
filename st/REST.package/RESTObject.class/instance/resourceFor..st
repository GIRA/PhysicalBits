accessing
resourceFor: anObject
	(anObject notNil and: [anObject = data]) ifTrue: [^ self].
	^ super resourceFor: anObject