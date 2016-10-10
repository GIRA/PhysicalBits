accessing
reset
	bytes := ByteArray new: bytes size withAll: 255.
	writes := Array new: writes size withAll: 0.
	max := 0