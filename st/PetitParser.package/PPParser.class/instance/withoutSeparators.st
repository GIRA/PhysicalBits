operators-convenience
withoutSeparators
	"Filters out the separators from a parse result produced by one of the productions #delimitedBy: or #separatedBy:."
	
	^ self ==> [ :items |
		| result |
		result := Array new: items size + 1 // 2.
		1 to: result size do: [ :index | result at: index put: (items at: 2 * index - 1) ].
		result ]