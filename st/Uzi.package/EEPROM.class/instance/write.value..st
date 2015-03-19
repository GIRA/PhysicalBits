as yet unclassified
write: index value: value
	| i |
	i := index + 1.
	[^ bytes at: i put: value ]
		ensure: [| w |
			w := (writes at: i) + 1.
			writes at: i put: w.
			w > max ifTrue: [max := w]]