grammar-variables
variableDefinition
	^ ws, variable , ws , ':=' asParser flatten, ws , (constant / number) , separator 
		==> [:nodes | UziVariableDefinitionNode name: nodes second name value: nodes sixth value]