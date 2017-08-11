utils
check: aStringOrParseNode
	| ast |
	"Allow either a tree or a string that we need to parse"
	ast := aStringOrParseNode isString 
		ifTrue: [UziParser parse: aStringOrParseNode]
		ifFalse: [aStringOrParseNode].
	ast isPetitFailure ifTrue: [^ ast].
	^ UziParseTreeValidator check: ast