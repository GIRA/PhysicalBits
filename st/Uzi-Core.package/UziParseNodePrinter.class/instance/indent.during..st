private
indent: level during: aBlock
	| old |
	old := indent.
	indent := indent + level.
	aBlock ensure: [indent := old]