grammar-binary
buildBinaryExpression: token
	| result |
	result := OrderedCollection new.
	
	"First, flatten the token value so that instead of (1 ((+ 2) (+ 3))) we
	have (1 + 2 + 3)"
	result add: token parsedValue first.
	token parsedValue second do: [:each |
		result add: each parsedValue second.
		result add: each parsedValue third].
	
	"Then reduce operators according to the precedence table"
	self precedenceTable do: [:operators |
		result := self fixPrecedence: result operators: operators].
	
	"If a binary expression was not reduced after going through the precedence	table, 
	we iterate once again but this time reducing any operator found (left to right)"
	result := self fixPrecedence: result operators: nil.
	
	"Finally, we should have a single expression in our results"
	^ result first