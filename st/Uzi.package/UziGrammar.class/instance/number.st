primitives and tokens
number
	^ $- asParser optional, (#digit asParser plus, ($. asParser, #digit asParser plus) optional)