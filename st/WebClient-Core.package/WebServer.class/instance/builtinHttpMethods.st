initialize
builtinHttpMethods
	"Returns the list of HTTP methods that the server handles built-in,
	i.e., without passing them to the registered service."

	^#('HEAD' 'TRACE' 'OPTIONS')