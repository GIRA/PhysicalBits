accessing
value
	| offset |
	self flag: #Richo. "This assumes the UNO layout!"
	offset := type = #A ifTrue: [14] ifFalse: [0].
	^ number + offset