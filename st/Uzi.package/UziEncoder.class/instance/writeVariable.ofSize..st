encoding programs
writeVariable: value ofSize: size
	self flag: #Richo. "Take care of size 4 special case"
	(size - 1 to: 0 by: -1) do: [:n |
		stream nextPut: ((value bitShift: (8 * n * -1)) bitAnd: 16rFF)]