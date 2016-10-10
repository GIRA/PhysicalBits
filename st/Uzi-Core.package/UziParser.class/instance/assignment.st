grammar
assignment
	^ super assignment 
		map: [:left :ign :right | UziAssignmentNode left: left right: right]