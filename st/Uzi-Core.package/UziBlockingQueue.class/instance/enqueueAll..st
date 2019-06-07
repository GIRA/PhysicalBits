as yet unclassified
enqueueAll: newItems
	^ monitor critical: [items addAll: newItems]