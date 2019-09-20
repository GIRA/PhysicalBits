initialize
initialize
	super initialize.
	nodes := parseTrees collect: [:tree | tree allNodes].
	nodesDict := (((1 to: (nodes detectMin: [:each | each size]) size) 
		collect: [:i | nodes collect: [:each | each at: i ifAbsent: [nil]]])
		collect: [:each | each first -> each allButFirst]) as: Dictionary