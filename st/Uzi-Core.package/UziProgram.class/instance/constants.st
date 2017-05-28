accessing
constants
	^ variables select: [:each | each name isNil]