visiting-utils
visitCollection: aCollection
	aCollection do: [:node | self visit: node]