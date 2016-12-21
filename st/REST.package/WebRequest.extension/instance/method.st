*REST-Core
method
	"The request method (GET, POST etc)"
	| valid |
	valid := #(GET POST PUT PATCH DELETE HEAD OPTIONS).
	(valid includes: method)
		ifFalse: [method := valid 
			detect: [:each | method endsWith: each]
			ifNone: method].
	^method