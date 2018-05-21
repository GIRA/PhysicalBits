private
assertNodesOfTheSameClass
	nodesDict associations do: [:kvp | 
		kvp value do: [:node |
			self assert: kvp key class
				equals: node class
				description: 'Node types don''t match']]