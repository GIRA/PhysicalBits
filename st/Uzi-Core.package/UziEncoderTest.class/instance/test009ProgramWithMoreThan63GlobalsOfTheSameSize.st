tests
test009ProgramWithMoreThan63GlobalsOfTheSameSize
	| program |
	program := Uzi program: [:p | 
		1 to: 64 do: [:n | p registerGlobal: 'a', n asString value: n]].
	Uzi encode: program