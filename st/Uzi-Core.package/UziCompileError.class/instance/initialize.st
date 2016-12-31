initializing
initialize
	super initialize.
	self messageText: ('{1} error{2} found!' 
		format: { errors size . errors size = 1 ifTrue: '' ifFalse: 's' })