visiting
visitTickingRateNode: node
	(nodesDict at: node) do: [:each | 
		self assert: node value
			equals: each value
			description: 'Ticking rate value doesn''t match'.
		self assert: node scale
			equals: each scale
			description: 'Ticking rate scale doesn''t match'].
	self compareChildren: node