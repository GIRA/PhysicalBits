grammar-literals
variableDefinition
	^ variable, ':=' asParser uziToken, (constantLiteral / numberLiteral), separator 