accessing
children
	^ { self value } select: [:each | each notNil]