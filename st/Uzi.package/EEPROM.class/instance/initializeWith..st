initializing
initializeWith: size
	bytes := ByteArray new: size withAll: 255.
	writes := Array new: size withAll: 0.
	max := 0