accessing
returnPC
	| val |
	val := stack basicAt: fp + self currentScript variables size + 2.
	^ val bitAnd: 16rFFFF