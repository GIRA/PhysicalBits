visiting
visitTickingRateNode: node
	self assert: self path second state ~= #once
		description: 'Ticking rate is not valid if task state is not specified'
		node: node.
	self assert: node value > 0 
		description: 'Ticking rate must be a positive value' 
		node: node