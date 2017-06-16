grammar-variables
variableDefinitionList
	^ (ws, $| asParser, (variableDefinition separatedBy: $, asParser), $| asParser, ws) optional
		==> [:nodes | 
			nodes 
				ifNil: [#()] 
				ifNotNil: [nodes third
					select: [:each | each ~= $,]
					thenCollect: [:def | def]]]