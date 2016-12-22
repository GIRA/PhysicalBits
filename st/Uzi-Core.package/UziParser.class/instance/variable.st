grammar-variables
variable
	^ identifier map: [:varName | UziVariableNode named: varName asSymbol]