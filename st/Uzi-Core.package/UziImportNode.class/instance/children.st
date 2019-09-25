accessing
children
	^{ self initializationBlock }
		select: [:each | each notNil]