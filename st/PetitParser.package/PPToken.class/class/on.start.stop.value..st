instance creation
on: aSequenceableCollection start: aStartInteger stop: aStopInteger value: anObject
	^ self basicNew 
		initializeOn: aSequenceableCollection
		start: aStartInteger stop: aStopInteger
		value: anObject