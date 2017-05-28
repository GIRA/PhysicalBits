accessing
variables
	^ variables select: [:each | each name notNil]