grammar-expressions
assignment 
	^ variable , ws , ':=' asParser , ws , expression 
		==> [:nodes | UziAssignmentNode left: nodes first right: nodes last]