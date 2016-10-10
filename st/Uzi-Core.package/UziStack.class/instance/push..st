accessing
push: anObject
	self at: pointer put: anObject.
	pointer := pointer + 1