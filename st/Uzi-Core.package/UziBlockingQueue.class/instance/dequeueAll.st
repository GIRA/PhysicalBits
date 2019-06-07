as yet unclassified
dequeueAll
	^ monitor critical: [| tmp |
		tmp := items asArray.
		items removeAll.
		tmp]