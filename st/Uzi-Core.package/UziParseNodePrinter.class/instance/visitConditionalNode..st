visiting
visitConditionalNode: node
	self saveIntervalFor: node
		during: [
			self write: 'if '; 
				visit: node condition;
				write: ' ';
				visit: node trueBranch.
			node falseBranch isEmpty 
				ifFalse: [self write: ' else ';
					visit: node falseBranch]]