utils
check: aStringOrParseNode
	| ast |

	ast := aStringOrParseNode isString 
		ifTrue: [UziParser parse: aStringOrParseNode]
		ifFalse: [aStringOrParseNode].
	ast isPetitFailure ifTrue: [^ ast].
	^ UziTypeChecker check: ast