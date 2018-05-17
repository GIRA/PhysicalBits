accessing
children
	^ { self script }, (self arguments collect: [:each | each value])