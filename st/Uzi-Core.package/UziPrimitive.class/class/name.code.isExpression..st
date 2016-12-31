private
name: nameSymbol code: codeInteger isExpression: isExpressionBool
	"Private! You should use #named: instead"
	^ self basicNew 
		initializeWithName: nameSymbol 
		code: codeInteger
		isExpression: isExpressionBool